package com.pcychips.vistamed

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddEditMedicamentoActivity : AppCompatActivity() {

    // ViewModel para interactuar con la base de datos
    private val medicamentoViewModel: MedicamentoViewModel by viewModels {
        MedicamentoViewModelFactory((application as MedicamentosApplication).repository)
    }

    // Referencias a todas las vistas del layout
    private lateinit var editTextNombre: EditText
    private lateinit var editTextDosis: EditText
    private lateinit var editTextInstrucciones: EditText
    private lateinit var editTextAdvertencias: EditText
    private lateinit var imageViewCaja: ImageView
    private lateinit var buttonGuardar: Button
    private lateinit var buttonSeleccionarImagen: Button
    private lateinit var buttonBorrar: Button

    // Variable para saber si estamos editando un medicamento existente
    private var medicamentoActual: Medicamento? = null

    // Lanzador para el selector de imágenes
    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Muestra la imagen seleccionada en el ImageView
                imageViewCaja.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_medicamento)

        // Inicialización completa de todas las vistas
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextDosis = findViewById(R.id.editTextDosis)
        editTextInstrucciones = findViewById(R.id.editTextInstrucciones)
        editTextAdvertencias = findViewById(R.id.editTextAdvertencias)
        imageViewCaja = findViewById(R.id.imageViewCaja)
        buttonGuardar = findViewById(R.id.buttonGuardar)
        buttonSeleccionarImagen = findViewById(R.id.buttonSeleccionarImagen)
        buttonBorrar = findViewById(R.id.buttonBorrar)

        // Obtenemos el ID del medicamento que nos pasaron desde MainActivity
        val medicamentoId = intent.getIntExtra("MEDICAMENTO_ID", -1)

        if (medicamentoId != -1) {
            // --- MODO EDICIÓN ---
            // Hacemos visible el botón de borrar
            buttonBorrar.visibility = View.VISIBLE
            lifecycleScope.launch {
                // Buscamos el medicamento específico en la base de datos usando su ID
                medicamentoActual = medicamentoViewModel.medicamentos.firstOrNull()
                    ?.find { it.id_medicamento == medicamentoId }

                // Si lo encontramos, llenamos el formulario con sus datos
                medicamentoActual?.let { poblarFormulario(it) }
            }
        }

        // Asignamos los listeners a los botones
        buttonGuardar.setOnClickListener { guardarMedicamento() }
        buttonSeleccionarImagen.setOnClickListener { seleccionarImagenLauncher.launch("image/*") }
        buttonBorrar.setOnClickListener { borrarMedicamento() }
    }

    /**
     * Rellena todos los campos del formulario con los datos de un medicamento existente.
     */
    private fun poblarFormulario(medicamento: Medicamento) {
        editTextNombre.setText(medicamento.nombre)
        editTextDosis.setText(medicamento.dosis)
        editTextInstrucciones.setText(medicamento.instrucciones_uso)
        editTextAdvertencias.setText(medicamento.advertencias)
    }

    /**
     * Lógica para guardar los cambios, ya sea creando o actualizando.
     */
    private fun guardarMedicamento() {
        val nombre = editTextNombre.text.toString()
        val dosis = editTextDosis.text.toString()
        val instrucciones = editTextInstrucciones.text.toString()
        val advertencias = editTextAdvertencias.text.toString()

        // Validación: El nombre es obligatorio
        if (nombre.isBlank()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (medicamentoActual != null) {
            // --- UPDATE (Actualizar) ---
            val medicamentoActualizado = medicamentoActual!!.copy(
                nombre = nombre,
                dosis = dosis,
                instrucciones_uso = instrucciones,
                advertencias = advertencias
            )
            medicamentoViewModel.update(medicamentoActualizado)
            Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_SHORT).show()
        } else {
            // --- CREATE (Crear) ---
            val nuevoMedicamento = Medicamento(
                nombre = nombre,
                dosis = dosis,
                fecha_caducidad = null,
                instrucciones_uso = instrucciones,
                advertencias = advertencias,
                fecha_registro = System.currentTimeMillis().toString()
            )
            medicamentoViewModel.insert(nuevoMedicamento)
            Toast.makeText(this, "Medicamento guardado", Toast.LENGTH_SHORT).show()
        }
        // Cerramos la actividad y volvemos a la lista
        finish()
    }

    /**
     * Lógica para eliminar el medicamento actual.
     */
    private fun borrarMedicamento() {
        medicamentoActual?.let {
            medicamentoViewModel.delete(it)
            Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}