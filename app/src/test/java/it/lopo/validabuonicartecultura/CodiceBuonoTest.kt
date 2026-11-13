package it.lopo.validabuonicartecultura

import it.lopo.validabuonicartecultura.CodiceBuono.Lettura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodiceBuonoTest {

    @Test
    fun codiceDaSoloSiUsaSubito() {
        assertEquals(Lettura.Esatto("AB12cd34"), CodiceBuono.leggi("AB12cd34"))
    }

    @Test
    fun spaziAttornoVengonoIgnorati() {
        assertEquals(Lettura.Esatto("AB12CD34"), CodiceBuono.leggi("  AB12CD34\n"))
    }

    @Test
    fun testoVuotoNonProduceNulla() {
        assertNull(CodiceBuono.leggi(""))
        assertNull(CodiceBuono.leggi("   "))
    }

    @Test
    fun codiceDentroUnIndirizzoVieneProposto() {
        assertEquals(
            Lettura.DaControllare("AB12CD34"),
            CodiceBuono.leggi("https://esempio.it/buono?codice=AB12CD34")
        )
    }

    @Test
    fun sequenzePiuLungheNonSonoCodici() {
        // 9 caratteri: non è un codice, quindi si propone tutto il testo.
        assertEquals(Lettura.DaControllare("AB12CD345"), CodiceBuono.leggi("AB12CD345"))
    }

    @Test
    fun piuCandidatiLascianoIlTestoIntero() {
        val testo = "AB12CD34 EF56GH78"
        assertEquals(Lettura.DaControllare(testo), CodiceBuono.leggi(testo))
    }

    @Test
    fun nessunCandidatoLasciaIlTestoIntero() {
        assertEquals(Lettura.DaControllare("ciao"), CodiceBuono.leggi("ciao"))
    }
}
