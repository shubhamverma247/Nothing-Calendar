// Generates app/src/main/res/drawable/glyph_toy_thumbnail.png
// Full gray dot-matrix "plate" filling a round area (like other Glyph Toys) with a
// white calendar icon on top. Transparent background (no black card in the picker).
// Pure Node — hand-rolled PNG encoder (zlib is built in).

const fs = require('fs');
const zlib = require('zlib');
const path = require('path');

const SIZE = 144;          // output px
const GRID = 25;           // matrix dots per side (mirrors the real 25x25 Glyph Matrix)
const cell = SIZE / GRID;  // px per dot cell
const dotR = cell * 0.38;  // dot radius (leaves gaps -> dot-matrix look)
const center = (GRID - 1) / 2;
const plateRadius = GRID / 2; // round mask

const GRAY = [45, 45, 45, 255];
const WHITE = [255, 255, 255, 255];

// ---- white calendar icon cells (25x25 grid) ----
const white = new Set();
const on = (x, y) => white.add(y * GRID + x);

// two hangers on top
[9, 15].forEach((x) => { on(x, 4); on(x, 5); });
// box border x:6..18 y:6..18
for (let x = 6; x <= 18; x++) { on(x, 6); on(x, 18); }
for (let y = 6; y <= 18; y++) { on(6, y); on(18, y); }
// solid header band
for (let x = 6; x <= 18; x++) { for (let y = 6; y <= 8; y++) on(x, y); }
// 3x2 date dots in the body (3 top, 3 bottom)
[[9, 12], [12, 12], [15, 12], [9, 15], [12, 15], [15, 15]].forEach(([x, y]) => on(x, y));

// ---- build active cells: gray plate inside circle, white icon on top ----
function cellColor(gx, gy) {
  if (white.has(gy * GRID + gx)) return WHITE;
  const dx = gx - center;
  const dy = gy - center;
  if (dx * dx + dy * dy <= plateRadius * plateRadius) return GRAY;
  return null; // outside round mask -> transparent
}

// ---- rasterize dots into an RGBA buffer ----
const buf = Buffer.alloc(SIZE * SIZE * 4, 0); // transparent
for (let gy = 0; gy < GRID; gy++) {
  for (let gx = 0; gx < GRID; gx++) {
    const color = cellColor(gx, gy);
    if (!color) continue;
    const cxp = (gx + 0.5) * cell;
    const cyp = (gy + 0.5) * cell;
    const minX = Math.max(0, Math.floor(cxp - dotR));
    const maxX = Math.min(SIZE - 1, Math.ceil(cxp + dotR));
    const minY = Math.max(0, Math.floor(cyp - dotR));
    const maxY = Math.min(SIZE - 1, Math.ceil(cyp + dotR));
    for (let py = minY; py <= maxY; py++) {
      for (let px = minX; px <= maxX; px++) {
        const ddx = px + 0.5 - cxp;
        const ddy = py + 0.5 - cyp;
        if (ddx * ddx + ddy * ddy > dotR * dotR) continue;
        const i = (py * SIZE + px) * 4;
        buf[i] = color[0]; buf[i + 1] = color[1]; buf[i + 2] = color[2]; buf[i + 3] = color[3];
      }
    }
  }
}

// ---- encode PNG (RGBA, no filter) ----
function crc32(b) {
  let c = ~0;
  for (let i = 0; i < b.length; i++) {
    c ^= b[i];
    for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xEDB88320 & -(c & 1));
  }
  return (~c) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length, 0);
  const t = Buffer.from(type, 'ascii');
  const body = Buffer.concat([t, data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(body), 0);
  return Buffer.concat([len, body, crc]);
}
const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
const ihdr = Buffer.alloc(13);
ihdr.writeUInt32BE(SIZE, 0); ihdr.writeUInt32BE(SIZE, 4);
ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
// raw scanlines with filter byte 0
const raw = Buffer.alloc(SIZE * (SIZE * 4 + 1));
for (let y = 0; y < SIZE; y++) {
  raw[y * (SIZE * 4 + 1)] = 0;
  buf.copy(raw, y * (SIZE * 4 + 1) + 1, y * SIZE * 4, (y + 1) * SIZE * 4);
}
const idat = zlib.deflateSync(raw, { level: 9 });
const png = Buffer.concat([
  sig,
  chunk('IHDR', ihdr),
  chunk('IDAT', idat),
  chunk('IEND', Buffer.alloc(0)),
]);
const out = path.join(__dirname, '..', 'app', 'src', 'main', 'res', 'drawable', 'glyph_toy_thumbnail.png');
fs.writeFileSync(out, png);
console.log('wrote', out, png.length, 'bytes');
