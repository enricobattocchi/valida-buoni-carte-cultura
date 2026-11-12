# Valida Buoni

App Android che apre la pagina reale "Valida Buono" di Carta del Docente e delle
Carte Cultura Giovani e Merito, precompila il codice esercente salvato nelle
Impostazioni e riempie il codice buono leggendo un QR o un codice a barre.

## Compilare

- Con Android Studio: apri la cartella, attendi la sincronizzazione, poi Run.
- Da riga di comando: `./gradlew assembleDebug`, il file risultante è
  `app/build/outputs/apk/debug/app-debug.apk`.
- Con GitHub: carica il progetto in un repository; il flusso "Compila APK"
  produce il file scaricabile dalla pagina Actions.

## Se i portali cambiano

Indirizzi in `Carta.kt`; nomi dei campi (`codiceEsercente`, `codiceVoucher`)
in `ValidaActivity.kt`.
