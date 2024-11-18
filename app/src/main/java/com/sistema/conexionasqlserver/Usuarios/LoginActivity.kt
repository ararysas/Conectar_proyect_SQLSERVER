package com.sistema.conexionasqlserver.Usuarios

import com.sistema.conexionasqlserver.MainActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sistema.conexionasqlserver.DatabaseHelper
import com.sistema.conexionasqlserver.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameField = findViewById(R.id.nameField)
        passwordField = findViewById(R.id.passwordField)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = nameField.text.toString()
            val password = passwordField.text.toString()

            Log.d("Login", "Intentando iniciar sesión con: $username")

            // Verificar que los campos no estén vacíos
            if (username.isNotEmpty() && password.isNotEmpty()) {
                autenticarUsuario(username, password)
            } else {
                Log.w("Login", "Nombre de usuario o contraseña vacíos")
                Toast.makeText(this, "Por favor ingrese nombre y contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun autenticarUsuario(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Login", "Autenticando usuario: $username")

            // Validar usuario y obtener el rol
            val (isAuthenticated, role) = DatabaseHelper.validarUsuario(username, password)

            withContext(Dispatchers.Main) {
                if (isAuthenticated) {
                    Log.d("Login", "Inicio de sesión exitoso para: $username")

                    // Verificar si el rol es 1
                    if (role == 1) {
                        // Si el rol es 1, redirigir a Pantalla2Activity
                        val intent = Intent(this@LoginActivity,UsuariosPantalla2Activity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Si el rol no es 1, mostrar mensaje de acceso restringido
                        Log.d("Login", "Acceso restringido para el usuario con rol: $role")
                        Toast.makeText(this@LoginActivity, "Acceso restringido para este Usuario.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("Login", "Usuario o contraseña incorrectos: $username")
                    Toast.makeText(this@LoginActivity, "Nombre de usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
