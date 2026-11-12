package it.lopo.validabuonicartecultura

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
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
    private var paginaPronta = false
    private var buonoDaScrivere: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        carta = Carta.valueOf(intent.getStringExtra(EXTRA_CARTA) ?: Carta.DOCENTE.name)
        title = carta.nome

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    paginaPronta = false
                }

                override fun doUpdateVisitedHistory(view: WebView, url: String, ricarica: Boolean) {
                    aggiornaIndietro()
                }

                override fun onPageFinished(view: WebView, url: String) {
                    paginaPronta = true
                    iniettaAiuto()
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.isForMainFrame) {
                        Toast.makeText(
                            this@ValidaActivity,
                            "Impossibile caricare la pagina. Controlla la connessione e tocca Ricarica.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val caricamento = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progressTintList = ColorStateList.valueOf(colore(R.color.primario))
            progressBackgroundTintList = ColorStateList.valueOf(colore(R.color.sfondo))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(3))
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, percentuale: Int) {
                caricamento.progress = percentuale
                caricamento.visibility = if (percentuale < 100) View.VISIBLE else View.INVISIBLE
            }
        }

        val barra = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(colore(R.color.superficie))
            elevation = dp(8).toFloat()
        }
        barra.addView(
            pulsante("Leggi QR code", R.drawable.ic_qr) { leggiCodice() },
            LinearLayout.LayoutParams(0, dp(56), 1f)
        )
        barra.addView(
            pulsante(null, R.drawable.ic_ricarica, primario = false) { web.loadUrl(carta.url) },
            LinearLayout.LayoutParams(dp(56), dp(56)).apply { marginStart = dp(10) }
        )

        val pagina = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(caricamento)
            addView(web)
        }
        setContentView(schermata(carta.nome, pagina, barra))

        if (Impostazioni.codiceEsercente(this, carta).isEmpty()) {
            Toast.makeText(
                this,
                "Nessun codice esercente salvato per questa carta: aggiungilo nelle Impostazioni.",
                Toast.LENGTH_LONG
            ).show()
        }

        web.loadUrl(carta.url)
    }

    /**
     * Installa nella pagina un piccolo controllo periodico. I due portali sono
     * applicazioni Angular: i campi compaiono dopo il caricamento e ricompaiono
     * vuoti quando si torna al modulo, quindi un riempimento una tantum non basta.
     * I campi si riconoscono dall'attributo formcontrolname, uguale sui due siti:
     * "codiceEsercente" e "codiceVoucher".
     */
    private fun iniettaAiuto() {
        if (!paginaPronta) return
        val hostCorrente = Uri.parse(web.url.orEmpty()).host
        val hostAtteso = Uri.parse(carta.url).host
        if (hostCorrente == null || hostCorrente != hostAtteso) return

        val codice = JSONObject.quote(Impostazioni.codiceEsercente(this, carta))
        val buono = buonoDaScrivere?.let { "window.__vbBuono = ${JSONObject.quote(it)};" }.orEmpty()
        buonoDaScrivere = null

        val script = """
            (function () {
              window.__vbEsercente = $codice;
              $buono
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
            typeface = Typeface.MONOSPACE
            setTextColor(colore(R.color.testo))
            background = sfondoCampo()
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        val contenitore = FrameLayout(this).apply {
            setPadding(dp(20), dp(12), dp(20), dp(4))
            addView(campo)
        }
        AlertDialog.Builder(this)
            .setTitle("Controlla il codice buono")
            .setMessage("Contenuto letto:\n$testo")
            .setView(contenitore)
            .setPositiveButton("Usa") { _, _ ->
                val scelto = campo.text.toString().trim()
                if (scelto.isNotEmpty()) scriviCodiceBuono(scelto)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun scriviCodiceBuono(codice: String) {
        buonoDaScrivere = codice
        iniettaAiuto()
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }

    /**
     * Da Android 13 il tasto Indietro passa da OnBackInvokedDispatcher e, con
     * targetSdk 36, onBackPressed non viene più chiamato. Registro il callback solo
     * quando la pagina ha una cronologia: altrimenti Indietro chiude la schermata
     * con l'animazione di sistema.
     */
    private fun aggiornaIndietro() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val serve = web.canGoBack()
        if (serve == indietroRegistrato) return
        val dispatcher = onBackInvokedDispatcher
        if (serve) {
            dispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, indietroNellaPagina)
        } else {
            dispatcher.unregisterOnBackInvokedCallback(indietroNellaPagina)
        }
        indietroRegistrato = serve
    }

    private val indietroNellaPagina by lazy {
        OnBackInvokedCallback { web.goBack() }
    }
    private var indietroRegistrato = false

    /** Usato solo fino ad Android 12. */
    @Deprecated("Sostituito da OnBackInvokedDispatcher da Android 13")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    companion object {
        const val EXTRA_CARTA = "carta"
        private val CODICE_ESATTO = Regex("^[A-Za-z0-9]{8}$")
        private val CODICE_NEL_TESTO = Regex("(?<![A-Za-z0-9])[A-Za-z0-9]{8}(?![A-Za-z0-9])")
    }
}
