package com.sistema.conexionasqlserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Build


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("BootReceiver", "El dispositivo se ha reiniciado.")

                // Recuperar el USER_ID de SharedPreferences
                val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                val userId = sharedPreferences.getInt("USER_ID", -1)

                if (userId != -1) {
                    val serviceIntent = Intent(context, LocationService::class.java).apply {
                        putExtra("USER_ID", userId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    // Guardar evento en la base de datos
                    saveEventToDatabase(context, userId, "Dispositivo reiniciado", 4)
                }
            }
        }
    }

    private fun saveEventToDatabase(context: Context, userId: Int, nota: String, codigo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseHelper.saveLocation(userId, 0.0, 0.0, nota, codigo)
        }
    }
}
