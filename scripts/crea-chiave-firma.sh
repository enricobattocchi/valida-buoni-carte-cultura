#!/usr/bin/env bash
# Crea la chiave di firma delle release e la carica nei segreti del repository GitHub.
# Da lanciare una sola volta. La chiave resta in ~/.android-keys: salvala anche altrove,
# perché senza di essa non puoi più pubblicare aggiornamenti firmati allo stesso modo.
set -euo pipefail

CARTELLA="$HOME/.android-keys"
CHIAVE="$CARTELLA/valida-buoni-release.jks"
ALIAS="valida-buoni"

if [ -e "$CHIAVE" ]; then
  echo "La chiave $CHIAVE esiste già: non la sovrascrivo." >&2
  exit 1
fi

mkdir -p "$CARTELLA"
chmod 700 "$CARTELLA"
PASSWORD=$(openssl rand -base64 24 | tr -d '/+=')

keytool -genkeypair -keystore "$CHIAVE" -storetype PKCS12 -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$PASSWORD" -keypass "$PASSWORD" \
  -dname "CN=${NOME_FIRMA:-$(git config user.name)}, C=IT"
chmod 600 "$CHIAVE"

printf 'KEY_ALIAS=%s\nKEYSTORE_PASSWORD=%s\nKEY_PASSWORD=%s\n' "$ALIAS" "$PASSWORD" "$PASSWORD" \
  > "$CARTELLA/valida-buoni-release.txt"
chmod 600 "$CARTELLA/valida-buoni-release.txt"

base64 -w0 "$CHIAVE" | gh secret set KEYSTORE_BASE64
printf %s "$PASSWORD" | gh secret set KEYSTORE_PASSWORD
printf %s "$PASSWORD" | gh secret set KEY_PASSWORD
printf %s "$ALIAS" | gh secret set KEY_ALIAS

echo "Chiave creata in $CHIAVE, password in $CARTELLA/valida-buoni-release.txt."
echo "Segreti caricati su GitHub. Fai una copia di sicurezza di entrambi i file."
