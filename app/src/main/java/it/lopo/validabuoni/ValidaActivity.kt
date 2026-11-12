package it.lopo.validabuoni

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.json.JSONObject

/**
 * Mostra la pagina reale "Valida Buono" del portale scelto e la aiuta in due modi:
 * precompila il codice esercente salvato e riempie il codice buono leggendo un QR.
 * La conferma della validazione resta un'azione manuale sulla pagina.
 */
class ValidaActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var carta: Carta

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        carta = Carta.valueOf(intent.getStringExtra(EXTRA_CARTA) ?: Carta.DOCENTE.name)
        title = carta.nome

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    iniettaAiuto()
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val barra = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        barra.addView(pulsante("Leggi QR code", peso = 3f) { leggiCodice() })
        barra.addView(pulsante("Ricarica", peso = 1f) { web.loadUrl(carta.url) })

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(web)
            addView(barra)
        })

        if (Impostazioni.codiceEsercente(this, carta).isEmpty()) {
            Toast.makeText(
                this,
                "Nessun codice esercente salvato per questa carta: aggiungilo nelle Impostazioni.",
                Toast.LENGTH_LONG
            ).show()
        }

        web.loadUrl(carta.url)
    }

    private fun pulsante(testo: String, peso: Float, azione: () -> Unit): Button =
        Button(this).apply {
            text = testo
            isAllCaps = false
            textSize = 18f
            setOnClickListener { azione() }
            layoutParams = LinearLayout.LayoutParams(0, dp(64), peso)
        }

    /**
     * Installa nella pagina un piccolo controllo periodico. I due portali sono
     * applicazioni Angular: i campi compaiono dopo il caricamento e ricompaiono
     * vuoti quando si torna al modulo, quindi un riempimento una tantum non basta.
     * I campi si riconoscono dall'attributo formcontrolname, uguale sui due siti:
     * "codiceEsercente" e "codiceVoucher".
     */
    private fun iniettaAiuto() {
        val codice = JSONObject.quote(Impostazioni.codiceEsercente(this, carta))
        val script = """
            (function () {
              window.__vbEsercente = $codice;
              if (window.__vbTimer) return;
              window.__vbScrivi = function (nome, valore) {
                var campo = document.querySelector('input[formcontrolname="' + nome + '"]');
                if (!campo) return false;
                campo.value = valore;
                // Angular aggiorna il proprio modello solo se riceve questi eventi.
                campo.dispatchEvent(new Event('input', { bubbles: true }));
                campo.dispatchEvent(new Event('change', { bubbles: true }));
                campo.dispatchEvent(new Event('blur', { bubbles: true }));
                return true;
              };
              window.__vbTimer = setInterval(function () {
                var campo = document.querySelector('input[formcontrolname="codiceEsercente"]');
                if (campo && !campo.value && document.activeElement !== campo && window.__vbEsercente) {
                  window.__vbScrivi('codiceEsercente', window.__vbEsercente);
                }
                if (window.__vbBuono && window.__vbScrivi('codiceVoucher', window.__vbBuono)) {
                  window.__vbBuono = null;
                }
              }, 400);
            })();
        """.trimIndent()
        web.evaluateJavascript(script, null)
    }

    private fun leggiCodice() {
        val opzioni = GmsBarcodeScannerOptions.Builder().enableAutoZoom().build()
        GmsBarcodeScanning.getClient(this, opzioni)
            .startScan()
            .addOnSuccessListener { codice -> gestisciLettura(codice.rawValue.orEmpty().trim()) }
            .addOnFailureListener { errore ->
                Toast.makeText(this, "Lettura non riuscita: ${errore.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Il codice buono è di 8 caratteri. Se il QR contiene esattamente quello lo uso
     * subito; se contiene altro (per esempio un indirizzo) propongo il candidato
     * trovato e lo lascio correggere prima di scriverlo nella pagina.
     */
    private fun gestisciLettura(testo: String) {
        if (testo.isEmpty()) return
        if (CODICE_ESATTO.matches(testo)) {
            scriviCodiceBuono(testo)
            return
        }
        val candidati = CODICE_NEL_TESTO.findAll(testo).map { it.value }.toList()
        val campo = EditText(this).apply {
            setText(if (candidati.size == 1) candidati[0] else testo)
            textSize = 20f
        }
        AlertDialog.Builder(this)
            .setTitle("Controlla il codice buono")
            .setMessage("Contenuto letto:\n$testo")
            .setView(campo)
            .setPositiveButton("Usa") { _, _ ->
                val scelto = campo.text.toString().trim()
                if (scelto.isNotEmpty()) scriviCodiceBuono(scelto)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun scriviCodiceBuono(codice: String) {
        iniettaAiuto()
        web.evaluateJavascript("window.__vbBuono = ${JSONObject.quote(codice)};", null)
    }

    @Deprecated("Sufficiente per il livello di sistema a cui è destinata l'app")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    companion object {
        const val EXTRA_CARTA = "carta"
        private val CODICE_ESATTO = Regex("^[A-Za-z0-9]{8}$")
        private val CODICE_NEL_TESTO = Regex("(?<![A-Za-z0-9])[A-Za-z0-9]{8}(?![A-Za-z0-9])")
    }
}
