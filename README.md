# Valida Buoni Carte Cultura

App Android che apre la pagina reale "Valida Buono" di Carta del Docente e delle
Carte Cultura Giovani e Merito, precompila il codice esercente salvato nelle
Impostazioni e riempie il codice buono leggendo un QR o un codice a barre.

## Compilare

- Con Android Studio: apri la cartella, attendi la sincronizzazione, poi Run.
- Da riga di comando: `./gradlew assembleDebug`, il file risultante è
  `app/build/outputs/apk/debug/app-debug.apk`.
- Con GitHub: il flusso "Compila APK" parte a ogni push su `main` e a ogni
  pull request, esegue i test e produce il file scaricabile dalla pagina Actions.
- Test: `./gradlew test` esegue gli unit test della logica che non dipende da
  Android.

## Se i portali cambiano

Indirizzi e domini in `Carta.kt`; nomi dei campi (`codiceEsercente`,
`codiceVoucher`) in `ValidaActivity.kt`. Se la pagina Valida Buono non ha più
quei campi, l'app mostra un avviso invece di non fare nulla.

## Licenza

Copyright (C) 2026 Enrico Battocchi.

Distribuito con licenza GNU General Public License versione 3: vedi il file
[LICENSE](LICENSE). App non ufficiale, non collegata al Ministero
dell'Istruzione e del Merito né al Ministero della Cultura.
