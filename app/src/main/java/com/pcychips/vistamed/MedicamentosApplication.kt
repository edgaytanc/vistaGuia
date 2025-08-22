package com.pcychips.vistamed

import android.app.Application

/**
 * Clase Application para centralizar la creación de singletons (objetos únicos).
 * Es el primer código que se ejecuta al iniciar la app.
 */
class MedicamentosApplication : Application() {
    // Usamos 'lazy' para que la base de datos y el repo solo se creen cuando se necesiten por primera vez.
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { MedicamentoRepository(database.medicamentoDao(), database.imagenCajaDao()) }
}