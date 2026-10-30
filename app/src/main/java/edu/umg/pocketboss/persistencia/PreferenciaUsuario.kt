package edu.umg.pocketboss

import android.content.Context

object PreferenciasUsuario {

    private const val PREFS = "pocketboss_prefs"
    private const val KEY_NOMBRE = "nombre_usuario"

    fun obtenerNombre(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NOMBRE, null)
    }

    fun guardarNombre(context: Context, nombre: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOMBRE, nombre.trim()).apply()
    }
}