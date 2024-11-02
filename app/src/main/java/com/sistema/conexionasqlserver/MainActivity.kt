package com.sistema.conexionasqlserver

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sistema.conexionasqlserver.Usuarios.LoginActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gpsRequestHelper: GPSRequestHelper
    private lateinit var connectionManager: ConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar SQLiteHelper (asegúrate de que esta clase esté bien implementada)


        // Inicializar el GPSRequestHelper
        gpsRequestHelper = GPSRequestHelper(this)

        // Inicializar el ConnectionManager
        connectionManager = ConnectionManager(this)

        // Verificar si ya existe una sesión
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1)

        if (userId != -1) {
            // Si el usuario ya está logueado, solicitar permisos de ubicación
            connectionManager.requestLocationPermission {
                startLocationService(userId) // Iniciar servicio de ubicación con el userId
            }
        } else {
            // Si no hay sesión, redirigir a LoginActivity
            val intent = Intent(this, LoginActivity::class.java) // Asegúrate de que LoginActivity esté importada
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

        connectionManager.handlePermissionsResult(requestCode, grantResults) {
            // Permiso concedido, verifica el GPS y la conexión a Internet antes de iniciar el servicio
            if (userId != -1) {
                startLocationService(userId) // Se invoca con el userId
            }
        }
    }

    // Método para iniciar el servicio de ubicación
    private fun startLocationService(userId: Int) {
        // Verificar que el GPS esté habilitado y que haya conexión a Internet
        if (connectionManager.isGPSEnabled() && connectionManager.isInternetAvailable()) {
            Log.d("MainActivity", "Iniciando servicio de ubicación para el usuario: $userId")
            // Aquí puedes agregar la lógica para iniciar el servicio de ubicación
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
