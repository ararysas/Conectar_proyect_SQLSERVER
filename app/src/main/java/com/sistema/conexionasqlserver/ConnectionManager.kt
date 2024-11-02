package com.sistema.conexionasqlserver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ConnectionManager(private val context: Context) {

    private companion object {
        const val REQUEST_LOCATION_PERMISSION = 100
    }

    // Solicita permisos de ubicación
    fun requestLocationPermission(onPermissionGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no se tienen
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        } else {
            // Si ya se tienen permisos, ejecutar la acción
            Log.d("ConnectionManager", "Permisos de ubicación ya concedidos.")
            onPermissionGranted()
        }
    }

    // Verifica si el GPS está habilitado
    fun isGPSEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("ConnectionManager", "GPS habilitado: $isEnabled")
        return isEnabled
    }

    // Verifica si hay conexión a Internet
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork?.let {
                connectivityManager.getNetworkCapabilities(it)
            }
        } else {
            // Para versiones anteriores a Android Marshmallow
            null
        }

        val isAvailable = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.d("ConnectionManager", "Internet disponible: $isAvailable")
        return isAvailable
    }

    // Método para verificar el estado de conexión
    fun checkConnectionStatus(): ConnectionStatus {
        return when {
            isGPSEnabled() && isInternetAvailable() -> ConnectionStatus.BOTH_ENABLED
            isGPSEnabled() -> ConnectionStatus.GPS_ONLY
            isInternetAvailable() -> ConnectionStatus.INTERNET_ONLY
            else -> ConnectionStatus.NONE
        }
    }

    // Manejo de resultados de permisos
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray, onPermissionGranted: () -> Unit) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Log.d("ConnectionManager", "Permiso de ubicación concedido.")
                onPermissionGranted()
            } else {
                // Permiso denegado
                Toast.makeText(context, "Permisos de ubicación denegados. No se puede acceder a la ubicación.", Toast.LENGTH_SHORT).show()
                Log.d("ConnectionManager", "Permiso de ubicación denegado.")
            }
        }
    }

    // Enumeración para el estado de conexión
    enum class ConnectionStatus {
        BOTH_ENABLED,
        GPS_ONLY,
        INTERNET_ONLY,
        NONE
    }
}
