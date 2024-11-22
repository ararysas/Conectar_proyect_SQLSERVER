package com.sistema.conexionasqlserver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import com.sistema.conexionasqlserver.Usuarios.Usuario
import kotlinx.coroutines.launch
import android.widget.ImageView
import com.sistema.conexionasqlserver.UsuarioViewModel

class DetalleUsuarioActivity : AppCompatActivity() {

    // Declaración de las vistas
    private lateinit var idTextView: TextView
    private lateinit var nombreTextView: TextView
    private lateinit var apellidoTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var activoTextView: TextView
    private lateinit var fechaCreaTextView: TextView
    private lateinit var rolTextView: TextView
    private lateinit var coTextView: TextView
    private lateinit var contraseñaTextView: TextView
    private lateinit var tiempoEsperaTextView: TextView
    private lateinit var movimientoTextView: TextView

    // EditText para campos editables
    private lateinit var smartphoneEditText: EditText
    private lateinit var horaInicioEditText: EditText
    private lateinit var horaFinalEditText: EditText
    private lateinit var botonActualizar: ImageView
    private lateinit var botonSeguir: Button

    private lateinit var gpsRequestHelper: GPSRequestHelper
    private lateinit var sincronizarTrakim: SincronizarTrakim

    // ViewModel para el usuario
    private val usuarioViewModel: UsuarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_usuario)

        gpsRequestHelper = GPSRequestHelper(this)
        sincronizarTrakim = SincronizarTrakim(this)

        // Inicializar vistas y configuraciones
        initViews()

        // Obtener el usuario pasado desde la actividad anterior
        val usuario = intent.getSerializableExtra("usuario") as? Usuario
        usuario?.let { usuarioViewModel.setUsuario(it) }

        // Observar cambios en el usuario LiveData
        usuarioViewModel.usuarioLiveData.observe(this) { usuario ->
            usuario?.let { actualizarVistaUsuario(it) }
        }

        // Observar el estado de la actualización
        usuarioViewModel.updateStatusLiveData.observe(this) { success ->
            val mensaje = if (success) "Usuario actualizado correctamente." else "Error al actualizar el usuario."
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón de actualizar
        botonActualizar.setOnClickListener {
            actualizarUsuario()
        }

        // Configurar el botón de seguir
        botonSeguir.setOnClickListener {
            usuarioViewModel.usuarioLiveData.value?.let { user ->
                seguirUsuario(user)
            }
        }
    }

    private fun initViews() {
        idTextView = findViewById(R.id.idTextView)
        nombreTextView = findViewById(R.id.nombreTextView)
        apellidoTextView = findViewById(R.id.apellidoTextView)
        emailTextView = findViewById(R.id.emailTextView)
        activoTextView = findViewById(R.id.activoTextView)
        fechaCreaTextView = findViewById(R.id.fechaCreaTextView)
        rolTextView = findViewById(R.id.rolTextView)
        coTextView = findViewById(R.id.coTextView)
        contraseñaTextView = findViewById(R.id.contraseñaTextView)
        tiempoEsperaTextView  = findViewById(R.id.tiempoEsperaTextView)
        movimientoTextView  = findViewById(R.id.movimientoTextView)

        // campos editables TextView
        smartphoneEditText = findViewById(R.id.smartphoneEditText)
        horaInicioEditText = findViewById(R.id.horaInicioEditText)
        horaFinalEditText = findViewById(R.id.horaFinalEditText)
        botonActualizar = findViewById(R.id.botonActualizar)
        botonSeguir = findViewById(R.id.botonSeguir)
    }

    private fun actualizarVistaUsuario(usuario: Usuario) {
        idTextView.text = "${usuario.id}"
        nombreTextView.text = usuario.nombre
        apellidoTextView.text = usuario.apellido
        emailTextView.text = usuario.email
        activoTextView.text = if (usuario.activo == 1) "Activo" else "Inactivo"
        fechaCreaTextView.text = usuario.fechaCrea
        rolTextView.text = usuario.rol.toString()


        // Concatenar " minutos" a los valores de tiempoEspera y movimiento
        tiempoEsperaTextView .setText("${usuario.tiempoEspera} minutos")
        movimientoTextView .setText("${usuario.movimiento} minutos")

        smartphoneEditText.setText(usuario.smartphone ?: "Sin Smartphone")

        // Concatenar AM y PM directamente
        horaInicioEditText.setText("${usuario.horaInicio}:00AM")
        horaFinalEditText.setText("${usuario.horaFinal}:00PM")

        coTextView.text = usuario.co.toString()
        contraseñaTextView.text = usuario.contraseña
    }

    private fun actualizarUsuario() {
        usuarioViewModel.usuarioLiveData.value?.let { user ->
            // Crear un nuevo objeto Usuario con los valores actualizados
            val usuarioActualizado = user.copy(
                tiempoEspera = tiempoEsperaTextView .text.toString().replace(" minutos", "").toIntOrNull() ?: user.tiempoEspera,
                movimiento = movimientoTextView .text.toString().replace(" minutos", "").toIntOrNull() ?: user.movimiento,
                smartphone = smartphoneEditText.text.toString(),
                horaInicio = horaInicioEditText.text.toString().replace(":00AM", "").toIntOrNull() ?: user.horaInicio,
                horaFinal = horaFinalEditText.text.toString().replace(":00PM", "").toIntOrNull() ?: user.horaFinal
            )

            // Actualizar usuario en ViewModel
            usuarioViewModel.actualizarUsuario(applicationContext, usuarioActualizado)
        }
    }

    private fun seguirUsuario(usuario: Usuario) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("USER_ID", usuario.id.toInt())
            apply()
        }
        gpsRequestHelper.iniciarVerificacionGPS(usuario.id.toInt())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsRequestHelper.onActivityResult(requestCode, resultCode, data)
    }
}
