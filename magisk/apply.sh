#!/system/bin/sh
# Sätter systemets färgtema (Material You) till SNC-guld.
KEY=theme_customization_overlay_packages
BACKUP=/data/adb/snc_theme_original.txt
GOLD=DDAE54
NAVY=011634
[ -f "$BACKUP" ] || settings get secure "$KEY" > "$BACKUP"
TS="$(date +%s)000"
JSON="{\"android.theme.customization.system_palette\":\"$NAVY\",\"android.theme.customization.accent_color\":\"$GOLD\",\"android.theme.customization.theme_style\":\"VIBRANT\",\"android.theme.customization.color_source\":\"preset\",\"android.theme.customization.color_both\":\"1\",\"android.theme.customization.color_index\":\"-1\",\"_applied_timestamp\":$TS}"
settings put secure "$KEY" "$JSON"
