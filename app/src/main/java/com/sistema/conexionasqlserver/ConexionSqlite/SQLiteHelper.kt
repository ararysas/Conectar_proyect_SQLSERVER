package com.sistema.conexionasqlserver

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "trakim.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_TRAKIM = "t010_trakim"  // Nombre de la tabla

        // Definición de las columnas con prefijo f010_
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "f010_ID_User"
        private const val COLUMN_COORDINATES = "f010_Coordenadas"
        private const val COLUMN_DATE = "f010_Fecha"
        private const val COLUMN_NOTE = "f010_Nota"
        private const val COLUMN_CODE = "f010_codigo"
    }

    // Creación de la tabla t010_trakim
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_TRAKIM (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_COORDINATES TEXT,
                $COLUMN_DATE TEXT,
                $COLUMN_NOTE TEXT,
                $COLUMN_CODE INTEGER
            )
        """.trimIndent()
        db.execSQL(createTable)
        Log.d("SQLiteHelper", "Tabla $TABLE_TRAKIM creada exitosamente.")
    }

    // Actualización de la base de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRAKIM")
        Log.d("SQLiteHelper", "Base de datos actualizada de la versión $oldVersion a la versión $newVersion.")
        onCreate(db)
    }

    // Método para insertar una ubicación
    fun insertLocation(userId: Int, coordinates: String, date: String, note: String, code: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_COORDINATES, coordinates)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTE, note)
            put(COLUMN_CODE, code)
        }

        return try {
            val result = db.insert(TABLE_TRAKIM, null, values)
            if (result == -1L) {
                Log.e("SQLiteHelper", "Error al insertar la ubicación en la base de datos.")
                false
            } else {
                Log.d("SQLiteHelper", "Ubicación insertada exitosamente con ID: $result")
                true
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Excepción durante la inserción: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

    // Método para obtener ubicaciones pendientes
    suspend fun getPendingLocations(): List<LocationData> {
        val locationsList = mutableListOf<LocationData>()

        withContext(Dispatchers.IO) {
            val db = readableDatabase
            val query = "SELECT * FROM $TABLE_TRAKIM"
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val location = LocationData(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        coordinates = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COORDINATES)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)),
                        code = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CODE))
                    )
                    locationsList.add(location)
                }
            }
            Log.d("SQLiteHelper", "Recuperadas ${locationsList.size} ubicaciones pendientes.")
        }

        return locationsList
    }

    // Método para eliminar una ubicación
    suspend fun deleteLocation(location: LocationData): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(TABLE_TRAKIM, "$COLUMN_ID=?", arrayOf(location.id.toString()))
            if (result > 0) {
                Log.d("SQLiteHelper", "Ubicación eliminada exitosamente con ID: ${location.id}")
                true
            } else {
                Log.e("SQLiteHelper", "Error al eliminar la ubicación con ID: ${location.id}")
                false
            }
        } catch (e: Exception) {
            Log.e("SQLiteHelper", "Excepción al eliminar la ubicación: ${e.message}")
            false
        } finally {
            db.close()
        }
    }
}

// Clase de datos para representar una ubicación
data class LocationData(
    val id: Int,
    val userId: Int,
    val coordinates: String,
    val date: String,
    val note: String,
    val code: Int
)
