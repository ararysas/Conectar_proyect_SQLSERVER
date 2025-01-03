package com.sistema.conexionasqlserver

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.sql.SQLException
import com.sistema.conexionasqlserver.Conexion_servidor.DatabaseConfig

object DatabaseHelper {

    private const val DB_URL = DatabaseConfig.DB_URL
    private const val DB_USER = DatabaseConfig.DB_USER
    private const val DB_PASSWORD = DatabaseConfig.DB_PASSWORD

    // Bloque de inicialización para cargar el driver JDBC solo una vez
    init {
        try {
            // Cargar el driver JDBC solo una vez cuando se inicializa la clase DatabaseHelper
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            Log.d("DatabaseHelper", "Driver JDBC cargado correctamente.")
        } catch (e: ClassNotFoundException) {
            Log.e("DatabaseHelper", "Error al cargar el driver: ${e.message}", e)
        }
    }

    // Método para guardar la ubicación en la base de datos remota
    suspend fun saveLocation(userId: Int, latitude: Double, longitude: Double, nota: String, codigo: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DatabaseHelper", "Intentando conectar con la base de datos.")
                DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD).use { connection ->
                    Log.d("DatabaseHelper", "Conexión establecida.")

                    val query = """
                        INSERT INTO t010_trakim (f010_ID_User, f010_Coordenadas, f010_Fecha, f010_Nota, f010_codigo)
                        VALUES (?, ?, GETDATE(), ?, ?)
                    """
                    connection.prepareStatement(query).use { preparedStatement ->
                        val coordenadas = "$latitude,$longitude"
                        preparedStatement.setInt(1, userId)
                        preparedStatement.setString(2, coordenadas)
                        preparedStatement.setString(3, nota)
                        preparedStatement.setInt(4, codigo)

                        Log.d("DatabaseHelper", "Parámetros de inserción: UserID=$userId, Coordenadas=$coordenadas, Nota='$nota', Código=$codigo")

                        val rowsInserted = preparedStatement.executeUpdate()
                        if (rowsInserted > 0) {
                            Log.d("DatabaseHelper", "Ubicación guardada exitosamente con nota '$nota' y código $codigo.")
                            return@withContext true
                        } else {
                            Log.e("DatabaseHelper", "No se pudo guardar la ubicación. Filas afectadas: $rowsInserted")
                            return@withContext false
                        }
                    }
                }
            } catch (e: SQLException) {
                Log.e("DatabaseError", "Error SQL: ${e.message}", e)
                return@withContext false
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error inesperado: ${e.message}", e)
                return@withContext false
            }
        }
    }

    // Método para obtener las ubicaciones pendientes desde SQLite
    private suspend fun getPendingLocationsFromSQLite(context: Context): List<LocationData> {
        return SQLiteHelper(context).getPendingLocations()
    }

    // Método para eliminar la ubicación de SQLite
    private suspend fun deleteLocationFromSQLite(context: Context, location: LocationData): Boolean {
        return SQLiteHelper(context).deleteLocation(location)
    }

    // Método para verificar y enviar ubicaciones pendientes
    suspend fun checkAndSendPendingLocations(context: Context) {
        val pendingLocations = getPendingLocationsFromSQLite(context)
        for (location in pendingLocations) {
            val coordinates = location.coordinates.split(",")
            val latitude = coordinates[0].toDouble()
            val longitude = coordinates[1].toDouble()
            val success = saveLocation(location.userId, latitude, longitude, location.note, location.code)
            if (success) {
                Log.d("DatabaseHelper", "Ubicación enviada exitosamente al servidor.")
                deleteLocationFromSQLite(context, location) // Eliminar la ubicación enviada
            } else {
                Log.e("DatabaseHelper", "Fallo al enviar la ubicación al servidor.")
            }
        }
    }

    // Método para obtener las últimas coordenadas del usuario
    suspend fun getLastCoordinates(userId: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD).use { connection ->
                    val query = """
                        SELECT TOP 1 f010_Coordenadas
                        FROM t010_trakim 
                        WHERE f010_ID_User = ?
                        ORDER BY f010_Fecha DESC
                    """
                    connection.prepareStatement(query).use { preparedStatement ->
                        preparedStatement.setInt(1, userId)
                        val resultSet = preparedStatement.executeQuery()
                        if (resultSet.next()) {
                            resultSet.getString("f010_Coordenadas")
                        } else {
                            null
                        }
                    }
                }
            } catch (e: SQLException) {
                Log.e("DatabaseError", "Error SQL: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error inesperado: ${e.message}", e)
                null
            }
        }
    }

    // Método para validar el usuario
    suspend fun validarUsuario(username: String, password: String): Pair<Boolean, Int?> {
        return withContext(Dispatchers.IO) {
            try {
                DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD).use { connection ->
                    val query = """
                    SELECT f001_ID_usuarios, f001_rol FROM t001_usuarios 
                    WHERE f001_Nombre = ? AND f001_contraseña = ?
                """
                    connection.prepareStatement(query).use { preparedStatement ->
                        preparedStatement.setString(1, username)
                        preparedStatement.setString(2, password)

                        val resultSet = preparedStatement.executeQuery()
                        if (resultSet.next()) {
                            val role = resultSet.getInt("f001_rol")
                            return@withContext Pair(true, role) // Usuario validado, devuelve el rol
                        } else {
                            return@withContext Pair(false, null) // Usuario no encontrado
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error en la validación del usuario: ${e.message}", e)
                return@withContext Pair(false, null)
            }
        }
    }

    // Método para obtener las horas restringidas del usuario
    suspend fun getUserRestrictedHours(userId: Int): Pair<Int, Int>? {
        return withContext(Dispatchers.IO) {
            try {
                DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD).use { connection ->
                    val query = """
                        SELECT f001_hora_inicio, f001_hora_final
                        FROM t001_usuarios
                        WHERE f001_ID_usuarios = ?
                    """
                    connection.prepareStatement(query).use { preparedStatement ->

                        preparedStatement.setInt(1, userId)
                        val resultSet = preparedStatement.executeQuery()
                        if (resultSet.next()) {
                            val startHour = resultSet.getInt("f001_hora_inicio")
                            val endHour = resultSet.getInt("f001_hora_final")
                            return@withContext Pair(startHour, endHour)
                        } else {
                            null
                        }
                    }
                }
            } catch (e: SQLException) {
                Log.e("DatabaseError", "Error SQL: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error inesperado: ${e.message}", e)
                null
            }
        }
    }
}

// Clase de datos para representar la ubicación
data class LocationInfo(
    val userId: Int,
    val latitude: Double,
    val longitude: Double,
    val nota: String,
    val codigo: Int
)



