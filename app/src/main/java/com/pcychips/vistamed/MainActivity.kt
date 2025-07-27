package com.pcychips.vistamed

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "VistaMedPrefs"
    private val PREF_INITIAL_DATA_INSERTED = "initial_data_inserted"

    private val medicamentoViewModel: MedicamentoViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = MedicamentoRepository(database.medicamentoDao(), database.imagenCajaDao())
                return MedicamentoViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this).apply {
            textSize = 18f
            setPadding(40, 40, 40, 40)
            text = "Cargando datos del backend local..."
        }
        setContentView(textView)

        lifecycleScope.launch {
            medicamentoViewModel.medicamentos.collect { medicamentos ->
                val stringBuilder = StringBuilder("--- DEMO BACKEND LOCAL ---\n\n")
                if (medicamentos.isNotEmpty()) {
                    stringBuilder.append("Consulta a la base de datos exitosa:\n\n")
                    medicamentos.forEach { medicamento ->
                        stringBuilder.append("- ${medicamento.nombre}\n")
                        stringBuilder.append("  Dosis: ${medicamento.dosis ?: "N/A"}\n\n")
                    }
                } else {
                    stringBuilder.append("La base de datos está vacía. Insertando datos de ejemplo...")
                }
                textView.text = stringBuilder.toString()
            }
        }

        insertInitialDataIfNeeded()
    }

    private fun insertInitialDataIfNeeded() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val initialDataInserted = prefs.getBoolean(PREF_INITIAL_DATA_INSERTED, false)

        if (!initialDataInserted) {
            medicamentoViewModel.insertInitialData() // ¡Ahora esta llamada sí funcionará!
            prefs.edit().putBoolean(PREF_INITIAL_DATA_INSERTED, true).apply()
        }
    }
}