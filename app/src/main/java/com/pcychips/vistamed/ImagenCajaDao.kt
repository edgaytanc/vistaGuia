package com.pcychips.vistamed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad ImagenCaja.
 * Define los métodos para interactuar con la tabla 'ImagenCaja' en la base de datos.
 */
@Dao
interface ImagenCajaDao {

    /**
     * Inserta una o varias imágenes de caja en la base de datos.
     * @param imagenCaja Las objetos ImagenCaja a insertar.
     * @return Un array de IDs de fila de las imágenes insertadas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // REPLACE para permitir actualizar si la imagen ya existe por alguna razón
    suspend fun insert(vararg imagenCaja: ImagenCaja): LongArray

    /**
     * Actualiza una imagen de caja existente en la base de datos.
     * @param imagenCaja El objeto ImagenCaja a actualizar.
     * @return El número de filas actualizadas.
     */
    @Update
    suspend fun update(imagenCaja: ImagenCaja): Int

    /**
     * Elimina una imagen de caja de la base de datos.
     * @param imagenCaja El objeto ImagenCaja a eliminar.
     * @return El número de filas eliminadas.
     */
    @Delete
    suspend fun delete(imagenCaja: ImagenCaja): Int

    /**
     * Obtiene todas las imágenes de caja asociadas a un medicamento específico.
     * @param id_medicamento El ID del medicamento cuyas imágenes se desean obtener.
     * @return Una lista de objetos ImagenCaja.
     */
    @Query("SELECT * FROM ImagenCaja WHERE id_medicamento = :id_medicamento ORDER BY fecha_captura ASC")
    suspend fun getImagenesByMedicamentoId(id_medicamento: Int): List<ImagenCaja>

    /**
     * Obtiene una imagen de caja por su ID.
     * @param id_imagen El ID de la imagen a buscar.
     * @return Un objeto ImagenCaja o null si no se encuentra.
     */
    @Query("SELECT * FROM ImagenCaja WHERE id_imagen = :id_imagen")
    suspend fun getImagenCajaById(id_imagen: Int): ImagenCaja?

    /**
     * Obtiene todas las imágenes de caja de la base de datos.
     * Se devuelve como un Flow para observar cambios en tiempo real.
     * @return Un Flow que emite una lista de todas las imágenes de caja.
     */
    @Query("SELECT * FROM ImagenCaja ORDER BY fecha_captura DESC")
    fun getAllImagenCajas(): Flow<List<ImagenCaja>>
}