package edu.umg.pocketboss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ResumenFragment : Fragment() {

    private lateinit var tvDetalle: TextView
    private lateinit var rvTodas: RecyclerView
    private lateinit var adapter: TransaccionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.activity_resumen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDetalle = view.findViewById(R.id.tvDetalleResumen)
        rvTodas   = view.findViewById(R.id.rvTodasTransacciones)

        adapter = TransaccionAdapter()
        rvTodas.layoutManager = LinearLayoutManager(requireContext())
        rvTodas.adapter = adapter

        cargar()
    }

    override fun onResume() {
        super.onResume()
        cargar()
    }

    private fun cargar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val transacciones = AppContainer.getControlGastos(ctx).listar()
            val categorias    = AppContainer.getControlCategorias(ctx).listarCategorias()
            val mapaCat       = categorias.associateBy { it.id }

            // --- Lista completa (misma que Inicio, pero sin take(10)) ---
            adapter.actualizarDatos(transacciones, mapaCat)

            // --- Texto de totales y agrupados ---
            if (transacciones.isEmpty()) {
                tvDetalle.text = "Aún no hay transacciones registradas."
                return@launch
            }

            var totalIngresos = 0
            var totalGastos   = 0
            transacciones.forEach { t ->
                val esGasto = mapaCat[t.categoria]?.esGasto ?: true
                if (esGasto) totalGastos += t.monto else totalIngresos += t.monto
            }
            val balance = totalIngresos - totalGastos

            val sb = StringBuilder()
            sb.appendLine("Ingresos: Q$totalIngresos  ·  Gastos: Q$totalGastos")
            sb.appendLine("Balance: Q$balance")
            sb.appendLine()

            sb.appendLine("Ingresos por categoría:")
            transacciones
                .filter { mapaCat[it.categoria]?.esGasto == false }
                .groupBy { it.categoria }
                .forEach { (catId, lista) ->
                    val nombre = mapaCat[catId]?.nombre ?: "Sin categoría"
                    sb.appendLine("• $nombre: Q${lista.sumOf { it.monto }} (${lista.size})")
                }

            sb.appendLine()
            sb.appendLine("Gastos por categoría:")
            transacciones
                .filter { mapaCat[it.categoria]?.esGasto != false }
                .groupBy { it.categoria }
                .forEach { (catId, lista) ->
                    val nombre = mapaCat[catId]?.nombre ?: "Sin categoría"
                    sb.appendLine("• $nombre: Q${lista.sumOf { it.monto }} (${lista.size})")
                }

            tvDetalle.text = sb.toString()
        }
    }
}