package com.sistema.conexionasqlserver.trakim

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.util.Log
import com.sistema.conexionasqlserver.SincronizarTrakim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseObserver(context: Context, handler: Handler) : ContentObserver(handler) {
    private val appContext = context.applicationContext // Usar el contexto de la aplicación

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // Iniciar la sincronización cuando haya un cambio
        Log.d("DatabaseObserver", "Cambios detectados en la base de datos.")

        // Usar CoroutineScope para lanzar la sincronización
        CoroutineScope(Dispatchers.IO).launch {
            val sincronizador = SincronizarTrakim(appContext)
            sincronizador.sincronizarTrakim()
        }
    }
}
