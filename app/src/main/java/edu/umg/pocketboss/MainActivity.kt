package edu.umg.pocketboss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar la pestaña de Inicio por defecto al abrir la aplicación
        if (savedInstanceState == null) {
            cambiarPestana(InicioFragment())
        }

        // Configurar la acción al hacer clic en las pestañas inferiores
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    cambiarPestana(InicioFragment())
                    true
                }
                R.id.nav_resumenes -> {
                    cambiarPestana(ResumenFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Función auxiliar para reemplazar los fragments de forma fluida
    private fun cambiarPestana(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}