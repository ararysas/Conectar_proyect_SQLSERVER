package com.sistema.conexionasqlserver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private val userTrackers = mutableMapOf<Int, UserTracker>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Registrar el BroadcastReceiver para apagado
        registerReceiver(ShutdownReceiver(), IntentFilter(Intent.ACTION_SHUTDOWN))

        // Inicializar el LocationManagerHelper
        LocationManagerHelper.initialize(this)

        // Verificar si el servicio fue reiniciado y recuperar el estado si es necesario
        checkAndRestoreUserTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getIntExtra("USER_ID", -1) ?: getUserIdFromPreferences()

        if (userId != -1) {
            if (!userTrackers.containsKey(userId)) {
                userTrackers[userId] = UserTracker(userId) { nota, codigo ->
                    saveLocation(userId, nota, codigo)
                }
                userTrackers[userId]?.startTracking(fusedLocationClient)
            }
        }

        startForegroundService()
        return START_STICKY
    }

    private fun getUserIdFromPreferences(): Int {
        return sharedPreferences.getInt("USER_ID", -1)
    }

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Ubicación en seguimiento")
            .setContentText("El servicio de ubicación está activo.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    private fun saveLocation(userId: Int, nota: String, codigo: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (UserTimeZoneHelper.isWithinRestrictedHours(applicationContext, userId)) {
                val userTracker = userTrackers[userId]
                userTracker?.lastLocation?.let { lastLocation ->
                    when {
                        !LocationManagerHelper.isInternetAvailable(this@LocationService) -> {
                            Log.d("LocationService", "No hay conexión a Internet. Guardando ubicación localmente con código 6.")
                            LocationManagerHelper.saveLocationLocally(this@LocationService, userId, lastLocation, nota)
                        }
                        !LocationManagerHelper.isGPSOn(this@LocationService) -> {
                            Log.d("LocationService", "GPS no está habilitado. Guardando ubicación localmente con código 5.")
                            LocationManagerHelper.saveLocationLocally(this@LocationService, userId, lastLocation, nota)
                        }
                        else -> {
                            DatabaseHelper.saveLocation(userId, lastLocation.latitude, lastLocation.longitude, nota, codigo)
                            Log.d("LocationService", "Ubicación guardada exitosamente para el usuario $userId: $nota con código $codigo.")
                        }
                    }
                } ?: run {
                    Log.e("LocationService", "La última ubicación es nula para el usuario $userId.")
                }
            } else {
                Log.d("LocationService", "No se guardará la ubicación: estamos fuera del rango restringido.")
            }
        }
    }

    private fun checkAndRestoreUserTrackingState() {
        val userId = getUserIdFromPreferences()
        val lastLocation = getLastSavedLocation(userId)
        if (lastLocation != null) {
            Log.d("LocationService", "Restaurando ubicación del usuario $userId desde SharedPreferences: $lastLocation")
            userTrackers[userId]?.lastLocation = lastLocation
            userTrackers[userId]?.startTracking(fusedLocationClient)
        }
    }

    private fun getLastSavedLocation(userId: Int): Location? {
        val latitude = sharedPreferences.getFloat("LAST_LATITUDE_$userId", Float.NaN)
        val longitude = sharedPreferences.getFloat("LAST_LONGITUDE_$userId", Float.NaN)

        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Location("").apply {
                this.latitude = latitude.toDouble()
                this.longitude = longitude.toDouble()
            }
        } else {
            null
        }
    }

    private fun checkUserCredentials() {
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)

        if (username != null && password != null) {
            Log.d("LocationService", "Usuario encontrado: $username")
        } else {
            Log.d("LocationService", "No hay credenciales guardadas.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userTrackers.values.forEach { it.stopTracking() }
    }

    inner class ShutdownReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SHUTDOWN) {
                val userId = getUserIdFromPreferences()
                val lastLocation = userTrackers[userId]?.lastLocation
                if (lastLocation != null) {
                    Log.d("LocationService", "Dispositivo apagándose. Última ubicación: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    saveLocation(userId, "Dispositivo apagado o Reiniciado", 3)
                    saveLocationInPreferences(userId, lastLocation)  // Guardar en SharedPreferences
                }
            }
        }
    }

    private fun saveLocationInPreferences(userId: Int, location: Location) {
        val editor = sharedPreferences.edit()
        editor.putFloat("LAST_LATITUDE_$userId", location.latitude.toFloat())
        editor.putFloat("LAST_LONGITUDE_$userId", location.longitude.toFloat())
        editor.apply()
    }

    inner class UserTracker(private val userId: Int, private val onSaveLocation: (String, Int) -> Unit) {
        var lastLocation: Location? = null
        private var lastSavedTime: Long = 0
        private val handler = android.os.Handler(Looper.getMainLooper())
        private val interval = 60 * 1000L // 1 minuto

        fun startTracking(fusedLocationClient: FusedLocationProviderClient) {
            startLocationUpdates(fusedLocationClient)

            // Verificación cada 1 minuto
            handler.postDelayed(object : Runnable {
                override fun run() {
                    checkLocationAndSave()
                    handler.postDelayed(this, interval) // Reprogramar
                }
            }, interval)
        }

        private fun checkLocationAndSave() {
            CoroutineScope(Dispatchers.IO).launch {
                val currentLocation = lastLocation
                val internetAvailable = LocationManagerHelper.isInternetAvailable(applicationContext)
                val gpsOn = LocationManagerHelper.isGPSOn(applicationContext)

                if (currentLocation != null) {
                    when {
                        !internetAvailable -> {
                            LocationManagerHelper.saveLocationLocally(applicationContext, userId, currentLocation, "Sin Internet")
                        }
                        !gpsOn -> {
                            LocationManagerHelper.saveLocationLocally(applicationContext, userId, currentLocation, "GPS Apagado")
                        }
                        else -> {
                            val distance = lastLocation?.distanceTo(currentLocation) ?: 0f
                            val currentTime = System.currentTimeMillis()

                            if (distance > 100) {
                                // Se guarda la ubicación si el usuario se movió más de 100 metros
                                onSaveLocation("Usuario en movimiento", 2)
                                lastSavedTime = currentTime
                            } else if (currentTime - lastSavedTime >= interval) {
                                // Si el usuario está en el mismo lugar, pero ha pasado un minuto, se guarda
                                onSaveLocation("Usuario mismo lugar", 1)
                                lastSavedTime = currentTime
                            }
                        }
                    }
                } else {
                    Log.e("UserTracker", "No hay ubicación disponible para guardar.")
                }
            }
        }

        private fun startLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10 * 60 * 1000L
            ).apply {
                setMinUpdateIntervalMillis(5000L)
            }.build()

            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { newLocation ->
                        handleNewLocation(newLocation)
                    }
                }
            }, Looper.getMainLooper())
        }

        private fun handleNewLocation(newLocation: Location) {
            val previousLocation = lastLocation
            val currentTime = System.currentTimeMillis()

            if (previousLocation == null || (currentTime - lastSavedTime >= interval)) {
                lastLocation = newLocation
                lastSavedTime = currentTime

                val distance = previousLocation?.distanceTo(newLocation) ?: 0f
                if (distance > 100) {
                    onSaveLocation("Usuario en movimiento", 2)
                } else {
                    onSaveLocation("Usuario mismo lugar", 1)
                }
            } else {
                Log.d("UserTracker", "Ubicación no guardada: todavía no ha pasado un minuto completo.")
            }
        }

        fun stopTracking() {
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
