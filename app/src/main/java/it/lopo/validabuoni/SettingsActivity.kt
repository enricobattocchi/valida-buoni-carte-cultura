package it.lopo.validabuoni

import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val margine = dp(24)
        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(margine, margine, margine, margine)
        }

        val campi = Carta.values().associateWith { carta ->
            radice.addView(TextView(this).apply {
                text = "Codice esercente: ${carta.nome}"
                textSize = 16f
            })
            EditText(this).apply {
                setText(Impostazioni.codiceEsercente(this@SettingsActivity, carta))
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                // Sui due portali il codice esercente è di 5 caratteri.
                filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(5))
                textSize = 22f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(24) }
                radice.addView(this)
            }
        }

        radice.addView(Button(this).apply {
            text = "Salva"
            isAllCaps = false
            textSize = 18f
            setOnClickListener {
                for ((carta, campo) in campi) {
                    Impostazioni.salvaCodiceEsercente(
                        this@SettingsActivity, carta, campo.text.toString()
                    )
                }
                Toast.makeText(this@SettingsActivity, "Codici salvati", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        setContentView(radice)
    }
}
