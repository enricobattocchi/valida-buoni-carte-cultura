package it.lopo.validabuonicartecultura

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
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
    private lateinit var avviso: TextView
    private lateinit var errore: View
    private var paginaPronta = false
    private var erroreNelCaricamento = false

    /**
     * Codice buono letto e non ancora scritto nel modulo. L'app lo tiene solo qui:
     * la pagina riceve il comando di scriverlo e risponde subito se c'è riuscita.
     */
    private var buonoInAttesa: String? = null

    /** Fino a quando cercare il campo del codice buono sul modulo aperto; 0 se non si cerca. */
    private var scadenzaBuono = 0L

    /**
     * Cresce a ogni cambio di pagina. La pagina risponde ai comandi dopo un po':
     * una risposta chiesta su una pagina precedente viene ignorata, perché la
     * pagina nuova fa già il proprio controllo.
     */
    private var numeroPagina = 0

    /** Avvisi della pagina aperta, ricalcolati a ogni cambio di pagina. */
    private var avvisoBuono: String? = null
    private var campiMancanti = false

    private val controlloCampi = Runnable { controllaCampi() }
    private val tentativoBuono = Runnable { provaAScrivereBuono() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        carta = Carta.valueOf(intent.getStringExtra(EXTRA_CARTA) ?: Carta.DOCENTE.name)
        title = carta.nome

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (!request.isForMainFrame) return false
                    val indirizzo = request.url
                    val schema = indirizzo.scheme?.lowercase()
                    if (schema == "about" || schema == "javascript") return false
                    if (carta.restaNellApp(indirizzo.scheme, indirizzo.host)) return false
                    apriFuori(indirizzo)
                    return true
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    paginaPronta = false
                    erroreNelCaricamento = false
                    numeroPagina++
                }

                override fun doUpdateVisitedHistory(view: WebView, url: String, ricarica: Boolean) {
                    aggiornaIndietro()
                    paginaCambiata()
                }

                override fun onPageFinished(view: WebView, url: String) {
                    // Dopo un errore arriva comunque onPageFinished, per la pagina di errore di sistema.
                    if (erroreNelCaricamento) return
                    mostraErrore(false)
                    paginaPronta = true
                    iniettaAiuto()
                    paginaCambiata()
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    if (request.isForMainFrame) caricamentoFallito()
                }

                // Errori del server, per esempio 503 durante una manutenzione: senza questo
                // si vedrebbe la risposta grezza del server al posto della schermata con Riprova.
                // Conta solo la pagina principale: i portali caricano i dati a parte, e un
                // errore lì lo mostra il portale stesso.
                override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    errorResponse: WebResourceResponse
                ) {
                    if (request.isForMainFrame && errorResponse.statusCode >= 400) caricamentoFallito()
                }
            }
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

        avviso = testo("", 15f, R.color.avviso).apply {
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setBackgroundColor(colore(R.color.superficie))
            visibility = View.GONE
            // TalkBack legge l'avviso quando compare o cambia.
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        errore = paginaErrore()
        val area = FrameLayout(this).apply {
            addView(web, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            addView(errore, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        val pagina = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(caricamento)
            addView(avviso)
            addView(area, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
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

    private fun caricamentoFallito() {
        erroreNelCaricamento = true
        paginaPronta = false
        mostraErrore(true)
        campiMancanti = false
        aggiornaAvviso()
    }

    /**
     * La schermata di errore sostituisce la pagina di errore di sistema. Nascondo
     * anche la WebView, così TalkBack e la tastiera non raggiungono la pagina
     * coperta. La schermata resta finché un nuovo caricamento non riesce: durante
     * il tentativo si vede la barra di avanzamento, non la pagina di sistema.
     */
    private fun mostraErrore(visibile: Boolean) {
        errore.visibility = if (visibile) View.VISIBLE else View.GONE
        web.visibility = if (visibile) View.INVISIBLE else View.VISIBLE
    }

    /**
     * Il contenuto sta al centro quando c'è spazio e scorre quando non c'è, per
     * esempio in orizzontale o con i caratteri ingranditi.
     */
    private fun paginaErrore(): View = ScrollView(this).apply {
        isFillViewport = true
        setBackgroundColor(colore(R.color.sfondo))
        isClickable = true
        visibility = View.GONE
        // TalkBack annuncia la schermata quando compare.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) accessibilityPaneTitle = "Pagina non raggiunta"
        addView(contenutoErrore())
    }

    private fun contenutoErrore(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(32), dp(32), dp(32), dp(32))

        val riga = { margineDp: Int -> LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(margineDp) } }

        addView(testo("Pagina non raggiunta", 20f, grassetto = true).apply { gravity = Gravity.CENTER })
        addView(
            testo(
                "Il portale non risponde o non è disponibile. Controlla la connessione a Internet e riprova.",
                16f,
                R.color.testo_attenuato
            ).apply { gravity = Gravity.CENTER },
            riga(12)
        )
        addView(pulsante("Riprova", R.drawable.ic_ricarica) { web.reload() }, riga(24))
    }

    /** Apre nel browser, o nell'app adatta, gli indirizzi che non sono del ministero del portale. */
    private fun apriFuori(indirizzo: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, indirizzo).addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Nessuna app può aprire questo collegamento.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Installa nella pagina un piccolo controllo periodico per il codice esercente.
     * I due portali sono applicazioni Angular: i campi compaiono dopo il caricamento
     * e ricompaiono vuoti quando si torna al modulo, quindi un riempimento una
     * tantum non basta. Il controllo vive dentro la pagina, sparisce con lei a ogni
     * caricamento e costa una ricerca nella pagina ogni 400 ms.
     * Definisce anche window.__vbScrivi, che [provaAScrivereBuono] usa per il codice
     * buono. I campi si riconoscono dall'attributo formcontrolname, uguale sui due
     * siti: "codiceEsercente" e "codiceVoucher". Se mancano, [controllaCampi] lo segnala.
     */
    private fun iniettaAiuto() {
        if (!paginaPronta || !suPaginaDelPortale()) return

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
              }, 400);
            })();
        """.trimIndent()
        web.evaluateJavascript(script, null)
    }

    private fun suPaginaDelPortale(): Boolean {
        val hostCorrente = Uri.parse(web.url.orEmpty()).host
        return hostCorrente != null && hostCorrente == Uri.parse(carta.url).host
    }

    /** Vero se è aperta, e caricata, la pagina Valida Buono del portale. */
    private fun suModulo(): Boolean =
        paginaPronta && suPaginaDelPortale() &&
            Uri.parse(web.url.orEmpty()).fragment.orEmpty().contains("validaBuono", ignoreCase = true)

    /**
     * Chiamata a ogni cambio di pagina, anche quando il portale cambia pagina
     * senza ricaricarla. Gli avvisi riguardano la pagina aperta, quindi li azzero
     * e li ricalcolo: il controllo dei campi riparte e il codice buono in attesa,
     * se c'è, prova a entrare nel nuovo modulo.
     */
    private fun paginaCambiata() {
        numeroPagina++
        campiMancanti = false
        avvisoBuono = null
        aggiornaAvviso()
        programmaControlloCampi()
        provaAScrivereBuono()
    }

    /** Mostra l'avviso più importante: quello sul codice buono, poi quello sui campi. */
    private fun aggiornaAvviso() {
        val messaggio = avvisoBuono ?: if (campiMancanti) {
            "Non trovo i campi del modulo: forse il portale è cambiato. " +
                "Inserisci i codici a mano e segnala il problema."
        } else {
            null
        }
        avviso.text = messaggio.orEmpty()
        avviso.visibility = if (messaggio == null) View.GONE else View.VISIBLE
    }

    private fun programmaControlloCampi() {
        web.removeCallbacks(controlloCampi)
        web.postDelayed(controlloCampi, ATTESA_CAMPI_MS)
    }

    /**
     * Se dopo qualche secondo la pagina Valida Buono non ha i campi che l'app cerca,
     * il portale è probabilmente cambiato: lo dico, invece di non fare nulla in silenzio.
     */
    private fun controllaCampi() {
        if (isDestroyed || !suModulo()) return
        val numero = numeroPagina
        web.evaluateJavascript(
            "!!document.querySelector('input[formcontrolname=\"codiceEsercente\"]')" +
                " && !!document.querySelector('input[formcontrolname=\"codiceVoucher\"]')"
        ) { risultato ->
            if (isDestroyed || numeroPagina != numero) return@evaluateJavascript
            campiMancanti = risultato == "false"
            aggiornaAvviso()
        }
    }

    private fun scriviCodiceBuono(codice: String) {
        buonoInAttesa = codice
        scadenzaBuono = 0L
        provaAScrivereBuono()
    }

    /**
     * Prova a scrivere il codice buono in attesa nel modulo.
     * - Se la pagina Valida Buono non è aperta o non è pronta, il codice resta in
     *   attesa con un avviso e viene scritto al prossimo cambio di pagina.
     * - Sul modulo riprova per qualche secondo, il tempo che Angular mostri il campo.
     * - Se il campo non compare, copia il codice negli appunti da incollare a mano.
     * Una risposta vale solo se nel frattempo non è cambiata pagina e il codice in
     * attesa è ancora quello: una lettura nuova sostituisce la vecchia.
     */
    private fun provaAScrivereBuono() {
        web.removeCallbacks(tentativoBuono)
        if (isDestroyed) return
        val codice = buonoInAttesa ?: return
        if (!suModulo()) {
            scadenzaBuono = 0L
            avvisoBuono = "Il codice buono $codice verrà inserito appena è pronta la pagina Valida Buono."
            aggiornaAvviso()
            return
        }
        if (scadenzaBuono == 0L) scadenzaBuono = SystemClock.uptimeMillis() + ATTESA_BUONO_MS
        val numero = numeroPagina
        web.evaluateJavascript(
            "window.__vbScrivi ? window.__vbScrivi('codiceVoucher', ${JSONObject.quote(codice)}) : false"
        ) { risultato ->
            if (isDestroyed || numeroPagina != numero || buonoInAttesa != codice) return@evaluateJavascript
            when {
                risultato == "true" -> {
                    buonoInAttesa = null
                    scadenzaBuono = 0L
                    avvisoBuono = null
                }
                SystemClock.uptimeMillis() < scadenzaBuono -> {
                    web.postDelayed(tentativoBuono, INTERVALLO_TENTATIVI_MS)
                    return@evaluateJavascript
                }
                else -> {
                    buonoInAttesa = null
                    scadenzaBuono = 0L
                    getSystemService(ClipboardManager::class.java)
                        ?.setPrimaryClip(ClipData.newPlainText("Codice buono", codice))
                    avvisoBuono =
                        "Non trovo il campo del codice buono. Il codice $codice è negli appunti: incollalo a mano."
                }
            }
            aggiornaAvviso()
        }
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
     * Se il QR contiene solo il codice buono lo uso subito; se contiene altro (per
     * esempio un indirizzo) propongo il candidato trovato e lo lascio correggere
     * prima di scriverlo nella pagina. Le regole sono in [CodiceBuono].
     */
    private fun gestisciLettura(testo: String) {
        val proposta = when (val lettura = CodiceBuono.leggi(testo)) {
            null -> return
            is CodiceBuono.Lettura.Esatto -> {
                scriviCodiceBuono(lettura.codice)
                return
            }
            is CodiceBuono.Lettura.DaControllare -> lettura.proposta
        }
        val campo = EditText(this).apply {
            setText(proposta)
            textSize = 20f
            typeface = Typeface.MONOSPACE
            setTextColor(colore(R.color.testo))
            background = sfondoCampo()
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        mostraFinestra { finestra ->
            val riga = { margineDp: Int -> LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(margineDp) } }

            addView(testo("Controlla il codice buono", 20f, grassetto = true))
            addView(testo("Contenuto letto:\n$testo", 15f, R.color.testo_attenuato), riga(12))
            addView(campo, riga(16))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(
                    pulsante("Annulla", primario = false) { finestra.dismiss() },
                    LinearLayout.LayoutParams(0, dp(56), 1f)
                )
                addView(
                    pulsante("Usa") {
                        val scelto = campo.text.toString().trim()
                        finestra.dismiss()
                        if (scelto.isNotEmpty()) scriviCodiceBuono(scelto)
                    },
                    LinearLayout.LayoutParams(0, dp(56), 1f).apply { marginStart = dp(10) }
                )
            }, riga(20))
        }
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
        web.removeCallbacks(controlloCampi)
        web.removeCallbacks(tentativoBuono)
        web.destroy()
        super.onDestroy()
    }

    /**
     * Da Android 13 il tasto Indietro passa da OnBackInvokedDispatcher e, con
     * targetSdk 36, onBackPressed non viene più chiamato. Registro il callback solo
     * quando la pagina ha una pagina precedente a cui tornare: altrimenti Indietro
     * chiude la schermata con l'animazione di sistema.
     */
    private fun aggiornaIndietro() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val serve = passiIndietro() != null
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
        OnBackInvokedCallback { tornaIndietro() }
    }
    private var indietroRegistrato = false

    /**
     * Quanti passi indietro servono per arrivare a una pagina diversa da quella
     * aperta, o null se non ce n'è una. Salta le copie della pagina aperta, che
     * Ricarica aggiunge alla cronologia, e gli indirizzi che finiscono in "#/":
     * su entrambi i portali quella pagina rimanda subito alla Home, quindi
     * tornarci riporterebbe sempre alla Home.
     */
    private fun passiIndietro(): Int? {
        val cronologia = web.copyBackForwardList()
        val corrente = cronologia.currentItem?.url ?: return null
        for (indice in cronologia.currentIndex - 1 downTo 0) {
            val url = cronologia.getItemAtIndex(indice).url
            if (url != corrente && Uri.parse(url).fragment != "/") {
                return indice - cronologia.currentIndex
            }
        }
        return null
    }

    private fun tornaIndietro() {
        val passi = passiIndietro()
        if (passi != null) web.goBackOrForward(passi) else finish()
    }

    /** Usato solo fino ad Android 12. */
    @Deprecated("Sostituito da OnBackInvokedDispatcher da Android 13")
    override fun onBackPressed() {
        if (passiIndietro() != null) tornaIndietro() else super.onBackPressed()
    }

    companion object {
        const val EXTRA_CARTA = "carta"
        private const val ATTESA_CAMPI_MS = 6_000L
        private const val ATTESA_BUONO_MS = 3_000L
        private const val INTERVALLO_TENTATIVI_MS = 400L
    }
}
