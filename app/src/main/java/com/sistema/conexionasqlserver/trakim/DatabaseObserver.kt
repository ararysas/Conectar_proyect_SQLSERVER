package com.sistema.conexionasqlserver.trakim

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.util.Log
import com.sistema.conexionasqlserver.SincronizarTrakim


class DatabaseObserver(context: Context, handler: Handler) : ContentObserver(handler) {
    private val context = context

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // Iniciar la sincronización cuando haya un cambio
        Log.d("DatabaseObserver", "Cambios detectados en la base de datos.")
        SincronizarTrakim(context).sincronizarTrakim()
    }
}
