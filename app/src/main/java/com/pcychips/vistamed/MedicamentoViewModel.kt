package com.pcychips.vistamed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la lógica de negocio relacionada con los medicamentos.
 * Prepara y gestiona los datos para la UI.
 */
class MedicamentoViewModel(private val repository: MedicamentoRepository) : ViewModel() {

    private val _medicamentos = MutableStateFlow<List<Medicamento>>(emptyList())
    val medicamentos = _medicamentos.asStateFlow()

    init {
        // Observa el Flow del repositorio para obtener actualizaciones de la lista de medicamentos
        viewModelScope.launch {
            repository.getAllMedicamentos().collect { medicamentosList ->
                _medicamentos.value = medicamentosList
            }
        }
    }

    /**
     * Esta es la función que faltaba y que causaba el error.
     * Inserta los datos de ejemplo usando el repositorio en un hilo de fondo.
     */
    fun insertInitialData() {
        viewModelScope.launch(Dispatchers.IO) { // Se ejecuta en un hilo de fondo para no bloquear la UI
            try {
                // --- Medicamento 1: Ibuprofeno ---
                val ibuprofeno = Medicamento(
                    nombre = "Ibuprofeno 600mg",
                    dosis = "Tomar 1 comprimido cada 8 horas",
                    fecha_caducidad = "2026-12-31",
                    instrucciones_uso = "No exceder la dosis recomendada.",
                    advertencias = "Puede causar irritación gástrica.",
                    fecha_registro = System.currentTimeMillis().toString()
                )
                repository.insert(ibuprofeno)

                // --- Medicamento 2: Paracetamol ---
                val paracetamol = Medicamento(
                    nombre = "Paracetamol 500mg",
                    dosis = "Tomar 1-2 comprimidos cada 6 horas",
                    fecha_caducidad = "2027-06-30",
                    instrucciones_uso = "No exceder 4g al día. Evitar alcohol.",
                    advertencias = "Riesgo de daño hepático en sobredosis.",
                    fecha_registro = System.currentTimeMillis().toString()
                )
                repository.insert(paracetamol)

                // --- Medicamento 3: Amoxicilina ---
                val amoxicilina = Medicamento(
                    nombre = "Amoxicilina 250mg",
                    dosis = "Tomar 1 cápsula cada 8 horas por 7 días",
                    fecha_caducidad = "2025-10-15",
                    instrucciones_uso = "Completar el ciclo de tratamiento.",
                    advertencias = "Puede causar reacciones alérgicas.",
                    fecha_registro = System.currentTimeMillis().toString()
                )
                repository.insert(amoxicilina)

                Log.d("VistaMed", "Datos iniciales insertados exitosamente.")

            } catch (e: Exception) {
                Log.e("VistaMed", "Error al insertar datos iniciales: ${e.message}", e)
            }
        }
    }
}