package com.pcychips.vistamed

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * clase abstracta que representa la base de datos Room para la aplicacion VistaMed.
 * Define las entidades y los DAOs asociados.
 */
@Database(entities = [Medicamento::class, ImagenCaja::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona el Data Acces Object (DAO) para la entidad medicamento.
     * @return una instancia de medicamentoDao
     */
    abstract fun medicamentoDao(): MedicamentoDao

    /**
     * Proporciona el Data Acces Object (DAO) para la entidad ImagenCaja.
     * @return una instancia de ImagenCajaDao
     */
    abstract fun imagenCajaDao(): ImagenCajaDao

    /**
     * Objeto complementario para proporcionar una instancia singleton de la base de datos.
     * Esto asegura que solo se cree una instancia de la base de datos en toda la aplicacion
     */
    companion object {
        @Volatile  // Hace que la instancia sea visible inmediatamente para otros hilos.
        private var INSTANCE: AppDatabase? = null

        /**
         * obtiene la instancia singleton de la base de datos.
         * Si la istancia es nula, la crea de forma segura en un bloque sincronizado.
         * @param context El contexto de la aplicacion.
         * @return la instancia de Appdatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            // si la instancia no es nula, devuelvela; de lo contrario, crea la base de datos.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vistamed_database" // nombre del archivo de la base de datos.
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}