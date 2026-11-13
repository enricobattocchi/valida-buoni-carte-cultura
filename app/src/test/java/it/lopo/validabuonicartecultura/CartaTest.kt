package it.lopo.validabuonicartecultura

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartaTest {

    @Test
    fun ilPortaleRestaNellApp() {
        for (carta in Carta.entries) {
            val indirizzo = java.net.URI(carta.url)
            assertTrue(carta.name, carta.restaNellApp(indirizzo.scheme, indirizzo.host))
        }
    }

    @Test
    fun altriSitiDelMinisteroRestanoNellApp() {
        assertTrue(Carta.DOCENTE.restaNellApp("https", "www.istruzione.it"))
        assertTrue(Carta.DOCENTE.restaNellApp("https", "istruzione.it"))
        assertTrue(Carta.GIOVANI.restaNellApp("https", "www.cultura.gov.it"))
    }

    @Test
    fun sitiEsterniVannoNelBrowser() {
        assertFalse(Carta.DOCENTE.restaNellApp("https", "www.google.com"))
        assertFalse(Carta.DOCENTE.restaNellApp("https", "cultura.gov.it"))
        assertFalse(Carta.GIOVANI.restaNellApp("https", "istruzione.it"))
        // Un nome che finisce allo stesso modo ma non è un sottodominio.
        assertFalse(Carta.DOCENTE.restaNellApp("https", "falsaistruzione.it"))
        assertFalse(Carta.DOCENTE.restaNellApp("https", null))
    }

    @Test
    fun maiuscoleIgnorate() {
        assertTrue(Carta.DOCENTE.restaNellApp("HTTPS", "WWW.Istruzione.IT"))
        assertTrue(Carta.GIOVANI.restaNellApp("Https", "CarteGiovani.Cultura.Gov.It"))
        assertFalse(Carta.DOCENTE.restaNellApp("HTTP", "www.istruzione.it"))
    }

    @Test
    fun soloHttps() {
        assertFalse(Carta.DOCENTE.restaNellApp("http", "www.istruzione.it"))
        assertFalse(Carta.DOCENTE.restaNellApp("mailto", null))
    }
}
