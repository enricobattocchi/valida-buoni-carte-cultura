package it.lopo.validabuonicartecultura

/**
 * Riconosce il codice buono nel testo letto da un QR. Il codice è di 8 caratteri
 * alfanumerici. Non usa classi Android, così si prova con semplici unit test.
 */
object CodiceBuono {
    private val ESATTO = Regex("^[A-Za-z0-9]{8}$")
    private val NEL_TESTO = Regex("(?<![A-Za-z0-9])[A-Za-z0-9]{8}(?![A-Za-z0-9])")

    sealed class Lettura {
        /** Il QR contiene solo il codice: si può usare subito. */
        data class Esatto(val codice: String) : Lettura()

        /** Il QR contiene altro: [proposta] va fatta controllare prima di usarla. */
        data class DaControllare(val proposta: String) : Lettura()
    }

    fun leggi(testo: String): Lettura? {
        val pulito = testo.trim()
        if (pulito.isEmpty()) return null
        if (ESATTO.matches(pulito)) return Lettura.Esatto(pulito)
        val candidati = NEL_TESTO.findAll(pulito).map { it.value }.toList()
        return Lettura.DaControllare(if (candidati.size == 1) candidati[0] else pulito)
    }
}
