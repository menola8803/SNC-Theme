SKIPUNZIP=0
ui_print "- SNC Tema: Scandinavian Nexum Consulting"
ui_print "- Guldfärgerna tillämpas efter omstart"
ui_print "- Eller tryck på Åtgärd i Magisk för att tillämpa direkt"
set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/action.sh" 0 0 0755
set_perm "$MODPATH/apply.sh" 0 0 0755
set_perm "$MODPATH/uninstall.sh" 0 0 0755
