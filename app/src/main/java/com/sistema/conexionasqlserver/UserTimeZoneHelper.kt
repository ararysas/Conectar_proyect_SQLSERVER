package com.sistema.conexionasqlserver

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object UserTimeZoneHelper {

    // Obtiene el horario permitido del usuario desde la base de datos a través de DatabaseHelper
    suspend fun getUserRestrictedHours(userId: Int): Pair<Int, Int>? {
        return withContext(Dispatchers.IO) {
            try {
                // Llamada directa a DatabaseHelper para obtener las horas restringidas
                return@withContext DatabaseHelper.getUserRestrictedHours(userId)
            } catch (e: Exception) {
                Log.e("UserTimeZoneHelper", "Error al obtener las horas restringidas: ${e.message}", e)
                null
            }
        }
    }

    // Verifica si la hora actual está dentro del horario permitido
    suspend fun isWithinRestrictedHours(context: Context, userId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val restrictedHours = getUserRestrictedHours(userId) // Se llama a getUserRestrictedHours sin context

            if (restrictedHours != null) {
                val (startHour, endHour) = restrictedHours

                // Verificar si la hora actual está dentro del rango de horas del usuario
                return@withContext (currentHour > startHour || (currentHour == startHour && currentMinute >= 0)) &&
                        (currentHour < endHour || (currentHour == endHour && currentMinute < 60))
            } else {
                Log.e("UserTimeZoneHelper", "No se pudo obtener el horario para el usuario $userId.")
                return@withContext false
            }
        }
    }
}

