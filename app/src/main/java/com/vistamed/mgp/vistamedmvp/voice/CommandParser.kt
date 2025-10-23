package com.vistamed.mgp.vistamedmvp.voice

import com.vistamed.mgp.vistamedmvp.core.LabelUtils

/**
 * Define los comandos de voz que la app puede entender.
 */
sealed class Command {
    object ActivarExploracion : Command()
    object ModoBusqueda : Command()
    data class Buscar(val objetivo: String) : Command()
    object Detener : Command()
    object ModoActual : Command()
    object Desconocido : Command()
}

/**
 * Parsea el texto crudo del motor de voz a un [Command] estructurado.
 */
object CommandParser {

    fun parse(rawText: String): Command {
        // Normaliza el texto: minúsculas, sin acentos, sin espacios extra
        // Ej: "Busca Paracetamol" -> "busca paracetamol"
        val text = LabelUtils.normalize(rawText)

        // Compara el texto normalizado con los comandos
        return when {
            text == "activar exploracion" -> Command.ActivarExploracion
            text == "modo busqueda" -> Command.ModoBusqueda
            text == "modo actual" || text == "que modo" -> Command.ModoActual
            text == "detener" -> Command.Detener

            // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
            // Aceptamos tanto "buscar" (infinitivo) como "busca" (imperativo)
            text.startsWith("buscar ") -> {
                val objetivo = text.substringAfter("buscar ").trim()
                if (objetivo.isNotEmpty()) Command.Buscar(objetivo) else Command.Desconocido
            }

            text.startsWith("busca ") -> {
                val objetivo = text.substringAfter("busca ").trim()
                if (objetivo.isNotEmpty()) Command.Buscar(objetivo) else Command.Desconocido
            }
            // --- FIN DE LA CORRECCIÓN ---

            else -> Command.Desconocido
        }
    }
}