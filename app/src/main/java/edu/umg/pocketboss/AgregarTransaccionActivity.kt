package edu.umg.pocketboss

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.pocketboss.persistencia.CategoriaDao
import edu.umg.pocketboss.persistencia.GastosDao
import kotlinx.coroutines.launch
import modelo.Categoria
import modelo.Transaccion
import java.text.SimpleDateFormat
import java.util.*

class AgregarTransaccionActivity : AppCompatActivity() {

    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var spinnerCategorias: Spinner
    private lateinit var btnGuardar: Button

    private val gastosDao by lazy { GastosDao(this) }
    private val categoriaDao by lazy { CategoriaDao(this) }

    private var categoriasList: List<Categoria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_transaccion)

        etDescripcion = findViewById(R.id.etDescripcion)
        etMonto = findViewById(R.id.etMonto)
        spinnerCategorias = findViewById(R.id.spinnerCategorias)
        btnGuardar = findViewById(R.id.btnGuardarTransaccion)

        cargarCategoriasEnSpinner()

        btnGuardar.setOnClickListener { guardarTransaccion() }
    }

    private fun cargarCategoriasEnSpinner() {
        lifecycleScope.launch {
            categoriasList = categoriaDao.listarCategorias()
            val nombres = categoriasList.map { it.nombre }
            val adapter = ArrayAdapter(
                this@AgregarTransaccionActivity,
                android.R.layout.simple_spinner_item,
                nombres
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategorias.adapter = adapter
        }
    }

    private fun guardarTransaccion() {
        val descripcionStr = etDescripcion.text.toString().trim()
        val montoStr = etMonto.text.toString().trim()

        if (descripcionStr.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val montoInt = montoStr.toIntOrNull() ?: 0
        if (montoInt <= 0) {
            Toast.makeText(this, "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoriasList.isEmpty()) {
            Toast.makeText(this, "Crea una categoría primero", Toast.LENGTH_SHORT).show()
            return
        }

        val categoriaSeleccionada = categoriasList[spinnerCategorias.selectedItemPosition]
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch {
            try {
                val nuevaTransaccion = Transaccion(
                    descripcion = descripcionStr,
                    monto = montoInt,
                    categoria = categoriaSeleccionada.id,
                    fecha = fechaActual
                )
                gastosDao.agregar(nuevaTransaccion)
                Toast.makeText(this@AgregarTransaccionActivity, "Guardado", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AgregarTransaccionActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}