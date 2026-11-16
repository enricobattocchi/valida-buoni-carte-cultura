# ML Kit crea i suoi registrar per riflessione, partendo dal nome scritto nel manifest.
# La regola di firebase-components conserva solo il nome della classe: in modalità
# completa R8 toglie il costruttore senza parametri e il lettore QR va in crash.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    <init>();
}
