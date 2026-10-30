package edu.umg.pocketboss

import android.content.Context
import androidx.lifecycle.lifecycleScope
import edu.umg.pocketboss.persistencia.CategoriaDao
import kotlinx.coroutines.launch

object CategoriasIniciales {

    private const val PREFS = "pocketboss_prefs"
    private const val KEY_SEED = "categorias_sembradas"

    // nombre a (esGasto)
    private val PREDEFINIDAS = listOf(
        // Gastos
        "Comida"        to true,
        "Transporte"    to true,
        "Salud"         to true,
        "Entretenimiento" to true,
        "Otros gastos"  to true,
        // Ingresos
        "Salario"       to false,
        "Ventas"        to false,
        "Regalos"       to false,
        "Otros ingresos" to false
    )

    /**
     * Siembra categorías la primera vez. Seguro llamarlo varias veces:
     * usa una bandera en SharedPreferences y también verifica que la tabla esté vacía.
     */
    fun sembrarSiHaceFalta(context: Context, onDone: (() -> Unit)? = null) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEED, false)) {
            onDone?.invoke()
            return
        }

        val app = context.applicationContext
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val dao = CategoriaDao(app)
                val existentes = dao.listarCategorias().map { it.nombre }.toSet()

                PREDEFINIDAS.forEach { (nombre, esGasto) ->
                    if (nombre !in existentes) {
                        try {
                            dao.crearCategoria(nombre, esGasto)
                        } catch (_: Exception) {
                            // UNIQUE constraint: ya existe, ignorar
                        }
                    }
                }

                prefs.edit().putBoolean(KEY_SEED, true).apply()
            } finally {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onDone?.invoke()
                }
            }
        }
    }
}