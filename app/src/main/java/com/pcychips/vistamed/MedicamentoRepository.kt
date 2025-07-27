package com.pcychips.vistamed

import androidx.annotation.WorkerThread

/**
 * Repositorio para manejar el acceso a los datos de medicamentos.
 * Abstrae las fuentes de datos (en este caso, la base de datos Room) del resto de la app.
 */
class MedicamentoRepository(private val medicamentoDao: MedicamentoDao, private val imagenCajaDao: ImagenCajaDao) {

    /**
     * Un Flow que emite la lista completa de medicamentos. Room se encarga de mantenerlo actualizado.
     */
    fun getAllMedicamentos() = medicamentoDao.getAllMedicamentos()

    /**
     * Función suspendida para buscar medicamentos por un nombre. Se ejecuta en un hilo de fondo.
     */
    @WorkerThread
    suspend fun findMedicamento(query: String) = medicamentoDao.searchMedicamentos(query)

    /**
     * Función suspendida para obtener las imágenes asociadas a un medicamento.
     */
    @WorkerThread
    suspend fun getImagenes(medicamentoId: Int) = imagenCajaDao.getImagenesByMedicamentoId(medicamentoId)

    /**
     * Función suspendida para insertar un medicamento y sus imágenes.
     * Esta es la lógica que antes estaba en MainActivity.
     */
    @WorkerThread
    suspend fun insert(medicamento: Medicamento, imagenes: List<ImagenCaja> = emptyList()) {
        val medicamentoId = medicamentoDao.insert(medicamento)
        if (imagenes.isNotEmpty()) {
            imagenes.forEach { imagen ->
                // Asignamos el ID del medicamento recién insertado a cada imagen
                val imagenConId = imagen.copy(id_medicamento = medicamentoId.toInt())
                imagenCajaDao.insert(imagenConId)
            }
        }
    }
}