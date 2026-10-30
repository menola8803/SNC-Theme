import base64, cairosvg

BG = "#011634"; SURF = "#0C2550"; SURF2 = "#123164"
GOLD = "#DDAE54"; GOLD_L = "#E9C27A"; WHITE = "#F5F2EA"; NAVY = BG
INTER = "font-family='Inter, sans-serif'"
CORM = "font-family='Cormorant Garamond, serif'"
CINZ = "font-family='Cinzel, serif'"

def b64(p): return "data:image/png;base64," + base64.b64encode(open(p, "rb").read()).decode()
WP = b64("wp_small.png"); FULL = b64("logo_full.png"); MONO = b64("logo_mono.png")

def img(href, x, y, w, h): return f"<image href='{href}' x='{x}' y='{y}' width='{w}' height='{h}' preserveAspectRatio='xMidYMid meet'/>"

def icon(d, cx, cy, size=26, color=GOLD_L, sw=1.8):
    s = size / 24
    return (f"<g transform='translate({cx-size/2},{cy-size/2}) scale({s})' fill='none' stroke='{color}' "
            f"stroke-width='{sw}' stroke-linecap='round' stroke-linejoin='round'>{d}</g>")

P = lambda d: f"<path d='{d}'/>"
I = {
 "cal": "<rect x='4' y='5' width='16' height='15' rx='2'/>" + P("M4 10h16M8 3v4M16 3v4"),
 "files": P("M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"),
 "gallery": "<rect x='4' y='4' width='16' height='16' rx='2'/><circle cx='9' cy='9' r='1.6'/>" + P("M20 15l-5-5L5 20"),
 "settings": "<circle cx='12' cy='12' r='3'/>" + P("M12 2.5v3M12 18.5v3M2.5 12h3M18.5 12h3M5.3 5.3l2.1 2.1M16.6 16.6l2.1 2.1M5.3 18.7l2.1-2.1M16.6 7.4l2.1-2.1"),
 "phone": P("M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2"),
 "msg": P("M4 5h16v11H9l-5 4z"),
 "web": "<circle cx='12' cy='12' r='9'/>" + P("M3 12h18M12 3a14 14 0 0 1 0 18a14 14 0 0 1 0-18"),
 "cam": P("M4 8h3l2-3h6l2 3h3v11H4z") + "<circle cx='12' cy='13' r='3.5'/>",
 "search": "<circle cx='11' cy='11' r='7'/>" + P("M20 20l-4-4"),
 "torch": P("M7 3h10v4l-3 4v10h-4V11L7 7zM7 7h10"),
 "finger": P("M6.5 7.5a7 7 0 0 1 11 0M5 12a7 7 0 0 1 14 0v1M8.5 19a10 10 0 0 0 .5-6.5a3 3 0 0 1 6 0c0 2.5-.3 4.5-1.2 6.5M12 12.5c0 3-.5 5.5-2 8"),
 "pen": P("M4 20h4L19 9l-4-4L4 16z"),
 "sun": "<circle cx='12' cy='12' r='4'/>" + P("M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"),
 "wifi": P("M2 9a15 15 0 0 1 20 0M5.5 12.5a10 10 0 0 1 13 0M9 16a5 5 0 0 1 6 0M12 19.5h.01"),
 "bt": P("M7 7l10 10-5 5V2l5 5L7 17"),
 "data": P("M7 20V8M3 12l4-4 4 4M17 4v12M13 12l4 4 4-4"),
 "dnd": "<circle cx='12' cy='12' r='9'/>" + P("M8 12h8"),
 "moon": P("M20 14A8 8 0 1 1 10 4a6 6 0 0 0 10 10"),
 "batt": "<rect x='5' y='7' width='13' height='11' rx='1'/>" + P("M21 11v3"),
 "rec": "<circle cx='12' cy='12' r='9'/><circle cx='12' cy='12' r='3'/>",
}

def t(x, y, txt, size, weight=500, color=WHITE, anchor="start", extra="", fam=INTER):
    return f"<text x='{x}' y='{y}' {fam} font-size='{size}' font-weight='{weight}' fill='{color}' text-anchor='{anchor}' {extra}>{txt}</text>"


from PIL import ImageFont
def _w(txt, size):
    f = ImageFont.truetype("/root/.fonts/Cinzel.ttf", int(size*10)); return f.getlength(txt)/10
