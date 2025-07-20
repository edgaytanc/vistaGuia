package com.pcychips.vistamed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad 'Medicamento' en la base de datos.
 * Define los metodos para interactuar con la tabla 'Medicamento' en la base de datos.
 */

@Dao
interface MedicamentoDao {
    /**
     * Inserta un nuevo medicamento en la base de datos.
     * Si hay u conflicto (ej. id_medicamento ya existe en la base de datos), lo ignora.
     * @param medicamento El medicamento a insertar.
     * @return El ID de la fila del medicamento insertado.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(medicamento: Medicamento): Long

    /**
     * Actualiza un medicamento existente en la base de datos.
     * @param medicamento El objeto Medicamento a actualizar.
     * @return El número de filas actualizadas.
     */
    @Update
    suspend fun update(medicamento: Medicamento): Int

    /**
     * Elimina un medicamento de la base de datos.
     * @param medicamento El objeto Medicamento a eliminar.
     * @return El número de filas eliminadas.
     */
    @Delete
    suspend fun delete(medicamento: Medicamento): Int

    /**
     * Obtiene un medicamento por su ID.
     * @param id_medicamento El ID del medicamento a buscar.
     * @return Un objeto Medicamento o null si no se encuentra.
     */
    @Query("SELECT * FROM Medicamento WHERE id_medicamento = :id_medicamento")
    suspend fun getMedicamentoById(id_medicamento: Int): Medicamento?

    /**
     * Obtiene todos los medicamentos de la base de datos, ordenados por nombre.
     * Se devuelve como un Flow para observar cambios en tiempo real.
     * @return Un Flow que emite una lista de todos los medicamentos.
     */
    @Query("SELECT * FROM Medicamento ORDER BY nombre ASC")
    fun getAllMedicamentos(): Flow<List<Medicamento>>

    /**
     * Busca medicamentos por nombre (case-insensitive).
     * @param query La cadena de búsqueda.
     * @return Una lista de medicamentos que coinciden con la consulta.
     */
    @Query("SELECT * FROM Medicamento WHERE nombre LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY nombre ASC")
    suspend fun searchMedicamentos(query: String): List<Medicamento>
}