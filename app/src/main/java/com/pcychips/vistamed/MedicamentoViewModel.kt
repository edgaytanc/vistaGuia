package com.pcychips.vistamed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// CLASE PRINCIPAL DEL VIEWMODEL
class MedicamentoViewModel(private val repository: MedicamentoRepository) : ViewModel() {

    // Flujo de datos para la lista de medicamentos
    val medicamentos: Flow<List<Medicamento>> = repository.getAllMedicamentos()

    // Función para insertar un nuevo medicamento
    fun insert(medicamento: Medicamento) = viewModelScope.launch {
        repository.insert(medicamento)
    }

    // Función para insertar los datos de ejemplo
    fun insertInitialData() = viewModelScope.launch {
        // (Aquí puedes pegar la lógica que tenías para insertar Ibuprofeno, etc.)
        // Por ahora la dejamos vacía para simplificar.
    }

    fun update(medicamento: Medicamento) = viewModelScope.launch {
        repository.update(medicamento)
    }

    fun delete(medicamento: Medicamento) = viewModelScope.launch {
        repository.delete(medicamento)
    }
}


// FÁBRICA PARA CREAR EL VIEWMODEL. DEBE ESTAR AQUÍ, FUERA DE LA OTRA CLASE.
class MedicamentoViewModelFactory(private val repository: MedicamentoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicamentoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicamentoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}