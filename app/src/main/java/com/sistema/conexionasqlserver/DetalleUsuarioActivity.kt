package com.sistema.conexionasqlserver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sistema.conexionasqlserver.Usuarios.Usuario


class DetalleUsuarioActivity : AppCompatActivity() {

    // Declaración de las vistas
    private lateinit var idTextView: TextView
    private lateinit var nombreTextView: TextView
    private lateinit var apellidoTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var activoTextView: TextView
    private lateinit var fechaCreaTextView: TextView
    private lateinit var rolTextView: TextView
    private lateinit var tiempoEsperaTextView: TextView
    private lateinit var coTextView: TextView
    private lateinit var smartphoneTextView: TextView
    private lateinit var movimientoTextView: TextView
    private lateinit var contraseñaTextView: TextView
    private lateinit var horaInicioTextView: TextView
    private lateinit var horaFinalTextView: TextView
    private lateinit var botonSeguir: Button // Botón para seguir al usuario

    private lateinit var gpsRequestHelper: GPSRequestHelper // Instancia de GPSRequestHelper
    private lateinit var sincronizarTrakim: SincronizarTrakim // Instancia de sincronización

    // Definir la variable de usuario a nivel de clase
    private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_usuario)

        // Inicializar helpers
        gpsRequestHelper = GPSRequestHelper(this)
        sincronizarTrakim = SincronizarTrakim(this)

        // Inicializar los TextViews
        idTextView = findViewById(R.id.idTextView)
        nombreTextView = findViewById(R.id.nombreTextView)
        apellidoTextView = findViewById(R.id.apellidoTextView)
        emailTextView = findViewById(R.id.emailTextView)
        activoTextView = findViewById(R.id.activoTextView)
        fechaCreaTextView = findViewById(R.id.fechaCreaTextView)
        rolTextView = findViewById(R.id.rolTextView)
        tiempoEsperaTextView = findViewById(R.id.tiempoEsperaTextView)
        coTextView = findViewById(R.id.coTextView)
        smartphoneTextView = findViewById(R.id.smartphoneTextView)
        movimientoTextView = findViewById(R.id.movimientoTextView)
        contraseñaTextView = findViewById(R.id.contraseñaTextView)
        horaInicioTextView = findViewById(R.id.horaInicioTextView)
        horaFinalTextView = findViewById(R.id.horaFinalTextView)
        botonSeguir = findViewById(R.id.botonSeguir)

        // Obtener el usuario pasado desde la actividad anterior
        usuario = intent.getSerializableExtra("usuario") as? Usuario

        // Configurar los TextViews con la información del usuario
        usuario?.let {
            idTextView.text = it.id
            nombreTextView.text = it.nombre
            apellidoTextView.text = it.apellido
            emailTextView.text = it.email
            activoTextView.text = if (it.activo == 1) "Activo" else "Inactivo"
            fechaCreaTextView.text = it.fechaCrea
            rolTextView.text = it.rol.toString()
            tiempoEsperaTextView.text = it.tiempoEspera.toString()
            coTextView.text = it.co.toString()
            movimientoTextView.text = it.movimiento.toString()
            contraseñaTextView.text = it.contraseña
            smartphoneTextView.text = it.smartphone ?: "Sin Smartphone"
            horaInicioTextView.text = it.horaInicio.toString()
            horaFinalTextView.text = it.horaFinal.toString()
        } ?: run {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón de seguir
        botonSeguir.setOnClickListener {
            usuario?.let { user ->
                seguirUsuario(user)
            }
        }
    }

    private fun seguirUsuario(usuario: Usuario) {
        // Almacenar el ID del usuario en SharedPreferences
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("USER_ID", usuario.id.toInt())
            apply()
        }

        // Iniciar la verificación de GPS
        gpsRequestHelper.iniciarVerificacionGPS(usuario.id.toInt())
    }

    // Llamado cuando se recibe el resultado de la activación del GPS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsRequestHelper.onActivityResult(requestCode, resultCode, data)
    }
}