def clock(x, y, size, anchor="start"):
    gap = size*0.16; w09 = _w("09", size); w41 = _w("41", size); tot = w09 + 2*gap + w41
    x0 = x if anchor == "start" else x - tot/2
    cx = x0 + w09 + gap; r = size*0.045
    return (t(x0, y, "09", size, 400, WHITE, fam=CINZ) + t(cx+gap, y, "41", size, 400, WHITE, fam=CINZ)
            + f"<circle cx='{cx:.1f}' cy='{y-size*0.5:.1f}' r='{r:.1f}' fill='{GOLD}'/><circle cx='{cx:.1f}' cy='{y-size*0.18:.1f}' r='{r:.1f}' fill='{GOLD}'/>")

def star(cx, cy, r, color=GOLD):
    import math
    pts = []
    for i in range(16):
        rr = r if i % 2 == 0 else r * 0.32
        if i % 4 == 2: rr = r * 0.62 if i % 2 == 0 else rr
        a = math.pi * i / 8 - math.pi / 2
        pts.append(f"{cx+rr*math.cos(a):.1f},{cy+rr*math.sin(a):.1f}")
    return f"<polygon points='{' '.join(pts)}' fill='{color}'/>"

def wallpaper(dim=False):
    g = f"<image href='{WP}' x='0' y='0' width='390' height='858' preserveAspectRatio='none'/>"
    if dim: g += "<rect width='390' height='858' fill='#000814' opacity='0.35'/>"
    return g

def _old_wallpaper(dim=False):
    o = 0.55 if dim else 1
    st = "" if dim else f"<g>{star(292, 300, 30, GOLD)}</g>"
    return (f"<rect width='390' height='858' fill='{BG}'/>" + st +
            "<path d='M0 440 L70 372 L120 402 L190 330 L250 388 L300 352 L390 416 L390 858 L0 858 Z' fill='#0A2550'/>"
            "<path d='M0 478 L50 450 L110 478 L170 430 L240 472 L320 440 L390 478 L390 858 L0 858 Z' fill='#071D42'/>"
            "<rect y='522' width='390' height='336' fill='#01112A'/>"
            f"<path d='M-10 530 C 90 570, 210 560, 400 488 C 230 548, 100 552, -10 518 Z' fill='{GOLD}' opacity='{0.9*o}'/>")

def battery(x, y):
    return (f"<g transform='translate({x},{y})'><rect x='1' y='1' width='21' height='12' rx='3' fill='none' stroke='{WHITE}' stroke-width='1.5'/>"
            f"<rect x='3' y='3' width='14' height='8' rx='1.5' fill='{WHITE}'/><rect x='23' y='5' width='2' height='4' rx='1' fill='{WHITE}'/></g>")

END = (f"<circle cx='195' cy='20' r='6' fill='#000814'/>"
       f"<rect x='155' y='842' width='80' height='4' rx='2' fill='{WHITE}' opacity='0.8'/>")

def appicon(cx, cy, k, gold=False):
    if gold:
        return f"<circle cx='{cx}' cy='{cy}' r='29' fill='{GOLD}'/>" + icon(I[k], cx, cy, 26, BG)
    return (f"<circle cx='{cx}' cy='{cy}' r='28.5' fill='{SURF}' stroke='{GOLD}' stroke-opacity='0.7' stroke-width='1.2'/>"
            + icon(I[k], cx, cy, 26, GOLD_L))

def home():
    s = wallpaper()
    s += t(26, 31, "09:41", 13, 600)
    s += icon(I["wifi"], 306, 25, 16, WHITE, 2.2)
    s += f"<path d='M316 33h3v-4h-3z M321.5 33h3v-8h-3z M327 33h3V21h-3z M332.5 33h2.5V17h-2.5z' fill='{WHITE}'/>" + battery(342, 18)
    s += clock(28, 140, 80)
    s += t(30, 176, "FREDAG 30 OKTOBER", 13, 500, GOLD_L, extra="letter-spacing='2.5'", fam=CINZ)
    s += img(MONO, 26, 196, 92, 36)
    for cx, k, lbl in [(62.25,"cal","Kalender"),(150.75,"files","Filer"),(239.25,"gallery","Galleri"),(327.75,"settings","Inställn.")]:
        s += appicon(cx, 600, k) + t(cx, 648, lbl, 12, 600, BG, "middle", "stroke='#011634' stroke-width='3.5' stroke-linejoin='round' opacity='0.85'") + t(cx, 648, lbl, 12, 600, anchor="middle")
    s += f"<rect x='16' y='680' width='358' height='80' rx='30' fill='{WHITE}' opacity='0.08'/>"
    for cx, k in [(65.25,"phone"),(151.75,"msg"),(238.25,"web"),(324.75,"cam")]:
        s += appicon(cx, 720, k, gold=(k == "cam"))
    s += f"<rect x='22' y='776' width='346' height='48' rx='24' fill='{WHITE}'/>" + icon(I["search"], 50, 800, 20, BG, 2)
    s += t(72, 805, "Sök i telefonen", 15, 500, "#4F5B6B")
    return s + END

