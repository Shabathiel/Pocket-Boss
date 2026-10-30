package edu.umg.pocketboss

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView
    private lateinit var tvSaldo: TextView
    private lateinit var adapter: TransaccionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_inicio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv          = view.findViewById(R.id.rvUltimasTransacciones)
        tvIngresos  = view.findViewById(R.id.tvIngresos)
        tvGastos    = view.findViewById(R.id.tvGastos)
        tvSaldo     = view.findViewById(R.id.tvSaldo)

        adapter = TransaccionAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.btnAgregarTransaccionFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarTransaccionActivity::class.java))
        }
        view.findViewById<FloatingActionButton>(R.id.btnAgregarCategoriaFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarCategoriaActivity::class.java))
        }

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

            // Clasificación en memoria
            var totalIngresos = 0
            var totalGastos   = 0
            transacciones.forEach { t ->
                val esGasto = mapaCat[t.categoria]?.esGasto ?: true
                if (esGasto) totalGastos += t.monto else totalIngresos += t.monto
            }
            val balance = totalIngresos - totalGastos  // puede ser negativo

            tvIngresos.text = "Ingresos: Q$totalIngresos"
            tvGastos.text   = "Gastos: Q$totalGastos"
            tvSaldo.text    = "Balance: Q$balance"

            adapter.actualizarDatos(transacciones.take(10), mapaCat)
        }
    }
}