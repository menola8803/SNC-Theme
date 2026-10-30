SKIPUNZIP=0
SRC=/product/etc/fonts_customization.xml
DST="$MODPATH/system/product/etc/fonts_customization.xml"
ui_print "- SNC Tema: klocktypsnitt Cinzel"
if [ ! -f "$SRC" ]; then
  ui_print "! Hittar inte $SRC, typsnittet ändras inte"
  rm -rf "$MODPATH/system/product/etc"
else
  mkdir -p "$(dirname "$DST")"
  cp -f "$SRC" "$DST"
  for FAM in google-sans-clock google-sans-flex-clock; do
    sed -i "/name=\"$FAM\"/,/<\/family>/{s#GoogleSansFlex-Regular\.ttf#SNC-Cinzel.ttf#;/<axis tag=\"ROND\"/d}" "$DST"
  done
  N=$(grep -c 'SNC-Cinzel.ttf' "$DST" 2>/dev/null); N=${N:-0}
  OPEN_A=$(grep -c '<family' "$SRC"); OPEN_B=$(grep -c '<family' "$DST")
  CLOSE_A=$(grep -c '</family>' "$SRC"); CLOSE_B=$(grep -c '</family>' "$DST")
  if [ "$N" -ge 1 ] && [ "$OPEN_A" = "$OPEN_B" ] && [ "$CLOSE_A" = "$CLOSE_B" ]; then
    ui_print "- Klockfamiljer omdirigerade till Cinzel: $N"
  else
    ui_print "! Kunde inte ändra filen säkert, avbryter typsnittsbytet"
    rm -rf "$MODPATH/system/product/etc"
  fi
fi
ui_print "- Starta om telefonen för att se den nya klockan"
set_perm_recursive "$MODPATH" 0 0 0755 0644
