package com.sistema.conexionasqlserver

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.sistema.conexionasqlserver.Conexion_servidor.DatabaseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SincronizarTrakim(private val context: Context) {

    private val TAG = "SincronizarTrakim"
    private val url = DatabaseConfig.DB_URL
    private val user = DatabaseConfig.DB_USER
    private val password = DatabaseConfig.DB_PASSWORD

    // Método para sincronizar datos desde SQLite a SQL Server
    fun sincronizarTrakim() {
        CoroutineScope(Dispatchers.IO).launch {
            // Verifica si hay conexión a Internet
            if (!isInternetAvailable()) {
                Log.e(TAG, "No hay conexión a Internet, no se puede sincronizar.")
                return@launch
            }

            // Verifica si hay datos en SQLite para sincronizar
            if (!hayDatosEnSQLite()) {
                Log.d(TAG, "No hay datos en SQLite para sincronizar.")
                return@launch
            }

            var connection: Connection? = null
            val dbHelper = SQLiteHelper(context)
            val sqliteDb = dbHelper.writableDatabase

            try {
                // Conectar a SQL Server
                Log.d(TAG, "Conectando a SQL Server...")
                connection = DriverManager.getConnection(url, user, password)
                connection.autoCommit = false  // Inicia una transacción en SQL Server

                // Obtener registros en SQLite
                val cursor = sqliteDb.rawQuery("SELECT * FROM t010_trakim", null)

                // Sincronizar registros
                while (cursor.moveToNext()) {
                    val userId = cursor.getInt(cursor.getColumnIndexOrThrow("f010_ID_User"))
                    val coordenadas = cursor.getString(cursor.getColumnIndexOrThrow("f010_Coordenadas"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("f010_Fecha"))
                    val nota = cursor.getString(cursor.getColumnIndexOrThrow("f010_Nota"))
                    val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("f010_codigo"))

                    try {
                        // Preparar consulta de inserción en SQL Server
                        val insertQuery = """
                            INSERT INTO t010_trakim (f010_ID_User, f010_Coordenadas, f010_Fecha, f010_Nota, f010_codigo) 
                            VALUES (?, ?, ?, ?, ?)
                        """
                        val stmt = connection.prepareStatement(insertQuery)
                        stmt.setInt(1, userId)
                        stmt.setString(2, coordenadas)
                        stmt.setString(3, fecha)
                        stmt.setString(4, nota)
                        stmt.setInt(5, codigo)
                        stmt.executeUpdate()

                        // Eliminar el registro de SQLite solo si se inserta correctamente en SQL Server
                        sqliteDb.beginTransaction()  // Inicia una transacción en SQLite
                        val deleteQuery = "DELETE FROM t010_trakim WHERE f010_ID_User = ? AND f010_Fecha = ?"
                        val deleteStmt = sqliteDb.compileStatement(deleteQuery)
                        deleteStmt.bindLong(1, userId.toLong())
                        deleteStmt.bindString(2, fecha)
                        deleteStmt.executeUpdateDelete()
                        sqliteDb.setTransactionSuccessful()  // Confirma la eliminación
                        Log.d(TAG, "Registro sincronizado y eliminado: Usuario ID $userId, Fecha $fecha")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error al sincronizar el registro de Usuario ID $userId, Fecha $fecha: ${e.message}", e)
                    } finally {
                        sqliteDb.endTransaction()  // Termina la transacción de SQLite
                    }
                }
                cursor.close()
                connection.commit()  // Confirma la transacción en SQL Server
                Log.d(TAG, "Sincronización completada.")

            } catch (e: Exception) {
                Log.e(TAG, "Error general al sincronizar: ${e.message}", e)
                connection?.rollback()  // Reversión en SQL Server si falla

            } finally {
                try {
                    connection?.close()
                    Log.d(TAG, "Conexión a SQL Server cerrada.")
                } catch (e: SQLException) {
                    Log.e(TAG, "Error al cerrar conexión SQL Server: ${e.message}", e)
                }
                sqliteDb.close()
            }
        }
    }

    // Método para verificar si hay datos en SQLite
    fun hayDatosEnSQLite(): Boolean {
        val dbHelper = SQLiteHelper(context)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM t010_trakim", null)

        return if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            cursor.close()
            count > 0
        } else {
            cursor.close()
            false
        }
    }

    // Método para verificar la conectividad a Internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
