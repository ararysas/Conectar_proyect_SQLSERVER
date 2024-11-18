package com.sistema.conexionasqlserver

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.sistema.conexionasqlserver.Usuarios.Usuario

class UsuarioViewModel : ViewModel() {

    // LiveData para el usuario
    private val _usuarioLiveData = MutableLiveData<Usuario?>()
    val usuarioLiveData: LiveData<Usuario?> = _usuarioLiveData

    // LiveData para el estado de la actualización
    private val _updateStatusLiveData = MutableLiveData<Boolean>()
    val updateStatusLiveData: LiveData<Boolean> = _updateStatusLiveData

    // Método para actualizar el usuario
    fun actualizarUsuario(context: Context, usuarioActualizado: Usuario) {
        viewModelScope.launch {
            val success = UserDatabaseHelper.updateUsuario(context, usuarioActualizado)
            if (success) {
                // Actualiza los datos nuevamente desde la base de datos para que `usuarioLiveData` esté fresco
                obtenerUsuarioPorId(context, usuarioActualizado.id)
            }
            _updateStatusLiveData.value = success
        }
    }



    // Método para obtener el usuario por su ID
    fun obtenerUsuarioPorId(context: Context, userId: Int) {
        viewModelScope.launch {
            val usuario = UserDatabaseHelper.getUsuarioById(context, userId)
            _usuarioLiveData.value = usuario  // Establecer el usuario obtenido en el LiveData
        }
    }

    // Método para configurar el usuario inicialmente
    fun setUsuario(usuario: Usuario?) {
        _usuarioLiveData.value = usuario
    }
}
