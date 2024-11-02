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
            if (!isInternetAvailable()) {
                Log.e(TAG, "No hay conexión a Internet, no se puede sincronizar.")
                return@launch
            }

            var connection: Connection? = null
            val dbHelper = SQLiteHelper(context)
            val sqliteDb = dbHelper.writableDatabase // Usa writableDatabase para poder eliminar

            try {
                Log.d(TAG, "Conectando a SQL Server...")
                connection = DriverManager.getConnection(url, user, password)

                // Obtener registros en SQLite
                val cursor = sqliteDb.rawQuery("SELECT * FROM t010_trakim", null)

                // Recorrer los registros y sincronizarlos
                while (cursor.moveToNext()) {
                    val userId = cursor.getInt(cursor.getColumnIndexOrThrow("f010_ID_User"))
                    val coordenadas = cursor.getString(cursor.getColumnIndexOrThrow("f010_Coordenadas"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("f010_Fecha"))
                    val nota = cursor.getString(cursor.getColumnIndexOrThrow("f010_Nota"))
                    val codigo = cursor.getInt(cursor.getColumnIndexOrThrow("f010_codigo"))

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

                    // Eliminar el registro sincronizado de SQLite
                    val deleteQuery = "DELETE FROM t010_trakim WHERE f010_ID_User = ? AND f010_Fecha = ?"
                    val deleteStmt = sqliteDb.compileStatement(deleteQuery)
                    deleteStmt.bindLong(1, userId.toLong())
                    deleteStmt.bindString(2, fecha)
                    deleteStmt.executeUpdateDelete()
                }
                cursor.close()
                Log.d(TAG, "Sincronización completada y registros eliminados exitosamente.")

            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar: ${e.message}", e)
            } finally {
                try {
                    connection?.close()
                    Log.d(TAG, "Conexión cerrada.")
                } catch (e: SQLException) {
                    Log.e(TAG, "Error al cerrar conexión: ${e.message}", e)
                }
                sqliteDb.close() // Cerrar la base de datos al final
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
