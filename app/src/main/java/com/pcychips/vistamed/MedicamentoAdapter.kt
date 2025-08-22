package com.pcychips.vistamed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MedicamentoAdapter(private val onItemClicked: (Medicamento) -> Unit) :
    ListAdapter<Medicamento, MedicamentoAdapter.MedicamentoViewHolder>(MedicamentosComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.nombre, current.dosis)
        // Asignamos el listener al item entero
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
    }

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombre)
        private val dosisTextView: TextView = itemView.findViewById(R.id.textViewDosis)

        fun bind(nombre: String?, dosis: String?) {
            nombreTextView.text = nombre
            dosisTextView.text = dosis ?: "Dosis no especificada"
        }
    }

    class MedicamentosComparator : DiffUtil.ItemCallback<Medicamento>() {
        override fun areItemsTheSame(oldItem: Medicamento, newItem: Medicamento): Boolean {
            return oldItem.id_medicamento == newItem.id_medicamento
        }

        override fun areContentsTheSame(oldItem: Medicamento, newItem: Medicamento): Boolean {
            return oldItem == newItem
        }
    }
}