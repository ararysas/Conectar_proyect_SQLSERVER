package com.sistema.conexionasqlserver.Usuarios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sistema.conexionasqlserver.Conexion_servidor.DatabaseConfig
import com.sistema.conexionasqlserver.R
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class UsuariosPantalla2Activity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter
    private val usuarios = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_pantalla2)

        // Inicializa el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUsuarios)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Llama a la función que carga los usuarios con rol 2 y que están activos
        cargarUsuariosConRol2Activos()
    }

    // Función que carga los usuarios con rol 2 que están activos y los ordena por código de despacho
    private fun cargarUsuariosConRol2Activos() {
        val url = DatabaseConfig.DB_URL
        val user = DatabaseConfig.DB_USER
        val password = DatabaseConfig.DB_PASSWORD

        // Ejecuta la consulta en un hilo separado para evitar bloquear la interfaz de usuario
        Thread {
            var connection: Connection? = null
            try {
                // Conexión a la base de datos
                connection = DriverManager.getConnection(url, user, password)
                val statement: Statement = connection.createStatement()

                // Cambiar la consulta SQL para filtrar usuarios con rol 2, que están activos, y ordenarlos por código de despacho de mayor a menor
                val resultSet: ResultSet = statement.executeQuery(
                    "SELECT * FROM t001_usuarios WHERE f001_rol = 2 AND f001_activo = 1 ORDER BY f001_co DESC"
                )

                // Itera sobre los resultados de la consulta
                while (resultSet.next()) {
                    val id = resultSet.getInt("f001_ID_usuarios") // Ahora como Int
                    val nombre = resultSet.getString("f001_Nombre") ?: ""
                    val apellido = resultSet.getString("f001_Apellido") ?: ""
                    val email = resultSet.getString("f001_Email") ?: ""
                    val contraseña = resultSet.getString("f001_contraseña") ?: ""
                    val activo = resultSet.getInt("f001_activo")
                    val fechaCrea = resultSet.getString("f001_fecha_crea") ?: ""
                    val rol = resultSet.getInt("f001_rol")
                    val movimiento = resultSet.getInt("f001_movimiento")
                    val tiempoEspera = resultSet.getInt("f001_Tiempo_espera")
                    val co = resultSet.getString("f001_co")
                    val smartphone = resultSet.getString("f001_smartphone") ?: "Sin Smartphone"
                    val horaInicio = resultSet.getInt("f001_hora_Inicio")
                    val horaFinal = resultSet.getInt("f001_hora_final")

                    // Crea el objeto Usuario
                    val usuario = Usuario(
                        id, nombre, apellido, contraseña, email, activo, fechaCrea, rol,
                        movimiento, tiempoEspera, co, smartphone, horaInicio, horaFinal
                    )
                    usuarios.add(usuario)
                }

                // Actualiza la interfaz de usuario desde el hilo principal
                runOnUiThread {
                    usuarioAdapter = UsuarioAdapter(usuarios)
                    recyclerView.adapter = usuarioAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Muestra un mensaje de error en la interfaz
                runOnUiThread {
                    Toast.makeText(this, "Error al cargar los usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                try {
                    connection?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
