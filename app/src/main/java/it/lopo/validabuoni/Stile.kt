package it.lopo.validabuoni

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
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
