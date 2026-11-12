package it.lopo.validabuonicartecultura

import android.content.Context

/** I due portali, con l'indirizzo della pagina pubblica "Valida Buono". */
enum class Carta(val nome: String, val url: String) {
    DOCENTE(
        "Carta del Docente",
        "https://www.cartadeldocente.istruzione.it/docentiEsercente/#/validaBuono"
    ),
    GIOVANI(
        "Carte Cultura Giovani e Merito",
        "https://cartegiovani.cultura.gov.it/giovanimeritoEsercente/#/validaBuono"
    )
}

/** Codici esercente salvati sul telefono, uno per portale. */
object Impostazioni {
    private const val FILE = "impostazioni"

    fun codiceEsercente(context: Context, carta: Carta): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(carta.name, "").orEmpty()

    fun salvaCodiceEsercente(context: Context, carta: Carta, codice: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(carta.name, codice.trim().uppercase())
            .apply()
    }
}

fun Context.dp(valore: Int): Int = (valore * resources.displayMetrics.density).toInt()
