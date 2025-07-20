package com.pcychips.vistamed


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "VistaMedPrefs"
    private val PREF_INITIAL_DATA_INSERTED = "initial_data_inserted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establece un layout simple para el MVP
        val textView = TextView(this).apply {
            text = "Iniciando VistaMed...\nVerificando datos iniciales..."
            textSize = 20f
            setPadding(32, 32, 32, 32)
        }
        setContentView(textView)

        // Obtiene una instancia de SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val initialDataInserted = prefs.getBoolean(PREF_INITIAL_DATA_INSERTED, false)

        // Si los datos iniciales no han sido insertados, procedemos a insertarlos
        if (!initialDataInserted) {
            textView.text = "Insertando datos de prueba..."
            insertInitialData(textView) { success ->
                if (success) {
                    prefs.edit().putBoolean(PREF_INITIAL_DATA_INSERTED, true).apply()
                    textView.text = "Datos iniciales insertados exitosamente.\n¡MVP Listo para la siguiente fase!"
                } else {
                    textView.text = "Error al insertar datos iniciales."
                }
            }
        } else {
            textView.text = "Datos iniciales ya existen.\n¡MVP Listo para la siguiente fase!"
        }
    }

    /**
     * Inserta datos de medicamentos y sus imágenes de ejemplo en la base de datos.
     * Esta función se ejecuta en un hilo de fondo.
     * @param statusTextView TextView para actualizar el estado en la UI.
     * @param onComplete Callback que se ejecuta al finalizar la inserción, indicando si fue exitosa.
     */
    private fun insertInitialData(statusTextView: TextView, onComplete: (Boolean) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val medicamentoDao = db.medicamentoDao()
                val imagenCajaDao = db.imagenCajaDao()

                // --- Medicamento 1: Ibuprofeno ---
                val ibuprofeno = Medicamento(
                    nombre = "Ibuprofeno 600mg",
                    dosis = "Tomar 1 comprimido cada 8 horas",
                    fecha_caducidad = "2026-12-31",
                    instrucciones_uso = "No exceder la dosis recomendada. Consultar al médico en caso de dudas.",
                    advertencias = "Puede causar irritación gástrica.",
                    fecha_registro = System.currentTimeMillis().toString() // Usar timestamp para fecha
                )
                val ibuprofenoId = medicamentoDao.insert(ibuprofeno)
                Log.d("VistaMed", "Ibuprofeno insertado con ID: $ibuprofenoId")

                val imgIbuprofeno1 = ImagenCaja(
                    id_medicamento = ibuprofenoId.toInt(),
                    ruta_archivo = "/data/data/com.yourcompany.vistamed/files/ibuprofeno_frontal.png", // Ruta de ejemplo
                    descripcion = "Cara frontal de la caja de Ibuprofeno",
                    fecha_captura = System.currentTimeMillis().toString()
                )
                val imgIbuprofeno2 = ImagenCaja(
                    id_medicamento = ibuprofenoId.toInt(),
                    ruta_archivo = "/data/data/com.yourcompany.vistamed/files/ibuprofeno_lateral.png", // Ruta de ejemplo
                    descripcion = "Cara lateral de la caja de Ibuprofeno",
                    fecha_captura = System.currentTimeMillis().toString()
                )
                imagenCajaDao.insert(imgIbuprofeno1, imgIbuprofeno2)

                // --- Medicamento 2: Paracetamol ---
                val paracetamol = Medicamento(
                    nombre = "Paracetamol 500mg",
                    dosis = "Tomar 1-2 comprimidos cada 6 horas",
                    fecha_caducidad = "2027-06-30",
                    instrucciones_uso = "No exceder 4g al día. Evitar alcohol.",
                    advertencias = "Riesgo de daño hepático en sobredosis.",
                    fecha_registro = System.currentTimeMillis().toString()
                )
                val paracetamolId = medicamentoDao.insert(paracetamol)
                Log.d("VistaMed", "Paracetamol insertado con ID: $paracetamolId")

                val imgParacetamol1 = ImagenCaja(
                    id_medicamento = paracetamolId.toInt(),
                    ruta_archivo = "/data/data/com.yourcompany.vistamed/files/paracetamol_frontal.png",
                    descripcion = "Cara frontal de la caja de Paracetamol",
                    fecha_captura = System.currentTimeMillis().toString()
                )
                imagenCajaDao.insert(imgParacetamol1)

                // --- Medicamento 3: Amoxicilina ---
                val amoxicilina = Medicamento(
                    nombre = "Amoxicilina 250mg",
                    dosis = "Tomar 1 cápsula cada 8 horas por 7 días",
                    fecha_caducidad = "2025-10-15",
                    instrucciones_uso = "Completar el ciclo de tratamiento. No suspender sin indicación médica.",
                    advertencias = "Puede causar reacciones alérgicas. No usar en caso de alergia a penicilinas.",
                    fecha_registro = System.currentTimeMillis().toString()
                )
                val amoxicilinaId = medicamentoDao.insert(amoxicilina)
                Log.d("VistaMed", "Amoxicilina insertado con ID: $amoxicilinaId")

                val imgAmoxicilina1 = ImagenCaja(
                    id_medicamento = amoxicilinaId.toInt(),
                    ruta_archivo = "/data/data/com.yourcompany.vistamed/files/amoxicilina_frontal.png",
                    descripcion = "Cara frontal de la caja de Amoxicilina",
                    fecha_captura = System.currentTimeMillis().toString()
                )
                imagenCajaDao.insert(imgAmoxicilina1)

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }

            } catch (e: Exception) {
                Log.e("VistaMed", "Error al insertar datos iniciales: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}
