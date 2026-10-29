package edu.umg.pocketboss.persistencia

import android.content.ContentValues
import android.content.Context
import datos.GastosInterface
import modelo.Transaccion

class GastosDao(context: Context) : GastosInterface {

    private val dbHelper = FinanzasDatabase(context)

    override suspend fun agregar(gasto: Transaccion): Int {
        val valores = ContentValues().apply {
            put("descripcion", gasto.descripcion)
            put("monto", gasto.monto)
            put("categoria", gasto.categoria)
            put("fecha", gasto.fecha)
        }

        val resultado = dbHelper.writableDatabase.insertOrThrow(
            "transacciones",
            null,
            valores
        )

        return resultado.toInt()
    }

    override suspend fun listar(): List<Transaccion> {
        val transacciones = mutableListOf<Transaccion>()

        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT id, descripcion, monto, categoria, fecha
            FROM transacciones
            ORDER BY id DESC
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                transacciones.add(
                    Transaccion(
                        id = it.getInt(0),
                        descripcion = it.getString(1),
                        monto = it.getInt(2),
                        categoria = it.getInt(3),
                        fecha = it.getString(4)
                    )
                )
            }
        }

        return transacciones
    }

    override suspend fun buscarPorId(id: Int): Transaccion? {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT id, descripcion, monto, categoria, fecha
            FROM transacciones
            WHERE id = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return Transaccion(
                    id = it.getInt(0),
                    descripcion = it.getString(1),
                    monto = it.getInt(2),
                    categoria = it.getInt(3),
                    fecha = it.getString(4)
                )
            }
        }

        return null
    }

    override suspend fun eliminarPorId(id: Int): Boolean {
        val filasEliminadas = dbHelper.writableDatabase.delete(
            "transacciones",
            "id = ?",
            arrayOf(id.toString())
        )

        return filasEliminadas > 0
    }

    override suspend fun obtenerCategoria(nombre: String): Int {
        val cursor = dbHelper.readableDatabase.rawQuery(
            """
            SELECT id
            FROM categorias
            WHERE nombre = ?
            """.trimIndent(),
            arrayOf(nombre)
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }

        throw IllegalArgumentException(
            "No existe una categoría con el nombre: $nombre"
        )
    }
}
