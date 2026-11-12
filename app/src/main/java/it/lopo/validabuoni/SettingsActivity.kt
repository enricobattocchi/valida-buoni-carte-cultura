package it.lopo.validabuoni

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        radice.addView(
            testo(
                "L'app inserisce questi codici nella pagina \"Valida Buono\" del portale.",
                15f,
                R.color.testo_attenuato
            ).apply { setPadding(0, 0, 0, dp(24)) }
        )

        val campi = Carta.entries.associateWith { carta ->
            radice.addView(testo(carta.nome, 17f, grassetto = true))
            radice.addView(
                testo("Codice esercente, 5 caratteri", 14f, R.color.testo_attenuato).apply {
                    setPadding(0, dp(2), 0, dp(8))
                }
            )
            EditText(this).apply {
                setText(Impostazioni.codiceEsercente(this@SettingsActivity, carta))
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                // Sui due portali il codice esercente è di 5 caratteri.
                filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(5))
                textSize = 22f
                letterSpacing = 0.15f
                typeface = Typeface.MONOSPACE
                setTextColor(colore(R.color.testo))
                background = sfondoCampo()
                setPadding(dp(16), dp(14), dp(16), dp(14))
                radice.addView(this, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(28) })
            }
        }

        radice.addView(pulsante("Salva") {
            for ((carta, campo) in campi) {
                Impostazioni.salvaCodiceEsercente(this, carta, campo.text.toString())
            }
            Toast.makeText(this, "Codici salvati", Toast.LENGTH_SHORT).show()
            finish()
        })

        setContentView(ScrollView(this).apply { addView(radice) })
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}
