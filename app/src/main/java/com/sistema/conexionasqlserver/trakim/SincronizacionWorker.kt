package com.sistema.conexionasqlserver

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

class SincronizacionWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "SincronizacionWorker"

    override fun doWork(): Result {
        return try {
            // Lógica de sincronización
            val sincronizador = SincronizarTrakim(applicationContext)
            if (sincronizador.hayDatosEnSQLite()) {
                sincronizador.sincronizarTrakim()
                Log.d(TAG, "Sincronización completada con éxito.")
            } else {
                Log.d(TAG, "No hay datos para sincronizar.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en SincronizacionWorker: ${e.message}", e)
            Result.retry()
        }
    }
}
