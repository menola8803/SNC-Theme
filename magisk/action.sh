#!/system/bin/sh
MODDIR=${0%/*}
echo "SNC Tema – klocktypsnitt"
F="$MODDIR/system/product/etc/fonts_customization.xml"
if [ -f "$F" ]; then
  echo "- Modulens fil: $(grep -c SNC-Cinzel.ttf "$F") omdirigeringar"
else
  echo "- Modulen ändrar inte fonts_customization.xml"
fi
echo "- Aktiv fil på telefonen: $(grep -c SNC-Cinzel.ttf /product/etc/fonts_customization.xml 2>/dev/null) omdirigeringar"
ls -la /product/fonts/SNC-Cinzel.ttf 2>&1
echo "Om siffran är 0: starta om telefonen."
