#!/usr/bin/env python3
"""Adatta una cattura dello schermo al formato richiesto dal Play Store.

Uso:
    python3 scripts/adatta-screenshot.py ORIGINALE.png DESTINAZIONE.png [--copri-logo]

Passi, uguali per ogni immagine:
  1. toglie la barra di stato (può mostrare notifiche e icone personali);
  2. con --copri-logo copre con una banda blu la parte alta del portale, dove
     compaiono il nome del Ministero e il logo di Carta del Docente;
  3. ridimensiona senza deformare a 1080x2160 (rapporto 2:1, il massimo accettato
     dal Play Store) e aggiunge ai lati una cornice del colore di sfondo dell'app;
  4. toglie la trasparenza e salva in PNG.

Le misure sono in pixel di una cattura 1240x2772 (Oppo/OnePlus CPH2663). Con un
altro telefono servono altre misure: controlla il risultato prima di usarlo.

Richiede Pillow (pip install pillow, meglio in un ambiente virtuale).
"""
import sys

from PIL import Image, ImageDraw

BARRA_DI_STATO = 150            # pixel da togliere in alto
LOGO_DA, LOGO_A = 360, 980      # fascia del portale da coprire (prima del taglio)
SFONDO_APP = (243, 244, 249)    # colore di sfondo della Home dell'app
LARGHEZZA, ALTEZZA = 1080, 2160


def main(argv):
    copri = "--copri-logo" in argv
    percorsi = [a for a in argv[1:] if not a.startswith("--")]
    if len(percorsi) != 2:
        sys.exit(__doc__)
    origine, destinazione = percorsi

    img = Image.open(origine).convert("RGB")

    if copri:
        # Prende il blu dalla fascia "Valida Buono", subito sotto la zona da coprire
        # (la zona arriva fino all'ombra che separa le due fasce).
        blu = img.getpixel((20, LOGO_A + 10))
        ImageDraw.Draw(img).rectangle([0, LOGO_DA, img.width, LOGO_A], fill=blu)

    img = img.crop((0, BARRA_DI_STATO, img.width, img.height))

    scala = ALTEZZA / img.height
    img = img.resize((round(img.width * scala), ALTEZZA), Image.LANCZOS)

    tela = Image.new("RGB", (LARGHEZZA, ALTEZZA), SFONDO_APP)
    tela.paste(img, ((LARGHEZZA - img.width) // 2, 0))
    tela.save(destinazione, "PNG")
    print(f"{destinazione}: {tela.width}x{tela.height}")


if __name__ == "__main__":
    main(sys.argv)
