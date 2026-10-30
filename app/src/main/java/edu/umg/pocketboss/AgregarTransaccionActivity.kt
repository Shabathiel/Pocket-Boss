package edu.umg.pocketboss

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import modelo.Categoria

class AgregarTransaccionActivity : AppCompatActivity() {

    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var spinnerCategorias: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbGasto: RadioButton

    private var categoriasTodas: List<Categoria> = emptyList()
    private var categoriasFiltradas: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_transaccion)

        etDescripcion     = findViewById(R.id.etDescripcion)
        etMonto           = findViewById(R.id.etMonto)
        spinnerCategorias = findViewById(R.id.spinnerCategorias)
        btnGuardar        = findViewById(R.id.btnGuardarTransaccion)
        rgTipo            = findViewById(R.id.rgTipoTransaccion)
        rbGasto           = findViewById(R.id.rbTipoGasto)

        rgTipo.setOnCheckedChangeListener { _, _ -> filtrarCategorias() }

        cargarCategorias()

        btnGuardar.setOnClickListener { guardar() }
    }

    private fun cargarCategorias() {
        lifecycleScope.launch {
            categoriasTodas = AppContainer.getControlCategorias(this@AgregarTransaccionActivity)
                .listarCategorias()
            filtrarCategorias()
        }
    }

    private fun filtrarCategorias() {
        val esGasto = rbGasto.isChecked
        categoriasFiltradas = categoriasTodas.filter { it.esGasto == esGasto }

        val nombres = categoriasFiltradas.map { it.nombre }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategorias.adapter = adapter
    }

    private fun guardar() {
        val descripcion = etDescripcion.text.toString().trim()
        val montoStr    = etMonto.text.toString().trim()
        val monto       = montoStr.toIntOrNull() ?: 0

        if (categoriasFiltradas.isEmpty()) {
            Toast.makeText(this, "No hay categorías de ese tipo. Crea una primero.", Toast.LENGTH_LONG).show()
            return
        }

        val categoriaNombre = categoriasFiltradas[spinnerCategorias.selectedItemPosition].nombre
        val fecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        lifecycleScope.launch {
            try {
                AppContainer.getControlGastos(this@AgregarTransaccionActivity)
                    .agregar(
                        descripcion = descripcion,
                        monto = monto,
                        categoria = categoriaNombre,
                        fecha = fecha
                    )
                Toast.makeText(this@AgregarTransaccionActivity, "Transacción guardada", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this@AgregarTransaccionActivity, "Validación: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@AgregarTransaccionActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}