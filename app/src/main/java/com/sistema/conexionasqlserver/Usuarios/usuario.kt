package com.sistema.conexionasqlserver.Usuarios

import java.io.Serializable

data class Usuario(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val contraseña: String,
    val email: String,
    val activo: Int,
    val fechaCrea: String,
    val rol: Int,
    val movimiento: Int,
    val tiempoEspera: Int,
    val co: String,
    val smartphone: String,
    val horaInicio: Int,  // Nuevo campo
    val horaFinal: Int,
) : Serializable // Asegúrate de implementar Serializable o Parcelable
