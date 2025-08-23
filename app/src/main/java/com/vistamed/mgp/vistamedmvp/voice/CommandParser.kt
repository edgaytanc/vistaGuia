package com.vistamed.mgp.vistamedmvp.voice

sealed class Command {
    data object ActivarExploracion : Command()
    data object ModoBusqueda : Command()
    data class Buscar(val objetivo: String) : Command()
    data object Detener : Command()
    data object Desconocido : Command()
}

object CommandParser {
    fun parse(text: String): Command {
        val t = text.lowercase().trim()
        return when {
            t.contains("activar exploración") || t.contains("activar exploracion") -> Command.ActivarExploracion
            t.contains("modo búsqueda") || t.contains("modo busqueda") -> Command.ModoBusqueda
            t.startsWith("buscar ") -> Command.Buscar(t.removePrefix("buscar ").trim())
            t.contains("detener") || t.contains("parar") -> Command.Detener
            else -> Command.Desconocido
        }
    }
}
