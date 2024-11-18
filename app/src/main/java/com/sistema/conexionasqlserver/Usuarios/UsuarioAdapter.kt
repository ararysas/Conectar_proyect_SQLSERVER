package com.sistema.conexionasqlserver.Usuarios

import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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

    // Vincular datos al ViewHolder con color específico para 'co'
    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val currentUser = usuarios[position]

        // Crear el texto concatenado de co, nombre y apellido
        val completoTexto = "${currentUser.co} ${currentUser.nombre} ${currentUser.apellido}"

        // Crear un SpannableString para personalizar los colores
        val spannable = SpannableString(completoTexto)


        val coLength = currentUser.co.toString().length
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#4CAF50")),
            0,
            coLength,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Asignar el SpannableString al TextView
        holder.nombreCompletoTextView.text = spannable
    }

    // Devolver el número total de elementos
    override fun getItemCount() = usuarios.size
}
