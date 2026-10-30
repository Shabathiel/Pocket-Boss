package edu.umg.pocketboss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Siembra categorías por defecto la primera vez
        CategoriasIniciales.sembrarSiHaceFalta(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            cambiarPestana(InicioFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> { cambiarPestana(InicioFragment()); true }
                R.id.nav_resumenes -> { cambiarPestana(ResumenFragment()); true }
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