package com.sistema.conexionasqlserver.Usuarios

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sistema.conexionasqlserver.DetalleUsuarioActivity
import com.sistema.conexionasqlserver.R

// Adaptador para mostrar una lista de usuarios en un RecyclerView
class UsuarioAdapter(private val usuarios: List<Usuario>) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    // ViewHolder para cada elemento de la lista
    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreCompletoTextView: TextView = itemView.findViewById(R.id.nombreCompletoTextView)

        // Inicialización del click listener para cada elemento
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val usuario = usuarios[position]

                    // Lanzar la actividad de detalles del usuario
                    val intent = Intent(itemView.context, DetalleUsuarioActivity::class.java)
                    intent.putExtra("usuario", usuario) // Asegúrate de que Usuario implemente Serializable o Parcelable
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    // Inflar el diseño del elemento de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(itemView)
    }

    // Vincular datos al ViewHolder
    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val currentUser = usuarios[position]
        holder.nombreCompletoTextView.text = "${currentUser.nombre} ${currentUser.apellido}" // Concatenar nombre y apellido
    }

    // Devolver el número total de elementos
    override fun getItemCount() = usuarios.size
}

// Este archivo es el adaptador para el RecyclerView. Conecta los datos de los usuarios
// con la vista de la lista, asegurando que cada usuario se muestre correctamente.
