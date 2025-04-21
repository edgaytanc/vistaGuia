package com.example.etiquetadodeimagenml_kotlin

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var Imagen : ImageView
    private lateinit var BtnEtiquetarImagen : Button
    private lateinit var Resultados : TextView

    private lateinit var imageLabeler : ImageLabeler
    private lateinit var progressDialog: ProgressDialog

    var imageUri : Uri ?= null

    private lateinit var translatorOptions : TranslatorOptions
    private lateinit var translator : Translator

    private val codigo_idioma_origen = "en"
    private val codigo_idioma_destino = "es"

    private var Texto_etiquetas = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InicializarVistas()

        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        //val bitmapDrawable = Imagen.drawable as BitmapDrawable
        //val bitmap = bitmapDrawable.bitmap

        BtnEtiquetarImagen.setOnClickListener {
            //EtiquetarImagen(bitmap)
            if (imageUri != null){
                EtiquetarImagenGaleria(imageUri!!)
            }else{
                Toast.makeText(applicationContext,"Por favor adjunte una imagen", Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun InicializarVistas(){
        Imagen = findViewById(R.id.Imagen)
        BtnEtiquetarImagen = findViewById(R.id.BtnEtiquetarImagen)
        Resultados = findViewById(R.id.Resultados)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)
    }

    private fun EtiquetarImagen(bitmap: Bitmap) {
        progressDialog.setMessage("Reconociendo objetos de la imagen")
        progressDialog.show()

        val inputImagen = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImagen)
            .addOnSuccessListener {labels->
                for (imageLabel in labels){
                    /*Obtener la etiqueta casa, gato , cielo , pastel*/
                    val etiqueta = imageLabel.text
                    /*Obtener el porcentaje de confianza 92% 95% 70%*/
                    val confianza = imageLabel.confidence
                    /*Obtener el indice*/
                    val indice = imageLabel.index


                    Resultados.append("Etiqueta: $etiqueta \n Confianza: $confianza \n Indice: $indice \n \n")

                }
                progressDialog.dismiss()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext,"No se pudo realizar la etiqueta de imagen debido a: ${e.message}"
                ,Toast.LENGTH_SHORT).show()
            }


    }

    private fun EtiquetarImagenGaleria(imageUri: Uri) {
        Resultados.text = ""
        progressDialog.setMessage("Reconociendo objetos de la imagen")
        progressDialog.show()

        var inputImage : InputImage ?= null
        try {
            inputImage = InputImage.fromFilePath(applicationContext, imageUri)
        }catch (e: IOException){
            e.printStackTrace()
        }

        if (inputImage != null)
        {
            imageLabeler.process(inputImage)
                .addOnSuccessListener {labels->
                    for (imageLabel in labels){
                        /*Obtener la etiqueta casa, gato , cielo , pastel*/
                        val etiqueta = imageLabel.text
                        /*Obtener el porcentaje de confianza 92% 95% 70%*/
                        val confianza = imageLabel.confidence
                        /*Obtener el indice*/
                        val indice = imageLabel.index


                        Resultados.append("||| Name: $etiqueta \n - with a confidence of: $confianza \n - and its index is: $indice \n \n")

                    }
                    progressDialog.dismiss()

                }
                .addOnFailureListener{e->
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext,"No se pudo realizar la etiqueta de imagen debido a: ${e.message}"
                        ,Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun TraducirTexto(){
        Texto_etiquetas = Resultados.text.toString().trim()
        progressDialog.setMessage("Procesando")
        progressDialog.show()

        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(codigo_idioma_origen)
            .setTargetLanguage(codigo_idioma_destino)
            .build()

        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener{
                progressDialog.setMessage("Traduciendo etiquetas")

                translator.translate(Texto_etiquetas)
                    .addOnSuccessListener { etiquetasTraducidas->
                        //Traducción es exitosa
                        progressDialog.dismiss()
                        Resultados.text = etiquetasTraducidas

                    }.addOnFailureListener{e->
                        progressDialog.dismiss()
                        Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_SHORT).show()

                    }
            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.mi_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.MenuGaleria ->{
                //Toast.makeText(applicationContext, "Abrir galería", Toast.LENGTH_SHORT).show()
                SeleccionarImagenGaleria()
                true
            }
            R.id.MenuTraducir->{
                Texto_etiquetas = Resultados.text.toString().trim()
                if (!Texto_etiquetas.isEmpty()){
                    TraducirTexto()
                }else{
                    Toast.makeText(applicationContext,"No hay etiquetas para traducir",Toast.LENGTH_SHORT).show()
                }
                true
            }

            else-> super.onOptionsItemSelected(item)
        }
    }

    private fun SeleccionarImagenGaleria(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galeriaARL.launch(intent)
    }

    private val galeriaARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback { result->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                Imagen.setImageURI(imageUri)
                Resultados.text = ""
            }
            else{
                Toast.makeText(applicationContext,"Cancelado por el usuario", Toast.LENGTH_SHORT).show()
            }

        }
    )
}