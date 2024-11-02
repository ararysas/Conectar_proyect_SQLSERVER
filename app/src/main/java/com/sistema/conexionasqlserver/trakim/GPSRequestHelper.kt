package com.sistema.conexionasqlserver

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import android.util.Log

import com.sistema.conexionasqlserver.SincronizarTrakim

class GPSRequestHelper(private val activity: Activity) {
    private val connectionManager = ConnectionManager(activity)

    fun iniciarVerificacionGPS(userId: Int) {
        connectionManager.requestLocationPermission {
            verificarGPS(userId)
        }
    }

    private fun verificarGPS(userId: Int) {
        Log.d("GPSRequestHelper", "Iniciando verificación de GPS.")
        if (!connectionManager.isGPSEnabled()) {
            Toast.makeText(activity, "Por favor, habilita el GPS.", Toast.LENGTH_SHORT).show()
            // Aquí podrías abrir la configuración del GPS si es necesario
        } else {
            startTracking(userId)
        }
    }

    private fun startTracking(userId: Int) {
        val serviceIntent = Intent(activity, LocationService::class.java).apply {
            putExtra("USER_ID", userId)
        }
        activity.startService(serviceIntent)
        Toast.makeText(activity, "Servicio de ubicación iniciado.", Toast.LENGTH_SHORT).show()

        // Aquí puedes llamar a sincronizarTrakim si es necesario
        val sincronizador = SincronizarTrakim(activity)
        sincronizador.sincronizarTrakim()
    }

    // Implementa onActivityResult si lo necesitas
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Manejo de resultados de actividad si es necesario
    }
}
