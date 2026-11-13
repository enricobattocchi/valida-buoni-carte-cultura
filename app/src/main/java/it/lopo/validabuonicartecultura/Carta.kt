package it.lopo.validabuonicartecultura

import android.content.Context

/**
 * I due portali, con l'indirizzo della pagina pubblica "Valida Buono" e il dominio
 * del ministero: le pagine di quel dominio restano nell'app, le altre si aprono nel browser.
 */
enum class Carta(val nome: String, val url: String, val dominio: String) {
    DOCENTE(
        "Carta del Docente",
        "https://www.cartadeldocente.istruzione.it/docentiEsercente/#/validaBuono",
        "istruzione.it"
    ),
    GIOVANI(
        "Carte Cultura Giovani e Merito",
        "https://cartegiovani.cultura.gov.it/giovanimeritoEsercente/#/validaBuono",
        "cultura.gov.it"
    );

    /**
     * Vero se l'indirizzo è del ministero del portale, in https. Schema e host non
     * distinguono maiuscole e minuscole, come negli indirizzi web.
     */
    fun restaNellApp(schema: String?, host: String?): Boolean =
        schema.equals("https", ignoreCase = true) && host != null &&
            (host.equals(dominio, ignoreCase = true) || host.endsWith(".$dominio", ignoreCase = true))
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
