package com.pcychips.vistamed

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Entidad de Room que representa la tabla 'ImagenCaja' en la base de datos.
 * Almacena las rutas de los archivos de imagenes de las cajas de medicamentos.
 * y las asocia a un medicamento especifico mediante una clave foranea.
 */

@Entity(
    tableName = "ImagenCaja",
    foreignKeys = [
        ForeignKey(
            entity = Medicamento::class,
            parentColumns = ["id_medicamento"],
            childColumns = ["id_medicamento"],
            onDelete = ForeignKey.CASCADE // Si se elimina un Medicamento, sus ImagenCaja asociadas también se eliminan
        )
    ]
)

data class ImagenCaja(
    /**
     * Clave primaria autoincrementable para la imagen de la caja.
     */
    @PrimaryKey(autoGenerate = true)
    val id_imagen: Int = 0, // Se inicializa en 0 para que Room lo autogenere

    /**
     * Clave foranea que referencia al 'id_medicamento' en la tabla 'Medicamento'.
     * indica a que medicamento pertenece esta imagen.
     */
    val id_medicamento: Int,
    /**
     * Ruta del archivo de imagen de la caja.
     */
    val ruta_archivo: String,

    /**
     * Descripción opcional de la imagen (ej. "cara frontal", "cara lateral").
     */
    val descripcion: String?,

    /**
     * Fecha y hora de la captura de la imagen. Campo opcional.
     * Por defecto, se establece la fecha y hora actual al insertar.
     */
    val fecha_captura: String? = null // Se puede manejar el valor por defecto al insertar
)
