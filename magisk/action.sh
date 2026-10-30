#!/system/bin/sh
MODDIR=${0%/*}
echo "SNC Tema"
echo "- Tillämpar guldfärger..."
sh "$MODDIR/apply.sh"
echo "- Klart. Färgerna byts inom några sekunder."
OUT=/sdcard/Download/snc-diagnostik.txt
{
  echo "== SNC diagnostik $(date) =="
  echo "== build =="; getprop ro.lineage.build.version; getprop ro.build.version.release; getprop ro.product.model
  echo "== theme =="; settings get secure theme_customization_overlay_packages
  echo "== secure clock/lock =="; settings list secure | grep -i -E "clock|lockscreen|lock_screen|font"
  echo "== system clock/font =="; settings list system | grep -i -E "clock|font"
  echo "== overlays (font/clock/theme) =="; cmd overlay list | grep -i -E "font|clock|theme"
  echo "== font families =="
  for f in /system/etc/font_fallback.xml /system/etc/fonts.xml /product/etc/fonts_customization.xml; do
    [ -f "$f" ] && { echo "-- $f"; grep -o 'family[^>]*name="[^"]*"' "$f" | sort -u; }
  done
  echo "== clock fonts in /system/fonts and /product/fonts =="
  ls /system/fonts /product/fonts 2>/dev/null | grep -i -E "clock|flex|sans|lineage"
  echo "== packages =="; pm list packages | grep -i -E "clock|systemui|theme|font|lineage"
} > "$OUT" 2>&1
echo "- Diagnostik sparad i Download/snc-diagnostik.txt"
echo "  Skicka filen till Claude."
