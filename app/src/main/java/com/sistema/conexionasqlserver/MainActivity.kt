package com.sistema.conexionasqlserver

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sistema.conexionasqlserver.Usuarios.LoginActivity

import java.util.concurrent.TimeUnit
class MainActivity : AppCompatActivity() {

    private lateinit var gpsRequestHelper: GPSRequestHelper
    private lateinit var locationManagerHelper: LocationManagerHelper
    private lateinit var usuarioViewModel: UsuarioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el LocationManagerHelper con el contexto
        LocationManagerHelper.initialize(this)

        // Inicializar el GPSRequestHelper
        gpsRequestHelper = GPSRequestHelper(this)

        // Inicializar el LocationManagerHelper
        locationManagerHelper = LocationManagerHelper

        // Inicializar el ViewModel
        usuarioViewModel = ViewModelProvider(this).get(UsuarioViewModel::class.java)

        // Verificar si ya existe una sesión
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId != -1) {
            // Si el usuario ya está logueado, obtener el usuario y verificar su rol
            usuarioViewModel.obtenerUsuarioPorId(this, userId)

            // Observar cambios en el usuario LiveData
            usuarioViewModel.usuarioLiveData.observe(this) { usuario ->
                usuario?.let {
                    if (it.rol == 1) {
                        // Si el rol es 1, continuar con la actividad principal
                        locationManagerHelper.requestLocationPermission(this) {
                            // Callback cuando los permisos son concedidos
                            startLocationService(userId)
                        }
                    } else {
                        // Si el rol no es 1, redirigir a otra actividad o mostrar mensaje
                        Log.d("MainActivity", "Acceso restringido para el usuario con rol: ${it.rol}")
                        Toast.makeText(this, "Acceso restringido para usuarios con rol distinto a 1.", Toast.LENGTH_SHORT).show()

                        // Redirigir a LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        } else {
            // Si no hay sesión, redirigir a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finaliza MainActivity si se redirige
        }

        // Programar la sincronización periódica
        scheduleSyncWork()
    }

    // Manejar el resultado de la activación del GPS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsRequestHelper.onActivityResult(requestCode, resultCode, data)
    }

    // Manejo de resultados de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Obtener el ID del usuario nuevamente
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        locationManagerHelper.handlePermissionsResult(requestCode, grantResults) {
            // Permiso concedido, verifica el GPS y la conexión a Internet antes de iniciar el servicio
            if (userId != -1) {
                startLocationService(userId)
            } else {
                Log.d("MainActivity", "Usuario no logueado, redirigiendo a LoginActivity.")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    // Método para iniciar el servicio de ubicación
    private fun startLocationService(userId: Int) {
        // Verificar que el GPS esté habilitado y que haya conexión a Internet
        if (LocationManagerHelper.isGPSOn(this) && LocationManagerHelper.isInternetAvailable(this)) {
            Log.d("MainActivity", "Iniciando servicio de ubicación para el usuario: $userId")
            // Aquí puedes agregar la lógica para iniciar el servicio de ubicación
            // Ejemplo: startService(Intent(this, LocationService::class.java))
        } else {
            Toast.makeText(this, "GPS desactivado o Internet no disponible.", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para programar la sincronización con WorkManager
    private fun scheduleSyncWork() {
        val syncWorkRequest = PeriodicWorkRequestBuilder<SincronizacionWorker>(15, TimeUnit.MINUTES)
            .build()

        // Encolar la solicitud de trabajo
        WorkManager.getInstance(this).enqueue(syncWorkRequest)
        Log.d("SyncWork", "Sincronización programada cada 15 minutos.")
    }
}
