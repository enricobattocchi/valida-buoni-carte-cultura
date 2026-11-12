package it.lopo.validabuonicartecultura

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    /** Riga di stato di ogni scheda, aggiornata al ritorno dalle Impostazioni. */
    private val stati = mutableMapOf<Carta, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(24))
        }

        radice.addView(intestazione())
        radice.addView(
            testo("Scegli la carta per validare un buono", 16f, R.color.testo_attenuato).apply {
                setPadding(0, dp(24), 0, dp(16))
            }
        )

        for (carta in Carta.entries) {
            radice.addView(scheda(carta), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) })
        }

        radice.addView(
            pulsante("Impostazioni", primario = false) {
                startActivity(Intent(this, SettingsActivity::class.java))
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        )

        // Lo spazio elastico spinge il piè di pagina in fondo allo schermo.
        radice.addView(android.view.View(this), LinearLayout.LayoutParams(0, 0, 1f))
        radice.addView(piePagina(), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(32) })

        setContentView(schermata(null, ScrollView(this).apply {
            isFillViewport = true
            addView(radice)
        }))
    }

    override fun onResume() {
        super.onResume()
        for ((carta, stato) in stati) {
            val codice = Impostazioni.codiceEsercente(this, carta)
            if (codice.isEmpty()) {
                stato.text = "Codice esercente da impostare"
                stato.setTextColor(colore(R.color.avviso))
            } else {
                stato.text = "Esercente $codice"
                stato.setTextColor(colore(R.color.testo_attenuato))
            }
        }
    }

    private fun versione(): String =
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

    private fun piePagina() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        // In un LinearLayout verticale i figli occupano di default tutta la larghezza:
        // servono parametri espliciti perché la centratura abbia effetto.
        val avvolgi = { LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ) }
        addView(testo("$NOME ${versione()} · App non ufficiale", 13f, R.color.testo_attenuato), avvolgi())
        addView(testo("Informazioni", 14f, R.color.primario, grassetto = true).apply {
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = sfondoTattile(colore(R.color.sfondo), 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { mostraInformazioni() }
        }, avvolgi().apply { topMargin = dp(4) })
    }

    /**
     * Finestra Informazioni disegnata in codice: il dialogo di sistema mette i tre
     * pulsanti uno sotto l'altro e ha uno sfondo grigio estraneo ai colori dell'app.
     */
    private fun mostraInformazioni() {
        val finestra = Dialog(this)
        val avvolgi = { LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ) }
        val riga = { margineDp: Int -> LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(margineDp) } }

        val contenuto = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(12))
            background = GradientDrawable().apply {
                cornerRadius = dp(28).toFloat()
                setColor(colore(R.color.superficie))
            }
        }

        contenuto.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_launcher)
                background = GradientDrawable().apply { cornerRadius = dp(12).toFloat() }
                clipToOutline = true
            }, LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), 0, 0, 0)
                addView(testo(NOME, 18f, grassetto = true))
                addView(testo("Versione ${versione()}", 14f, R.color.testo_attenuato))
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        })

        contenuto.addView(testo(
            "Apre la pagina ufficiale «Valida Buono» di Carta del Docente " +
                "(cartadeldocente.istruzione.it) e di Carte Cultura Giovani e Merito " +
                "(cartegiovani.cultura.gov.it), inserisce il codice esercente salvato e " +
                "legge il codice del buono da un QR o da un codice a barre.",
            16f
        ), riga(20))
        contenuto.addView(testo(
            "La conferma della validazione resta sempre a te, sulla pagina del portale. " +
                "I codici esercente restano solo su questo telefono.",
            16f
        ), riga(12))
        contenuto.addView(testo(
            "App non ufficiale, non collegata al Ministero dell'Istruzione e del Merito " +
                "né al Ministero della Cultura.\n\nRealizzata da $AUTORE.\nLicenza GPL-3.0.",
            14f,
            R.color.testo_attenuato
        ), riga(12))

        contenuto.addView(pulsante("Informativa sulla privacy", primario = false) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY)))
        }, riga(20))
        contenuto.addView(pulsante("Codice sorgente su GitHub", primario = false) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(REPOSITORY)))
        }, riga(8))

        contenuto.addView(testo("Chiudi", 16f, R.color.primario, grassetto = true).apply {
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = sfondoTattile(colore(R.color.superficie), 12)
            isClickable = true
            isFocusable = true
            setOnClickListener { finestra.dismiss() }
        }, avvolgi().apply {
            topMargin = dp(8)
            gravity = Gravity.END
        })

        finestra.setContentView(ScrollView(this).apply { addView(contenuto) })
        finestra.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val larghezza = (resources.displayMetrics.widthPixels - dp(48)).coerceAtMost(dp(480))
            setLayout(larghezza, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        finestra.show()
    }

    companion object {
        private const val NOME = "Valida Buoni Carte Cultura"
        private const val AUTORE = "Enrico Battocchi"
        private const val REPOSITORY = "https://github.com/enricobattocchi/valida-buoni-carte-cultura"
        private const val PRIVACY = "https://enricobattocchi.github.io/valida-buoni-carte-cultura/"
    }

    private fun intestazione() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(ImageView(context).apply {
            setImageResource(R.drawable.ic_launcher)
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat() }
            clipToOutline = true
        }, LinearLayout.LayoutParams(dp(52), dp(52)))
        addView(testo(NOME, 26f, grassetto = true).apply {
            setPadding(dp(16), 0, 0, 0)
        })
    }

    private fun scheda(carta: Carta) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(20), dp(12), dp(20))
        background = sfondoTattile(colore(R.color.superficie), 20, colore(R.color.bordo))
        isClickable = true
        isFocusable = true
        setOnClickListener {
            startActivity(
                Intent(context, ValidaActivity::class.java)
                    .putExtra(ValidaActivity.EXTRA_CARTA, carta.name)
            )
        }

        val (sigla, tinta) = when (carta) {
            Carta.DOCENTE -> "CD" to R.color.carta_docente
            Carta.GIOVANI -> "CG" to R.color.carta_giovani
        }
        addView(TextView(context).apply {
            text = sigla
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(colore(android.R.color.white))
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(colore(tinta))
            }
        }, LinearLayout.LayoutParams(dp(52), dp(52)))

        val testi = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), 0, dp(8), 0)
            addView(testo(carta.nome, 18f, grassetto = true))
            addView(testo("", 14f).apply {
                setPadding(0, dp(4), 0, 0)
                stati[carta] = this
            })
        }
        addView(testi, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        addView(testo("›", 32f, R.color.testo_attenuato))
    }
}
