package edu.umg.pocketboss

import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InicioFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var tvNombreUsuario: TextView
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

        rv               = view.findViewById(R.id.rvUltimasTransacciones)
        tvNombreUsuario  = view.findViewById(R.id.tvNombreUsuario)
        tvIngresos       = view.findViewById(R.id.tvIngresos)
        tvGastos         = view.findViewById(R.id.tvGastos)
        tvSaldo          = view.findViewById(R.id.tvSaldo)

        adapter = TransaccionAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // 👇 NUEVO: swipe para eliminar
        crearSwipeEliminar(rv) { position ->
            val t = adapter.getItemAt(position) ?: return@crearSwipeEliminar
            confirmarEliminar(t)
        }

        view.findViewById<FloatingActionButton>(R.id.btnAgregarTransaccionFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarTransaccionActivity::class.java))
        }
        view.findViewById<FloatingActionButton>(R.id.btnAgregarCategoriaFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarCategoriaActivity::class.java))
        }

        verificarNombreUsuario()
        cargar()
    }

    // 👇 NUEVO método
    private fun confirmarEliminar(t: modelo.Transaccion) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar transacción")
            .setMessage("¿Eliminar \"${t.descripcion}\" por Q${t.monto}?")
            .setNegativeButton("Cancelar") { _, _ ->
                // Refresca para que la fila vuelva a su lugar
                adapter.notifyDataSetChanged()
            }
            .setPositiveButton("Eliminar") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        AppContainer.getControlGastos(requireContext())
                            .eliminarPorId(t.id)
                        adapter.eliminarDeLista(t)
                        cargar()  // recalcula totales y refresca
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al eliminar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        actualizarSaludo()
        cargar()
    }

    // ---- NOMBRE DE USUARIO ----

    private fun verificarNombreUsuario() {
        val nombre = PreferenciasUsuario.obtenerNombre(requireContext())
        if (nombre.isNullOrBlank()) {
            pedirNombre()
        } else {
            actualizarSaludo()
        }
    }

    private fun actualizarSaludo() {
        val nombre = PreferenciasUsuario.obtenerNombre(requireContext()) ?: "Usuario"
        tvNombreUsuario.text = "Hola, $nombre"
    }

    private fun pedirNombre() {
        val input = EditText(requireContext()).apply {
            hint = "Tu nombre"
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("¡Bienvenido!")
            .setMessage("¿Cómo te llamas?")
            .setView(input)
            .setCancelable(false)  // obligatorio ponerlo la primera vez
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "Escribe un nombre", Toast.LENGTH_SHORT).show()
                    pedirNombre()  // vuelve a preguntar
                } else {
                    PreferenciasUsuario.guardarNombre(requireContext(), nombre)
                    actualizarSaludo()
                }
            }
            .show()
    }

    // ---- DATOS ----

    private fun cargar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val transacciones = AppContainer.getControlGastos(ctx).listar()
            val categorias    = AppContainer.getControlCategorias(ctx).listarCategorias()
            val mapaCat       = categorias.associateBy { it.id }

            var totalIngresos = 0
            var totalGastos   = 0
            transacciones.forEach { t ->
                val esGasto = mapaCat[t.categoria]?.esGasto ?: true
                if (esGasto) totalGastos += t.monto else totalIngresos += t.monto
            }
            val balance = totalIngresos - totalGastos

            tvIngresos.text = "Ingresos: Q$totalIngresos"
            tvGastos.text   = "Gastos: Q$totalGastos"
            tvSaldo.text = "Q$balance"
            val colorSaldo = if (balance < 0) {
                ContextCompat.getColor(requireContext(), R.color.gasto)
            } else {
                // Color del texto sobre el contenedor primario (se adapta a claro/oscuro)
                MaterialColors.getColor(
                    tvSaldo,
                    com.google.android.material.R.attr.colorOnPrimaryContainer
                )
            }
            tvSaldo.setTextColor(colorSaldo)

            adapter.actualizarDatos(transacciones.take(10), mapaCat)
        }
    }
}