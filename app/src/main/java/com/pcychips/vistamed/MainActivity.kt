package com.pcychips.vistamed

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "VistaMedPrefs"
    private val PREF_INITIAL_DATA_INSERTED = "initial_data_inserted"

    private val medicamentoViewModel: MedicamentoViewModel by viewModels {
        MedicamentoViewModelFactory((application as MedicamentosApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Usamos nuestro nuevo layout
        setContentView(R.layout.activity_main)

        // Configuramos el RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMedicamentos)
        // Pasamos una función lambda al adapter que se ejecutará al hacer clic
        val adapter = MedicamentoAdapter { medicamento ->
            // Cuando el usuario pulsa un item, abrimos la actividad de edición
            val intent = Intent(this, AddEditMedicamentoActivity::class.java).apply {
                // Ponemos el ID del medicamento en el Intent como un "extra"
                putExtra("MEDICAMENTO_ID", medicamento.id_medicamento)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

// Observamos los datos y los enviamos al adapter
        lifecycleScope.launch {
            medicamentoViewModel.medicamentos.collect { medicamentos ->
                // El ListAdapter tiene un método especial 'submitList' que maneja las actualizaciones
                adapter.submitList(medicamentos)
            }
        }

        // Configurar el botón flotante (FAB)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddMedicamento)
        fab.setOnClickListener {
            // ESTA ES LA LÓGICA QUE ABRE LA NUEVA PANTALLA
            val intent = Intent(this@MainActivity, AddEditMedicamentoActivity::class.java)
            startActivity(intent)
        }

        insertInitialDataIfNeeded()
    }

    private fun insertInitialDataIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val initialDataInserted = prefs.getBoolean(PREF_INITIAL_DATA_INSERTED, false)

        if (!initialDataInserted) {
            medicamentoViewModel.insertInitialData()
            prefs.edit().putBoolean(PREF_INITIAL_DATA_INSERTED, true).apply()
        }
    }
}