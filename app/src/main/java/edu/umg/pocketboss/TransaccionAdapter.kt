package edu.umg.pocketboss

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import modelo.Categoria
import modelo.Transaccion

class TransaccionAdapter(
    private var lista: List<Transaccion> = emptyList(),
    private var mapaCategorias: Map<Int, Categoria> = emptyMap(),
    private val onEliminar: ((Transaccion) -> Unit)? = null
) : RecyclerView.Adapter<TransaccionAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvSigno: TextView       = view.findViewById(R.id.tvSigno)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvDetalle: TextView     = view.findViewById(R.id.tvDetalle)
        val tvMonto: TextView       = view.findViewById(R.id.tvMonto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = lista[position]
        val cat = mapaCategorias[t.categoria]
        val esGasto = cat?.esGasto ?: true
        val nombreCat = cat?.nombre ?: "Sin categoría"

        val verde = Color.parseColor("#2E7D32")
        val rojo  = Color.parseColor("#C62828")
        val color = if (esGasto) rojo else verde

        holder.tvSigno.text = if (esGasto) "−" else "+"
        (holder.tvSigno.background as? GradientDrawable)?.setColor(color)

        holder.tvDescripcion.text = t.descripcion
        holder.tvDetalle.text = "$nombreCat · ${t.fecha}"

        val signoStr = if (esGasto) "- " else "+ "
        holder.tvMonto.text = "$signoStr Q${t.monto}"
        holder.tvMonto.setTextColor(color)
    }

    override fun getItemCount() = lista.size

    fun actualizarDatos(nueva: List<Transaccion>, categorias: Map<Int, Categoria>) {
        lista = nueva
        mapaCategorias = categorias
        notifyDataSetChanged()
    }

    /** Obtiene la transacción en una posición (útil para el swipe) */
    fun getItemAt(position: Int): Transaccion? =
        lista.getOrNull(position)

    /** Quita una transacción de la lista local y notifica el cambio */
    fun eliminarDeLista(transaccion: Transaccion) {
        val idx = lista.indexOfFirst { it.id == transaccion.id }
        if (idx >= 0) {
            lista = lista.toMutableList().also { it.removeAt(idx) }
            notifyItemRemoved(idx)
        }
    }
}