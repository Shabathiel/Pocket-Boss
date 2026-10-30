package edu.umg.pocketboss

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.pocketboss.persistencia.CategoriaDao
import kotlinx.coroutines.launch

class AgregarCategoriaActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var rbGasto: RadioButton
    private lateinit var rbIngreso: RadioButton
    private lateinit var btnGuardar: Button

    private val categoriaDao by lazy { CategoriaDao(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_categoria)

        etNombre = findViewById(R.id.etNombreCategoria)
        rbGasto = findViewById(R.id.rbGasto)
        rbIngreso = findViewById(R.id.rbIngreso)
        btnGuardar = findViewById(R.id.btnGuardarCategoria)

        btnGuardar.setOnClickListener { guardarCategoria() }
    }

    private fun guardarCategoria() {
        val nombre = etNombre.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val esGasto = rbGasto.isChecked

        lifecycleScope.launch {
            try {
                categoriaDao.crearCategoria(nombre, esGasto)
                Toast.makeText(this@AgregarCategoriaActivity, "Categoría creada", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AgregarCategoriaActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}