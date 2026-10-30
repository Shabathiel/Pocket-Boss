package edu.umg.pocketboss

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        view.findViewById<FloatingActionButton>(R.id.btnAgregarTransaccionFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarTransaccionActivity::class.java))
        }
        view.findViewById<FloatingActionButton>(R.id.btnAgregarCategoriaFab).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarCategoriaActivity::class.java))
        }

        verificarNombreUsuario()
        cargar()
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
            tvSaldo.text    = "Balance: Q$balance"
            tvSaldo.setTextColor(
                if (balance < 0) 0xFFC62828.toInt() else 0xFF000000.toInt()
            )

            adapter.actualizarDatos(transacciones.take(10), mapaCat)
        }
    }
}