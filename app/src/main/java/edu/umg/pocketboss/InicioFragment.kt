package edu.umg.pocketboss

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Botón flotante para Agregar Transacción
        val btnTransaccion = view.findViewById<FloatingActionButton>(R.id.btnAgregarTransaccionFab)
        btnTransaccion.setOnClickListener {
            val intent = Intent(requireContext(), AgregarTransaccionActivity::class.java)
            startActivity(intent)
        }

        // 2. Botón flotante para Agregar Categoría
        val btnCategoria = view.findViewById<FloatingActionButton>(R.id.btnAgregarCategoriaFab)
        btnCategoria.setOnClickListener {
            val intent = Intent(requireContext(), AgregarCategoriaActivity::class.java)
            startActivity(intent)
        }
    }
}