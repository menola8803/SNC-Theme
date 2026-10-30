#!/system/bin/sh
# Återställer färginställningen som fanns innan modulen installerades.
KEY=theme_customization_overlay_packages
BACKUP=/data/adb/snc_theme_original.txt
if [ -f "$BACKUP" ]; then
  OLD="$(cat "$BACKUP")"
  if [ -z "$OLD" ] || [ "$OLD" = "null" ]; then
    settings delete secure "$KEY"
  else
    settings put secure "$KEY" "$OLD"
  fi
  rm -f "$BACKUP"
fi
