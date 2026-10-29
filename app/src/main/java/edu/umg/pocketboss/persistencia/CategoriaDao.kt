package edu.umg.pocketboss.persistencia

import android.content.ContentValues
import android.content.Context
import datos.CategoriaInterface
import modelo.Categoria

class CategoriaDao(context: Context) : CategoriaInterface {

    private val dbHelper = FinanzasDatabase(context)

    override suspend fun crearCategoria(
        nombre: String,
        esGasto: Boolean
    ): Int {
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("esGasto", if (esGasto) 1 else 0)
        }

        return dbHelper.writableDatabase.insertOrThrow(
            "categorias",
            null,
            valores
        ).toInt()
    }

    override suspend fun listarCategorias(): List<Categoria> {
        val categorias = mutableListOf<Categoria>()

        val cursor = dbHelper.readableDatabase.rawQuery(
            "SELECT id, nombre, esGasto FROM categorias ORDER BY nombre",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                categorias.add(
                    Categoria(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        nombre = it.getString(
                            it.getColumnIndexOrThrow("nombre")
                        ),
                        esGasto = it.getInt(
                            it.getColumnIndexOrThrow("esGasto")
                        ) == 1
                    )
                )
            }
        }

        return categorias
    }

    override suspend fun eliminarCategoria(nombre: String): Int {
        val valores = arrayOf(nombre)

        return dbHelper.writableDatabase.delete(
            "categorias",
            "nombre = ?",
            valores
        )
    }
}