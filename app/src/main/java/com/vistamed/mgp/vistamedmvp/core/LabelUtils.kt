package com.vistamed.mgp.vistamedmvp.core

import java.text.Normalizer

object LabelUtils {

    // Mapa de sinónimos: clave = forma canónica, valores = alias/comercial
    // Ajusta con tus medicamentos más comunes.
    private val synonyms: Map<String, List<String>> = mapOf(
        "acetaminofen" to listOf("paracetamol", "tylenol"),
        "ibuprofeno"   to listOf("advil", "motrin"),
        "aspirina"     to listOf("acido acetilsalicilico", "asa", "bayer"),
        "amoxicilina"  to listOf("amoxil")
    )

    /** Quita acentos, pasa a minúsculas, elimina signos comunes. */
    fun normalize(input: String): String {
        val lower = input.lowercase()
        val noAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
        // Cambia separadores y limpia
        return noAccents
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /** Devuelve un conjunto de variantes (target + sinónimos) ya normalizados. */
    fun variants(target: String): Set<String> {
        val base = normalize(target)
        val extras = synonyms[base]?.map { normalize(it) } ?: emptyList()
        return (listOf(base) + extras).toSet()
    }

    /** ¿Alguna variante está contenida en la etiqueta detectada? */
    fun matches(target: String, detectedLabel: String): Boolean {
        val d = normalize(detectedLabel)
        val vars = variants(target)
        return vars.any { v -> d.contains(v) }
    }
}