def notif(y, gold, k, app, when, title, sub):
    bg, fg = (GOLD, BG) if gold else (BG, GOLD_L)
    return (f"<rect x='16' y='{y}' width='358' height='80' rx='22' fill='{WHITE}' opacity='0.96'/>"
            f"<circle cx='50' cy='{y+32}' r='18' fill='{bg}'/>" + icon(I[k], 50, y+32, 18, fg, 2) +
            t(78, y+26, app, 12, 600, "#4A5568") + t(358, y+26, when, 12, 600, "#4A5568", "end") +
            t(78, y+46, title, 15, 700, BG) + t(78, y+65, sub, 13, 500, "#36435A"))

def lock():
    s = wallpaper(dim=True)
    s += t(26, 31, "[Operatör]", 13, 600, "#D6D2C8") + battery(342, 18)
    s += img(FULL, 70, 62, 250, 146)
    s += clock(195, 330, 92, 'middle')
    s += t(195, 370, "FREDAG 30 OKTOBER", 13, 500, GOLD_L, "middle", "letter-spacing='2.5'", CINZ)
    s += notif(570, False, "cal", "Kalender", "om 20 min", "Workshop med [Kund]", "10:00–11:30 · [Plats]")
    s += notif(658, True, "msg", "Meddelanden", "nu", "[Kollega]", "Underlaget är uppladdat i projektmappen.")
    for cx, k in [(54,"torch"),(336,"cam")]:
        s += f"<circle cx='{cx}' cy='786' r='26' fill='{WHITE}' opacity='0.14'/>" + icon(I[k], cx, 786, 22, WHITE)
    s += f"<circle cx='195' cy='786' r='30' fill='none' stroke='{GOLD}' stroke-opacity='0.7' stroke-width='1.5'/>" + icon(I["finger"], 195, 786, 28, GOLD_L, 1.6)
    return s + END

