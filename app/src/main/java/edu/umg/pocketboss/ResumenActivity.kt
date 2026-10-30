package edu.umg.pocketboss

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class ResumenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumen)

        // Lista de todas las transacciones
        val rv = findViewById<RecyclerView>(R.id.rvTodasTransacciones)
        val adapter = TransaccionAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        cargarResumen(adapter)
    }

    private fun cargarResumen(adapter: TransaccionAdapter) {
        lifecycleScope.launch {
            val transacciones = AppContainer.getControlGastos(this@ResumenActivity).listar()
            val categorias    = AppContainer.getControlCategorias(this@ResumenActivity).listarCategorias()
            val mapaCat       = categorias.associateBy { it.id }

            // ===== Totales =====
            var totalIngresos = 0
            var totalGastos   = 0
            // categoría nombre -> monto, separado por ingreso/gasto
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

            // ===== Textos de la parte superior =====
            findViewById<TextView>(R.id.tvTotalIngresos).text = "Q$totalIngresos"
            findViewById<TextView>(R.id.tvTotalGastos).text   = "Q$totalGastos"
            findViewById<TextView>(R.id.tvBalanceResumen).text = "Q$balance"

            // Color del balance según signo
            val tvBalance = findViewById<TextView>(R.id.tvBalanceResumen)
            tvBalance.setTextColor(
                if (balance < 0) ContextCompat.getColor(this@ResumenActivity, R.color.gasto)
                else ContextCompat.getColor(this@ResumenActivity, R.color.ingreso)
            )

            // ===== Listas por categoría =====
            pintarCategorias(
                contenedor = findViewById(R.id.llGastosPorCategoria),
                vacio      = findViewById(R.id.tvSinGastos),
                datos      = gastosPorCategoria,
                total      = totalGastos,
                colorBarra = R.color.gasto
            )
            pintarCategorias(
                contenedor = findViewById(R.id.llIngresosPorCategoria),
                vacio      = findViewById(R.id.tvSinIngresos),
                datos      = ingresosPorCategoria,
                total      = totalIngresos,
                colorBarra = R.color.ingreso
            )

            // ===== Lista completa de transacciones =====
            adapter.actualizarDatos(transacciones, mapaCat)
        }
    }

    /**
     * Rellena el contenedor con una fila por categoría, ordenadas de mayor a menor monto.
     * Cada fila muestra nombre, monto, y una barra de progreso proporcional.
     */
    private fun pintarCategorias(
        contenedor: LinearLayout,
        vacio: TextView,
        datos: Map<String, Int>,
        total: Int,
        colorBarra: Int
    ) {
        contenedor.removeAllViews()

        if (datos.isEmpty() || total <= 0) {
            vacio.visibility = android.view.View.VISIBLE
            return
        }
        vacio.visibility = android.view.View.GONE

        // Ordenar de mayor a menor monto
        val ordenado = datos.entries.sortedByDescending { it.value }

        val inflater = LayoutInflater.from(this)
        val color = ContextCompat.getColor(this, colorBarra)

        ordenado.forEach { (nombre, monto) ->
            val fila = inflater.inflate(R.layout.item_categoria_resumen, contenedor, false)

            fila.findViewById<TextView>(R.id.tvNombreCategoria).text = nombre
            fila.findViewById<TextView>(R.id.tvMontoCategoria).text  = "Q$monto"

            var progreso = fila.findViewById<LinearProgressIndicator>(R.id.progressCategoria)
            val porcentaje = ((monto.toFloat() / total.toFloat()) * 100f).toInt()
            progreso.progress = porcentaje
            progreso.setIndicatorColor(color)

            // También coloreamos el monto con el color de la categoría (opcional)
            fila.findViewById<TextView>(R.id.tvMontoCategoria).setTextColor(color)

            contenedor.addView(fila)
        }
    }
}