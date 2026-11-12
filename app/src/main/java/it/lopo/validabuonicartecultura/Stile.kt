package it.lopo.validabuonicartecultura

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Elementi grafici comuni alle schermate, disegnati in codice come il resto dell'app. */

fun Context.colore(id: Int): Int = getColor(id)

private fun Context.forma(riempimento: Int, raggioDp: Int, bordo: Int?, spessoreDp: Int = 1) =
    GradientDrawable().apply {
        cornerRadius = dp(raggioDp).toFloat()
        setColor(riempimento)
        if (bordo != null) setStroke(dp(spessoreDp), bordo)
    }

/** Sfondo arrotondato con l'effetto al tocco. */
fun Context.sfondoTattile(riempimento: Int, raggioDp: Int = 16, bordo: Int? = null): Drawable =
    RippleDrawable(
        ColorStateList.valueOf(colore(R.color.increspatura)),
        forma(riempimento, raggioDp, bordo),
        forma(Color.WHITE, raggioDp, null)
    )

/** Sfondo dei campi di testo: il bordo diventa del colore primario quando il campo è attivo. */
fun Context.sfondoCampo(): Drawable = StateListDrawable().apply {
    addState(
        intArrayOf(android.R.attr.state_focused),
        forma(colore(R.color.superficie), 12, colore(R.color.primario), 2)
    )
    addState(intArrayOf(), forma(colore(R.color.superficie), 12, colore(R.color.bordo)))
}

fun Context.testo(
    contenuto: String,
    dimensione: Float,
    coloreId: Int = R.color.testo,
    grassetto: Boolean = false
): TextView = TextView(this).apply {
    text = contenuto
    textSize = dimensione
    setTextColor(colore(coloreId))
    if (grassetto) typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}

/**
 * Pulsante con icona facoltativa e testo centrati insieme. Il pulsante di sistema
 * mette l'icona sul bordo, lontana dal testo, quindi uso un contenitore cliccabile.
 */
fun Context.pulsante(
    etichetta: String?,
    icona: Int? = null,
    primario: Boolean = true,
    azione: () -> Unit
): LinearLayout {
    val coloreTesto = colore(if (primario) R.color.su_primario else R.color.primario)
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = dp(56)
        setPadding(dp(16), 0, dp(16), 0)
        background = if (primario) {
            sfondoTattile(colore(R.color.primario))
        } else {
            sfondoTattile(colore(R.color.superficie), bordo = colore(R.color.bordo))
        }
        isClickable = true
        isFocusable = true
        setOnClickListener { azione() }

        if (icona != null) {
            addView(ImageView(context).apply {
                setImageResource(icona)
                imageTintList = ColorStateList.valueOf(coloreTesto)
                contentDescription = if (etichetta == null) "Ricarica" else null
            }, LinearLayout.LayoutParams(dp(24), dp(24)))
        }
        if (etichetta != null) {
            addView(TextView(context).apply {
                text = etichetta
                textSize = 18f
                setTextColor(coloreTesto)
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                if (icona != null) setPadding(dp(12), 0, 0, 0)
            })
        }
    }
}

/**
 * Struttura comune delle schermate: barra del titolo facoltativa con il pulsante
 * Indietro, contenuto elastico e piede facoltativo.
 *
 * Da Android 15, con targetSdk 35 o superiore, l'app occupa tutto lo schermo anche
 * sotto le barre di sistema, e adjustResize non restringe più la finestra quando
 * compare la tastiera. Qui aggiungo quindi lo spazio della barra di stato in alto
 * e il maggiore tra barra di navigazione e tastiera in basso.
 */
fun Activity.schermata(titolo: String?, contenuto: View, piede: View? = null): View {
    val radice = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(colore(R.color.sfondo))
    }
    val barra = titolo?.let { barraTitolo(it) }
    barra?.let { radice.addView(it) }
    radice.addView(contenuto, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
    piede?.let { radice.addView(it) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        val inAlto = barra ?: radice
        val baseAlto = inAlto.paddingTop
        val baseLati = radice.paddingLeft
        radice.setOnApplyWindowInsetsListener { _, insets ->
            val sistema = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            val tastiera = insets.getInsets(WindowInsets.Type.ime())
            inAlto.setPadding(inAlto.paddingLeft, baseAlto + sistema.top, inAlto.paddingRight, inAlto.paddingBottom)
            radice.setPadding(
                baseLati + sistema.left,
                radice.paddingTop,
                baseLati + sistema.right,
                maxOf(sistema.bottom, tastiera.bottom)
            )
            WindowInsets.CONSUMED
        }
    }
    return radice
}

private fun Activity.barraTitolo(titolo: String) = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    setPadding(dp(4), dp(6), dp(16), dp(6))
    setBackgroundColor(colore(R.color.sfondo))

    addView(ImageView(context).apply {
        setImageResource(R.drawable.ic_indietro)
        imageTintList = ColorStateList.valueOf(colore(R.color.testo))
        scaleType = ImageView.ScaleType.CENTER
        contentDescription = "Indietro"
        background = sfondoTattile(colore(R.color.sfondo), 24)
        isClickable = true
        isFocusable = true
        setOnClickListener { finish() }
    }, LinearLayout.LayoutParams(dp(48), dp(48)))

    addView(testo(titolo, 20f, grassetto = true).apply {
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(dp(8), 0, 0, 0)
    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
}

/**
 * Finestra di dialogo con i colori e gli angoli arrotondati dell'app, al posto
 * del dialogo di sistema.
 */
fun Activity.mostraFinestra(costruisci: LinearLayout.(Dialog) -> Unit) {
    val finestra = Dialog(this)
    val contenuto = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(24), dp(24), dp(24), dp(16))
        background = GradientDrawable().apply {
            cornerRadius = dp(28).toFloat()
            setColor(colore(R.color.superficie))
        }
        costruisci(finestra)
    }
    finestra.setContentView(ScrollView(this).apply { addView(contenuto) })
    finestra.window?.apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val larghezza = (resources.displayMetrics.widthPixels - dp(48)).coerceAtMost(dp(480))
        setLayout(larghezza, WindowManager.LayoutParams.WRAP_CONTENT)
    }
    finestra.show()
}
