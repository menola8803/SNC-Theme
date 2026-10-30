import numpy as np
from PIL import Image, ImageFilter

W, H = 1264, 2780
rng = np.random.default_rng(7)

def noise(scale, octaves=5, persistence=0.55, seed=0):
    r = np.random.default_rng(seed)
    out = np.zeros((H, W)); amp = 1; tot = 0
    for o in range(octaves):
        gw = max(2, int(W / scale * 2**o)); gh = max(2, int(H / scale * 2**o))
        g = r.random((gh, gw)).astype(np.float32)
        up = np.array(Image.fromarray(g).resize((W, H), Image.BICUBIC))
        out += up * amp; tot += amp; amp *= persistence
    out /= tot
    return (out - out.min()) / (out.max() - out.min())

yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
n_shore = noise(500, 4, seed=1)
n_foam = noise(60, 6, 0.6, seed=2)
n_foam2 = noise(12, 4, 0.6, seed=3)
n_water = noise(180, 5, 0.5, seed=4)
n_sand = noise(8, 3, 0.6, seed=5)
n_large = noise(900, 3, seed=6)

# shoreline: diagonal, sea upper-left, beach lower-right
shore = 0.78 * H + 0.10 * H * (xx / W - 0.5) * -1 + 60 * np.sin(xx / W * 5.0 + 1) + 140 * (n_shore - 0.5)
d = shore - yy  # >0 sea side (distance above shore in px)

def lerp(a, b, t): return a + (b - a) * t[..., None]
c = lambda h: np.array([int(h[i:i+2], 16) for i in (1, 3, 5)], np.float32) / 255

# --- water colour by depth
depth = np.clip(d / (0.62 * H), 0, 1)
shallow, mid, deep = c("#1D5770"), c("#0A2E52"), c("#010F26")
water = np.where((depth < 0.18)[..., None], lerp(shallow, mid, np.clip(depth / 0.18, 0, 1)),
                 lerp(mid, deep, np.clip((depth - 0.18) / 0.82, 0, 1) ** 0.8))
ripple = (np.sin((yy + 40 * n_water) / 9.0 + xx / 60) * 0.5 + 0.5) * (n_water - 0.3)
fine = np.sin((yy*0.9 + 60*n_foam2 + 0.25*xx) / 4.5) * (n_foam - 0.5)
water = water * (0.86 + 0.25 * ripple[..., None] + 0.22 * fine[..., None])

# --- sand
sand_dry, sand_wet = c("#3B3528"), c("#1C2024")
wet = np.clip((d + 260) / 260, 0, 1)  # near shore region on sand side is wet
sand = lerp(sand_dry, sand_wet, wet ** 1.5) * (0.85 + 0.3 * n_sand[..., None])
sand = sand * (0.9 + 0.2 * n_large[..., None])

img = np.where((d > 0)[..., None], water, sand)

# smooth transition (thin water film on wet sand)
film = np.clip(1 - np.abs(d) / 40, 0, 1)
img = img * (1 - 0.5 * film[..., None]) + c("#244C5C") * 0.5 * film[..., None]

# --- foam lines (breaking waves) at several distances
foam = np.zeros((H, W), np.float32)
for dist, width, strength in [(10, 30, 1.0), (170, 22, 0.9), (360, 26, 0.85), (620, 30, 0.7), (950, 26, 0.45)]:
    wob = dist + 70 * (n_shore - 0.5) + 25 * np.sin(xx / 140 + dist)
    band = np.exp(-((d - wob) / width) ** 2)
    trail = np.exp(-np.clip(d - wob, 0, None) / (width * 2.2)) * (d > wob)
    lace = np.clip(((n_foam * 0.45 + n_foam2 * 0.75) - 0.6) * 3.5, 0, 1)
    edge = np.exp(-((d - wob) / (width*0.35)) ** 2)
    foam += strength * (0.8*edge + band * 0.5 * lace + 0.6 * trail * lace)
# swash edge on sand
swash = np.exp(-((d + 90 + 50 * (n_foam - 0.5)) / 10) ** 2) * 0.6
foam = np.clip(foam + swash * (n_foam2 > 0.35), 0, 1)
foam_col = c("#D9E2EC")
img = img * (1 - foam[..., None]) + foam_col * foam[..., None]

# --- moonlight: soft warm-silver glow across sea + glints
glow = np.exp(-(((xx - 0.78 * W) / (0.55 * W)) ** 2 + ((yy - 0.38 * H) / (0.45 * H)) ** 2))
img = img + c("#3A3220") * 0.55 * glow[..., None]
glint = (n_foam2 > 0.9) & (d > 300) & (d < 1300) & (n_water > 0.6)
img = img + c("#E9C27A") * (glint * glow * 0.9)[..., None]

# --- night grade + top darkening for clock legibility
top = np.clip(1 - yy / (0.45 * H), 0, 1) ** 1.6
img = img * (1 - 0.55 * top[..., None]) + c("#011634") * 0.55 * top[..., None]
vig = 1 - 0.35 * (((xx - W / 2) / (W * 0.75)) ** 2 + ((yy - H * 0.6) / (H * 0.8)) ** 2)
img = img * np.clip(vig, 0.55, 1)[..., None]
grain = rng.normal(0, 0.012, (H, W, 1))
img = np.clip(img + grain, 0, 1)

out = Image.fromarray((img * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(0.8))
out.save("/home/claude/snc_bakgrund_strand_natt_1264x2780.png")
print("ok")
