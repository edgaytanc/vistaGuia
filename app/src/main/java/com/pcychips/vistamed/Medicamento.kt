package com.pcychips.vistamed

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla 'Medicamento' en la base de datos.
 * Contiene la informacion principal de cada medicamento.
 */

@Entity(tableName = "Medicamento")
data class Medicamento(
    /**
     * Clave primaria autoincrementable para el medicamento.
     */
    @PrimaryKey(autoGenerate = true)
    val id_medicamento: Int = 0, //Se inicializa en 0 para que Room lo autogenere

    /**
     * Nombre del medicamento. Es un campo obligatorio
     */
    val nombre: String,

    /**
     * Dosis del medicamento. Campo opcional
     */
    val dosis: String?, // indica que es nullable (opcional)

    /**
     * fecha de caducidad del medicamento. Campo opcional
     * Se almacena como String para flexibilidad
     */
    val fecha_caducidad: String?,

    /**
     * Instruciones de uso del medicamenteo. Caompo opcional.
     */
    val instrucciones_uso: String?,

    /**
     * Advertencias o precauciones del medicamento. campo opcional
     */
    val advertencias: String?,

    /**
     * Fecha y hora de registro del medicamento en la aplicación.
     * Por defecto, se establece la fecha y hora actual al insertar.
     * Room no soporta directamente DEFAULT CURRENT_TIMESTAMP en la anotación,
     * pero se puede manejar en el DAO o al insertar. Aquí se define como String.
     */
    val fecha_registro: String? = null // Se puede manejar el valor por defecto al insertar
)
