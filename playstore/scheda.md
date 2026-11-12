# Scheda Play Store – Valida Buoni

Questo file raccoglie tutti i testi e le risposte ai moduli della **Google Play Console** per pubblicare l'app.

---

## 1. Testi della scheda dello store (Italiano `it-IT`)

### Nome dell'app *(max 30 caratteri – 26 usati)*
```text
Valida Buoni Carte Cultura
```

### Breve descrizione *(max 80 caratteri – 76 usati)*
```text
Valida i buoni di Carta del Docente e Carte Cultura Giovani e Merito col QR.
```

### Descrizione completa *(max 4000 caratteri)*
```text
Valida Buoni aiuta gli esercenti a validare rapidamente i buoni di Carta del Docente e delle Carte Cultura Giovani e Merito senza dover digitare ogni volta a mano il codice esercente e il codice del buono.

COME FUNZIONA
• Salva una sola volta il tuo codice esercente (5 caratteri) nelle Impostazioni, uno per ciascun portale.
• Scegli la carta da validare: l'app apre la pagina pubblica ufficiale «Valida Buono» del portale e compila subito il tuo codice esercente.
• Tocca «Leggi QR code» per inquadrare il QR o il codice a barre del buono: il codice di 8 caratteri viene inserito automaticamente nel modulo. Se il QR contiene un indirizzo web o altro testo, l'app estrae il codice e te lo fa controllare prima di inserirlo.
• Controlla i dati sulla pagina ufficiale e conferma tu stesso la validazione.

RISPETTO DELLA PRIVACY
• Nessun permesso fotocamera richiesto: la lettura del QR usa il lettore di sistema di Google Play Services.
• I codici esercente restano salvati solo sul tuo telefono.
• Nessuna pubblicità, nessun tracciamento, nessun server intermedio.
• Codice sorgente aperto (licenza GPLv3): https://github.com/enricobattocchi/valida-buoni-carte-cultura

FONTI UFFICIALI E AVVISO DI NON AFFILIAZIONE
Questa è un'applicazione indipendente e NON UFFICIALE: non rappresenta alcun ente governativo e non è affiliata, collegata né autorizzata dal Ministero dell'Istruzione e del Merito né dal Ministero della Cultura.
L'applicazione apre esclusivamente le pagine pubbliche ufficiali di validazione dei rispettivi portali ministeriali:
• Carta del Docente (Ministero dell'Istruzione e del Merito): https://www.cartadeldocente.istruzione.it/docentiEsercente/#/validaBuono
• Carte Cultura Giovani e Merito (Ministero della Cultura): https://cartegiovani.cultura.gov.it/giovanimeritoEsercente/#/validaBuono
```

---

## 2. Indirizzo Privacy Policy (GitHub Pages)

Una volta attivato GitHub Pages sulla cartella `docs/` del branch `main`, l'URL da incollare nella Play Console (sezione **Contenuti dell'app → Norme sulla privacy**) è:

```text
https://enricobattocchi.github.io/valida-buoni-carte-cultura/
```

---

## 3. Risposte ai questionari della Play Console (Contenuti dell'app)

1. **Norme sulla privacy**:
   * URL: `https://enricobattocchi.github.io/valida-buoni-carte-cultura/`
2. **Annunci**:
   * «No, la mia app non contiene annunci»
3. **Accesso all'app**:
   * «Tutte le funzionalità sono disponibili senza restrizioni speciali» (la pagina «Valida Buono» è pubblica e non richiede login; per testare la precompilazione basta inserire un codice di prova di 5 caratteri come `ABCDE` nelle Impostazioni).
4. **Classificazione dei contenuti (IARC)**:
   * Categoria: *Utility, produttività, comunicazione o altro*
   * Rispondi **No** a tutte le domande su violenza, sessualità, linguaggio, sostanze, interazione tra utenti, condivisione posizione e acquisti.
5. **Pubblico di destinazione e contenuti**:
   * Fascia d'età di destinazione: **18 anni e oltre** (l'app è destinata agli esercenti commerciali).
   * «L'app potrebbe attirare involontariamente i bambini?» → **No**.
6. **Sicurezza dei dati (Data safety)**:
   * «La tua app raccoglie o condivide uno dei tipi di dati utente richiesti?» → **No**.
   * (I codici esercente restano solo sul dispositivo e non lasciano mai il telefono verso server dello sviluppatore).
7. **App governative**:
   * «La tua app è sviluppata da o per conto di un governo?» → **No**.
   * Nota importante: Google Play richiede che le app non governative che mostrano siti governativi indichino chiaramente nella descrizione e nell'app la non affiliazione e gli URL ufficiali delle fonti. Entrambi sono già presenti nella finestra *Informazioni* dell'app e nella *Descrizione completa* qui sopra.
8. **Funzionalità finanziarie**:
   * «La mia app non offre funzionalità finanziarie» (è uno strumento di utilità per compilare il modulo web pubblico di validazione buoni).
9. **Categoria e contatti dello store**:
   * Categoria: **Strumenti** (oppure **Economia / Affari**)
   * Email di contatto: `lopo@lopo.it`
   * Sito web (facoltativo): `https://github.com/enricobattocchi/valida-buoni-carte-cultura`
