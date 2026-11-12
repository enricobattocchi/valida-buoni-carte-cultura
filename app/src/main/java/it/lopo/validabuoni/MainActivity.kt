package it.lopo.validabuoni

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val margine = dp(24)
        val radice = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(margine, margine, margine, margine)
        }

        for (carta in Carta.values()) {
            radice.addView(pulsante(carta.nome, grande = true) {
                startActivity(
                    Intent(this, ValidaActivity::class.java)
                        .putExtra(ValidaActivity.EXTRA_CARTA, carta.name)
                )
            })
        }
        radice.addView(pulsante("Impostazioni", grande = false) {
            startActivity(Intent(this, SettingsActivity::class.java))
        })

        setContentView(radice)
    }

    private fun pulsante(testo: String, grande: Boolean, azione: () -> Unit): Button =
        Button(this).apply {
            text = testo
            isAllCaps = false
            textSize = if (grande) 20f else 16f
            setOnClickListener { azione() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(if (grande) 96 else 56)
            ).apply { bottomMargin = dp(16) }
        }
}
