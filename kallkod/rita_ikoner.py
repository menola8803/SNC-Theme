import os, cairosvg
GOLD="#DDAE54"; TEAL="#4DB6A4"; CREAM="#EAD9AE"
OUT="svg"; os.makedirs(OUT, exist_ok=True)

def frame(inner, ring=True, dots=True):
    s=("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 108 108' width='432' height='432'>"
       "<defs><linearGradient id='bg' x1='0' y1='0' x2='1' y2='1'>"
       "<stop offset='0' stop-color='#123844'/><stop offset='1' stop-color='#0A1E29'/></linearGradient></defs>"
       "<rect width='108' height='108' fill='url(#bg)'/>")
    if ring:
        s+=f"<circle cx='54' cy='54' r='31' fill='none' stroke='{GOLD}' stroke-width='2.2'/>"
    if dots:
        s+=f"<circle cx='32.1' cy='32.1' r='3.4' fill='{TEAL}'/><circle cx='75.9' cy='75.9' r='3.4' fill='{CREAM}'/>"
    s+=f"<g transform='translate(54 54) scale(1.35) translate(-12 -12)' fill='none' stroke='{GOLD}' stroke-width='1.6' stroke-linecap='round' stroke-linejoin='round'>{inner}</g></svg>"
    return s

P=lambda d: f"<path d='{d}'/>"
ICONS = {
 "telefon":   P("M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2"),
 "meddelanden": P("M4 5h16v11H9l-5 4z")+P("M8 9.5h8M8 12.5h5"),
 "kontakter": "<circle cx='12' cy='9' r='3.5'/>"+P("M5 20a7 7 0 0 1 14 0"),
 "kamera":    P("M4 8h3l2-3h6l2 3h3v11H4z")+"<circle cx='12' cy='13' r='3.5'/>",
 "galleri":   "<rect x='4' y='4' width='16' height='16' rx='2'/><circle cx='9' cy='9' r='1.6'/>"+P("M20 15l-5-5L5 20"),
 "klocka":    "<circle cx='12' cy='12' r='8.5'/>"+P("M12 7v5l3.5 2"),
 "kalender":  "<rect x='4' y='5' width='16' height='15' rx='2'/>"+P("M4 10h16M8 3v4M16 3v4")+"<circle cx='12' cy='15' r='1.2' fill='"+GOLD+"' stroke='none'/>",
 "kalkylator":"<rect x='5' y='3' width='14' height='18' rx='2'/>"+P("M8 7h8M8.5 12h.01M12 12h.01M15.5 12h.01M8.5 16h.01M12 16h.01M15.5 16h.01"),
 "installningar":"<circle cx='12' cy='12' r='3'/>"+P("M12 2.5v3M12 18.5v3M2.5 12h3M18.5 12h3M5.3 5.3l2.1 2.1M16.6 16.6l2.1 2.1M5.3 18.7l2.1-2.1M16.6 7.4l2.1-2.1"),
 "filer":     P("M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"),
 "webblasare":"<circle cx='12' cy='12' r='8.5'/>"+P("M3.5 12h17M12 3.5a13 13 0 0 1 0 17a13 13 0 0 1 0-17"),
 "musik":     P("M9 18V6l10-2v12")+"<circle cx='6.5' cy='18' r='2.5'/><circle cx='16.5' cy='16' r='2.5'/>",
 "inspelning":"<rect x='9' y='3' width='6' height='11' rx='3'/>"+P("M5.5 11a6.5 6.5 0 0 0 13 0M12 17.5V21M9 21h6"),
 "e-post":    "<rect x='3.5' y='6' width='17' height='12' rx='2'/>"+P("M4 7l8 6 8-6"),
 "uppdatering":P("M20 12a8 8 0 1 1-2.3-5.6M20 4v4.5h-4.5"),
 "appbutik":  P("M5 8h14l-1 12H6z")+P("M9 8V6.5a3 3 0 0 1 6 0V8"),
}
LABELS = {"telefon":"Telefon","meddelanden":"Meddelanden","kontakter":"Kontakter","kamera":"Kamera","galleri":"Galleri",
 "klocka":"Klocka","kalender":"Kalender","kalkylator":"Kalkylator","installningar":"Inställningar","filer":"Filer",
 "webblasare":"Webbläsare","musik":"Musik","inspelning":"Inspelning","e-post":"E-post","uppdatering":"Uppdatering","appbutik":"Appbutik"}
for k,v in ICONS.items():
    open(f"{OUT}/{k}.svg","w").write(frame(v))

# preview sheet on the beach wallpaper
import base64
wp="data:image/png;base64,"+base64.b64encode(open("/home/claude/snc_bakgrund_strand_natt_1264x2780.png","rb").read()).decode()
cols=4; W=1080; H=1500
sheet=[f"<svg xmlns='http://www.w3.org/2000/svg' width='{W}' height='{H}' viewBox='0 0 {W} {H}'>",
       f"<image href='{wp}' x='0' y='-700' width='{W}' height='{W*2780/1264}' preserveAspectRatio='none'/>",
       f"<rect width='{W}' height='{H}' fill='#000510' opacity='0.35'/>",
       f"<text x='540' y='110' font-family='Cinzel, serif' font-size='40' fill='{GOLD}' text-anchor='middle' letter-spacing='4'>SNC IKONER</text>"]
for i,k in enumerate(ICONS):
    r,c=divmod(i,cols); x=60+c*255; y=180+r*320
    inner=frame(ICONS[k]).split('>',1)[1].rsplit('</svg>',1)[0]
    sheet.append(f"<defs><clipPath id='sq{i}'><rect x='{x}' y='{y}' width='190' height='190' rx='58'/></clipPath></defs>")
    sheet.append(f"<g clip-path='url(#sq{i})'><svg x='{x}' y='{y}' width='190' height='190' viewBox='0 0 108 108'>{inner}</svg></g>")
    sheet.append(f"<text x='{x+95}' y='{y+240}' font-family='Inter, sans-serif' font-size='30' font-weight='500' fill='#F5F2EA' text-anchor='middle'>{LABELS[k]}</text>")
sheet.append("</svg>")
cairosvg.svg2png(bytestring="".join(sheet).encode(), write_to="/home/claude/snc_ikoner_forhandsvisning.png")
print(len(ICONS))
