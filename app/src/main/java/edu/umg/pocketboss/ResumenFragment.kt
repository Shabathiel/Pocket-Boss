package edu.umg.pocketboss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class ResumenFragment : Fragment() {

    private lateinit var adapter: TransaccionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_resumen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvTodasTransacciones)
        adapter = TransaccionAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 👇 NUEVO
        crearSwipeEliminar(rv) { position ->
            val t = adapter.getItemAt(position) ?: return@crearSwipeEliminar
            confirmarEliminar(t, view)
        }

        cargarResumen(view)
    }

    // 👇 NUEVO
    private fun confirmarEliminar(t: modelo.Transaccion, rootView: View) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar transacción")
            .setMessage("¿Eliminar \"${t.descripcion}\" por Q${t.monto}?")
            .setNegativeButton("Cancelar") { _, _ -> adapter.notifyDataSetChanged() }
            .setPositiveButton("Eliminar") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        AppContainer.getControlGastos(requireContext()).eliminarPorId(t.id)
                        adapter.eliminarDeLista(t)
                        cargarResumen(rootView)  // recalcula todos los totales y listas por categoría
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Error al eliminar: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun cargarResumen(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val transacciones = AppContainer.getControlGastos(ctx).listar()
            val categorias    = AppContainer.getControlCategorias(ctx).listarCategorias()
            val mapaCat       = categorias.associateBy { it.id }

            var totalIngresos = 0
            var totalGastos   = 0
            val gastosPorCategoria   = mutableMapOf<String, Int>()
            val ingresosPorCategoria = mutableMapOf<String, Int>()

            transacciones.forEach { t ->
                val cat   = mapaCat[t.categoria]
                val nombre = cat?.nombre ?: "Sin categoría"
                val esGasto = cat?.esGasto ?: true

                if (esGasto) {
                    totalGastos += t.monto
                    gastosPorCategoria[nombre] = (gastosPorCategoria[nombre] ?: 0) + t.monto
                } else {
                    totalIngresos += t.monto
                    ingresosPorCategoria[nombre] = (ingresosPorCategoria[nombre] ?: 0) + t.monto
                }
            }
            val balance = totalIngresos - totalGastos

            view.findViewById<TextView>(R.id.tvTotalIngresos).text = "Q$totalIngresos"
            view.findViewById<TextView>(R.id.tvTotalGastos).text   = "Q$totalGastos"
            val tvBalance = view.findViewById<TextView>(R.id.tvBalanceResumen)
            tvBalance.text = "Q$balance"

            tvBalance.setTextColor(
                if (balance < 0) ContextCompat.getColor(ctx, R.color.gasto)
                else ContextCompat.getColor(ctx, R.color.ingreso)
            )

            pintarCategorias(
                contenedor = view.findViewById(R.id.llGastosPorCategoria),
                vacio      = view.findViewById(R.id.tvSinGastos),
                datos      = gastosPorCategoria,
                total      = totalGastos,
                colorBarra = R.color.gasto
            )
            pintarCategorias(
                contenedor = view.findViewById(R.id.llIngresosPorCategoria),
                vacio      = view.findViewById(R.id.tvSinIngresos),
                datos      = ingresosPorCategoria,
                total      = totalIngresos,
                colorBarra = R.color.ingreso
            )

            adapter.actualizarDatos(transacciones, mapaCat)
        }
    }

    private fun pintarCategorias(
        contenedor: LinearLayout,
        vacio: TextView,
        datos: Map<String, Int>,
        total: Int,
        colorBarra: Int
    ) {
        contenedor.removeAllViews()

        if (datos.isEmpty() || total <= 0) {
            vacio.visibility = View.VISIBLE
            return
        }
        vacio.visibility = View.GONE

        val ordenado = datos.entries.sortedByDescending { it.value }
        val inflater = LayoutInflater.from(requireContext())
        val color = ContextCompat.getColor(requireContext(), colorBarra)

        ordenado.forEach { (nombre, monto) ->
            val fila = inflater.inflate(R.layout.item_categoria_resumen, contenedor, false)

            fila.findViewById<TextView>(R.id.tvNombreCategoria).text = nombre
            fila.findViewById<TextView>(R.id.tvMontoCategoria).text  = "Q$monto"
            fila.findViewById<TextView>(R.id.tvMontoCategoria).setTextColor(color)

            val progreso = fila.findViewById<LinearProgressIndicator>(R.id.progressCategoria)
            val porcentaje = ((monto.toFloat() / total.toFloat()) * 100f).toInt()
            progreso.progress = porcentaje
            progreso.setIndicatorColor(color)

            contenedor.addView(fila)
        }
    }
}