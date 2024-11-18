package com.sistema.conexionasqlserver

import android.content.Intent
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class GPSRequestHelper(private val activity: AppCompatActivity) {  // Cambiar a AppCompatActivity

    fun iniciarVerificacionGPS(userId: Int) {
        // Solicitar permisos de ubicación
        LocationManagerHelper.requestLocationPermission(activity) {
            verificarGPS(userId)
        }
    }

    private fun verificarGPS(userId: Int) {
        Log.d("GPSRequestHelper", "Iniciando verificación de GPS.")
        // Aquí se usa el método isGPSOn correctamente
        if (!LocationManagerHelper.isGPSOn(activity)) {
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