def qs():
    s = f"<rect width='390' height='858' fill='{BG}'/>"
    s += clock(22, 100, 48) + t(23, 126, "FRE 30 OKT", 12, 500, GOLD_L, extra="letter-spacing='2.2'", fam=CINZ)
    for cx, k in [(302,"pen"),(346,"settings")]:
        s += f"<circle cx='{cx}' cy='84' r='22' fill='{SURF}'/>" + icon(I[k], cx, 84, 20, GOLD_L)
    s += f"<rect x='22' y='146' width='346' height='52' rx='26' fill='{SURF}'/><rect x='22' y='146' width='221' height='52' rx='26' fill='{GOLD}'/>"
    s += icon(I["sun"], 49, 172, 22, BG)
    tiles = [("wifi","Wi-Fi","SNC-Kontor",1),("bt","Bluetooth","Hörlurar",1),("data","Mobildata","5G",0),("dnd","Stör ej","Av",0),
             ("moon","Mörkt tema","På",1),("torch","Ficklampa","Av",0),("batt","Batterispar","Av",0),("rec","Skärminspeln.","Starta",0)]
    for i, (k, lbl, sub, on) in enumerate(tiles):
        x = 22 + (i % 2) * 178; y = 214 + (i // 2) * 78
        bg, fg, ib, ic = (WHITE, BG, GOLD, BG) if on else (SURF, WHITE, SURF2, GOLD_L)
        s += f"<rect x='{x}' y='{y}' width='168' height='68' rx='22' fill='{bg}'/>"
        s += f"<circle cx='{x+32}' cy='{y+34}' r='18' fill='{ib}'/>" + icon(I[k], x+32, y+34, 20, ic)
        s += t(x+60, y+31, lbl, 14, 700, fg) + t(x+60, y+49, sub, 12, 500, fg, extra="opacity='0.8'")
    y = 538
    s += f"<rect x='22' y='{y}' width='346' height='84' rx='24' fill='{SURF}'/><rect x='36' y='{y+14}' width='60' height='56' rx='14' fill='{BG}'/>"
    s += img(MONO, 39, y+30, 54, 22)
    s += t(108, y+38, "Fokusläge: Kundmöte", 14, 700) + t(108, y+57, "Aviseringar pausade", 12, 500, "#C7C9CF")
    s += f"<rect x='290' y='{y+24}' width='66' height='36' rx='18' fill='{GOLD}'/>" + t(323, y+47, "Avsluta", 13, 700, BG, "middle")
    s += star(195, 760, 9, GOLD)
    s += f"<rect x='60' y='759.5' width='120' height='1' fill='{GOLD}' opacity='0.7'/><rect x='210' y='759.5' width='120' height='1' fill='{GOLD}' opacity='0.7'/>"
    s += t(195, 792, "CONNECTING VISION WITH OPPORTUNITY", 9.5, 500, "#D6D2C8", "middle", "letter-spacing='1.6'", CINZ)
    s += t(195, 816, "LineageOS 23.2 · OnePlus 13R", 11, 500, "#8F9AAD", "middle")
    return s + END

def saver():
    s = wallpaper() + "<rect width='390' height='858' fill='#000510' opacity='0.62'/>"
    s += img(FULL, 55, 120, 280, 164)
    s += clock(195, 470, 100, 'middle')
    s += t(195, 512, "FREDAG 30 OKTOBER", 13, 500, GOLD_L, "middle", "letter-spacing='2.5'", CINZ)
    s += icon(I["batt"], 160, 560, 16, GOLD_L, 2) + t(174, 565, "Laddar", 12, 500, "#C9CCD3")
    s += star(195, 760, 8, GOLD)
    s += f"<rect x='70' y='759.5' width='110' height='1' fill='{GOLD}' opacity='0.6'/><rect x='210' y='759.5' width='110' height='1' fill='{GOLD}' opacity='0.6'/>"
    s += t(195, 790, "CONNECTING VISION WITH OPPORTUNITY", 9.5, 500, "#D6D2C8", "middle", "letter-spacing='1.6'", CINZ)
    return s + "<circle cx='195' cy='20' r='6' fill='#000814'/>"

W, H = 1956, 1150
out = [f"<svg xmlns='http://www.w3.org/2000/svg' width='{W}' height='{H}' viewBox='0 0 {W} {H}'>",
       "<defs><clipPath id='scr'><rect width='390' height='858' rx='38'/></clipPath></defs>",
       f"<rect width='{W}' height='{H}' fill='#EEE9DF'/>",
       t(60, 82, "SCANDINAVIAN NEXUM CONSULTING", 32, 600, BG, extra="letter-spacing='2'", fam=CINZ),
       t(62, 116, "Temaförslag · LineageOS 23.2 på OnePlus 13R", 18, 500, "#4A5568")]
for i, (fn, lbl) in enumerate([(home,"Hemskärm"),(lock,"Låsskärm"),(saver,"Skärmsläckare"),(qs,"Snabbinställningar")]):
    x = 60 + i * 474; y = 160
    out.append(f"<g transform='translate({x},{y})'><rect width='414' height='882' rx='48' fill='#000A1C'/>"
               f"<g transform='translate(12,12)' clip-path='url(#scr)'>{fn()}</g></g>")
    out.append(t(x + 207, y + 930, lbl, 18, 600, BG, "middle"))
out.append("</svg>")
svg = "".join(out)
cairosvg.svg2png(bytestring=svg.encode(), write_to="/home/claude/snc_tema_oneplus13r_v5.png", output_width=W*2, output_height=H*2)
print("done")


WPFULL = b64("/home/claude/snc_bakgrund_strand_natt_1264x2780.png")
ss = (f"<svg xmlns='http://www.w3.org/2000/svg' width='1264' height='2780' viewBox='0 0 390 858'>"
      f"<image href='{WPFULL}' x='0' y='0' width='390' height='858' preserveAspectRatio='none'/>"
      "<rect width='390' height='858' fill='#000510' opacity='0.55'/>" + img(FULL, 45, 230, 300, 176) +
      star(195, 760, 8, GOLD) +
      f"<rect x='70' y='759.5' width='110' height='1' fill='{GOLD}' opacity='0.6'/><rect x='210' y='759.5' width='110' height='1' fill='{GOLD}' opacity='0.6'/>"
      + t(195, 790, "CONNECTING VISION WITH OPPORTUNITY", 9.5, 500, "#D6D2C8", "middle", "letter-spacing='1.6'", CINZ) + "</svg>")
cairosvg.svg2png(bytestring=ss.encode(), write_to="/home/claude/snc_skarmslackare_logga_1264x2780.png", output_width=1264, output_height=2780)
print("saver img done")
