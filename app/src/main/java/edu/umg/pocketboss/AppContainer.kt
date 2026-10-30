package edu.umg.pocketboss

import android.content.Context
import edu.umg.pocketboss.persistencia.CategoriaDao
import edu.umg.pocketboss.persistencia.GastosDao
import servicio.ControlCategorias
import servicio.ControlGastos

object AppContainer {
    @Volatile private var gastos: ControlGastos? = null
    @Volatile private var categorias: ControlCategorias? = null

    fun getControlGastos(context: Context): ControlGastos =
        gastos ?: synchronized(this) {
            gastos ?: ControlGastos(GastosDao(context.applicationContext)).also { gastos = it }
        }

    fun getControlCategorias(context: Context): ControlCategorias =
        categorias ?: synchronized(this) {
            categorias ?: ControlCategorias(CategoriaDao(context.applicationContext)).also { categorias = it }
        }
}