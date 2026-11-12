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

## Rilascio

Il flusso "Rilascio" crea una release su GitHub con l'APK firmato e il bundle
AAB per il Play Store ogni volta che carichi un tag di versione:

```
git tag v1.1.0
git push origin v1.1.0
```

Il numero di versione Android deriva dal tag: `v1.2.3` diventa 10203.

Prima del primo rilascio, crea la chiave di firma e caricala nei segreti del
repository:

```
./scripts/crea-chiave-firma.sh
```

Lo script salva la chiave e la password in `~/.android-keys`. Fai una copia di
sicurezza dei due file: chi installa l'app può aggiornarla solo con versioni
firmate dalla stessa chiave.

## Se i portali cambiano

Indirizzi in `Carta.kt`; nomi dei campi (`codiceEsercente`, `codiceVoucher`)
in `ValidaActivity.kt`.

## Licenza

Copyright (C) 2026 Enrico Battocchi.

Distribuito con licenza GNU General Public License versione 3: vedi il file
[LICENSE](LICENSE). App non ufficiale, non collegata al Ministero
dell'Istruzione e del Merito né al Ministero della Cultura.
