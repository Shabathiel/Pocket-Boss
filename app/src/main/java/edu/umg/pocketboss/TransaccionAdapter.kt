package edu.umg.pocketboss

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import modelo.Categoria
import modelo.Transaccion

class TransaccionAdapter(
    private var lista: List<Transaccion> = emptyList(),
    private var mapaCategorias: Map<Int, Categoria> = emptyMap()
) : RecyclerView.Adapter<TransaccionAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tv1: TextView = view.findViewById(android.R.id.text1)
        val tv2: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = lista[position]
        val cat = mapaCategorias[t.categoria]
        val esGasto = cat?.esGasto ?: true
        val nombreCat = cat?.nombre ?: "Sin categoría"

        holder.tv1.text = t.descripcion

        val signo = if (esGasto) "-" else "+"
        holder.tv2.text = "$signo Q${t.monto} · $nombreCat · ${t.fecha}"

        val color = if (esGasto) Color.parseColor("#C62828") else Color.parseColor("#2E7D32")
        holder.tv2.setTextColor(color)
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nueva: List<Transaccion>, categorias: Map<Int, Categoria>) {
        lista = nueva
        mapaCategorias = categorias
        notifyDataSetChanged()
    }
}