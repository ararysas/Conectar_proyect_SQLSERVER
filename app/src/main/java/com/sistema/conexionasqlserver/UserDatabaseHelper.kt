package com.sistema.conexionasqlserver

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import com.sistema.conexionasqlserver.Usuarios.Usuario
import com.sistema.conexionasqlserver.Conexion_servidor.DatabaseConfig

object UserDatabaseHelper {

    // Configuración de la conexión a la base de datos
    private const val DB_URL = DatabaseConfig.DB_URL
    private const val DB_USER = DatabaseConfig.DB_USER
    private const val DB_PASSWORD = DatabaseConfig.DB_PASSWORD

    // Función para actualizar los datos del usuario en la base de datos
    suspend fun updateUsuario(context: Context, usuario: Usuario): Boolean {
        return withContext(Dispatchers.IO) {
            var connection: Connection? = null
            var preparedStatement: java.sql.PreparedStatement? = null
            try {
                // Cargar el driver JDBC
                Class.forName("net.sourceforge.jtds.jdbc.Driver")

                // Establecer la conexión a la base de datos
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)

                // Consulta SQL de actualización solo con los campos relevantes
                val query = """
                    UPDATE t001_usuarios
                    SET 
                        f001_nombre = ?, 
                        f001_apellido = ?, 
                        f001_email = ?, 
                        f001_activo = ?, 
                        f001_rol = ?, 
                        f001_tiempo_espera = ?, 
                        f001_co = ?, 
                        f001_smartphone = ?, 
                        f001_movimiento = ?, 
                        f001_contraseña = ?, 
                        f001_hora_inicio = ?, 
                        f001_hora_final = ?
                    WHERE f001_ID_usuarios = ?
                """

                // Preparar la sentencia
                preparedStatement = connection.prepareStatement(query).apply {
                    setString(1, usuario.nombre)
                    setString(2, usuario.apellido)
                    setString(3, usuario.email)
                    setInt(4, usuario.activo)
                    setInt(5, usuario.rol)
                    setInt(6, usuario.tiempoEspera)  // Tiempo de espera en minutos
                    setString(7, usuario.co)
                    setString(8, usuario.smartphone ?: "Sin Smartphone")
                    setInt(9, usuario.movimiento)
                    setString(10, usuario.contraseña)
                    setInt(11, usuario.horaInicio)
                    setInt(12, usuario.horaFinal)
                    setInt(13, usuario.id)
                }

                // Ejecutar la actualización y verificar si se actualizó alguna fila
                val rowsUpdated = preparedStatement.executeUpdate()
                if (rowsUpdated > 0) {
                    // Obtener el usuario actualizado directamente desde la base de datos
                    val usuarioActualizado = getUsuarioById(context, usuario.id)
                    usuarioActualizado != null  // Retorna verdadero si se obtuvo el usuario actualizado
                } else {
                    false
                }
            } catch (e: SQLException) {
                Log.e("UserDatabaseHelper", "Error SQL: ${e.message}", e)
                false
            } catch (e: ClassNotFoundException) {
                Log.e("UserDatabaseHelper", "Error al cargar el driver: ${e.message}", e)
                false
            } catch (e: Exception) {
                Log.e("UserDatabaseHelper", "Error inesperado: ${e.message}", e)
                false
            } finally {
                try {
                    preparedStatement?.close()  // Cerrar PreparedStatement
                    connection?.close()  // Cerrar conexión
                } catch (e: SQLException) {
                    Log.e("UserDatabaseHelper", "Error al cerrar los recursos: ${e.message}")
                }
            }
        }
    }

    // Función para obtener un usuario por su ID desde la base de datos
    suspend fun getUsuarioById(context: Context, userId: Int): Usuario? {
        return withContext(Dispatchers.IO) {
            var connection: Connection? = null
            var preparedStatement: java.sql.PreparedStatement? = null
            var resultSet: java.sql.ResultSet? = null
            try {
                // Cargar el driver JDBC
                Class.forName("net.sourceforge.jtds.jdbc.Driver")

                // Establecer conexión a la base de datos
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)

                // Consulta SQL para obtener el usuario por ID
                val query = "SELECT * FROM t001_usuarios WHERE f001_ID_usuarios = ?"
                preparedStatement = connection.prepareStatement(query).apply {
                    setInt(1, userId)
                }

                resultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    Usuario(
                        id = resultSet.getInt("f001_ID_usuarios"),
                        nombre = resultSet.getString("f001_nombre"),
                        apellido = resultSet.getString("f001_apellido"),
                        email = resultSet.getString("f001_email"),
                        activo = resultSet.getInt("f001_activo"),
                        rol = resultSet.getInt("f001_rol"),
                        tiempoEspera = resultSet.getInt("f001_tiempo_espera"),
                        co = resultSet.getString("f001_co"),
                        smartphone = resultSet.getString("f001_smartphone"),
                        movimiento = resultSet.getInt("f001_movimiento"),
                        contraseña = resultSet.getString("f001_contraseña"),
                        horaInicio = resultSet.getInt("f001_hora_inicio"),
                        horaFinal = resultSet.getInt("f001_hora_final"),
                        fechaCrea = resultSet.getString("f001_fecha_crea")  // Agregar fechaCrea
                    )
                } else {
                    null
                }
            } catch (e: SQLException) {
                Log.e("UserDatabaseHelper", "Error SQL: ${e.message}", e)
                null
            } catch (e: ClassNotFoundException) {
                Log.e("UserDatabaseHelper", "Error al cargar el driver: ${e.message}", e)
                null
            } catch (e: Exception) {
                Log.e("UserDatabaseHelper", "Error inesperado: ${e.message}", e)
                null
            } finally {
                try {
                    resultSet?.close()  // Cerrar ResultSet
                    preparedStatement?.close()  // Cerrar PreparedStatement
                    connection?.close()  // Cerrar conexión
                } catch (e: SQLException) {
                    Log.e("UserDatabaseHelper", "Error al cerrar los recursos: ${e.message}")
                }
            }
        }
    }
}
