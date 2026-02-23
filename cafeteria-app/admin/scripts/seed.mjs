/**
 * Seed script — populates Firestore menuItems with a realistic USIU cafeteria menu.
 * Run: npm run seed
 */

import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";
import { dirname, join } from "path";

const __dirname = dirname(fileURLToPath(import.meta.url));

// Parse .env.local — handle the FIREBASE_SERVICE_ACCOUNT JSON value which contains = signs
const envText = readFileSync(join(__dirname, "../.env.local"), "utf8");
let serviceAccount;
for (const line of envText.split("\n")) {
  if (line.startsWith("FIREBASE_SERVICE_ACCOUNT=")) {
    serviceAccount = JSON.parse(line.slice("FIREBASE_SERVICE_ACCOUNT=".length));
    break;
  }
}
if (!serviceAccount) throw new Error("FIREBASE_SERVICE_ACCOUNT not found in .env.local");

initializeApp({ credential: cert(serviceAccount) });
const db = getFirestore();
db.settings({ preferRest: true });

// Unsplash photo IDs — stable direct links
const IMG = {
  mandazi:    "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&fit=crop&q=80",
  eggs:       "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=400&fit=crop&q=80",
  porridge:   "https://images.unsplash.com/photo-1547592180-85f173990554?w=400&fit=crop&q=80",
  tea:        "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=400&fit=crop&q=80",
  chapati:    "https://images.unsplash.com/photo-1574484284002-952d92456975?w=400&fit=crop&q=80",
  rice_bowl:  "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&fit=crop&q=80",
  chicken:    "https://images.unsplash.com/photo-1598103442097-8b74394b95c3?w=400&fit=crop&q=80",
  pilau:      "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=400&fit=crop&q=80",
  curry:      "https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400&fit=crop&q=80",
  beans:      "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?w=400&fit=crop&q=80",
  pasta:      "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400&fit=crop&q=80",
  fish:       "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400&fit=crop&q=80",
  mashed:     "https://images.unsplash.com/photo-1574484284002-952d92456975?w=400&fit=crop&q=80",
  chips:      "https://images.unsplash.com/photo-1562967914-608f82629710?w=400&fit=crop&q=80",
};

const MENU = [
  // ── Breakfast ─────────────────────────────────────────────────────────────
  {
    name: "Mandazi & Chai",
    description: "Freshly fried mandazi served with a cup of hot chai",
    price: 80,
    category: "breakfast",
    imageUrl: IMG.mandazi,
    available: true,
  },
  {
    name: "Eggs (Boiled / Fried)",
    description: "Farm-fresh eggs boiled or fried to your liking, served with bread",
    price: 60,
    category: "breakfast",
    imageUrl: IMG.eggs,
    available: true,
  },
  {
    name: "Uji (Millet Porridge)",
    description: "Warm millet porridge, lightly sweetened — a morning classic",
    price: 40,
    category: "breakfast",
    imageUrl: IMG.porridge,
    available: true,
  },
  {
    name: "Chapati & Beans",
    description: "Soft layered chapati served with well-seasoned githeri beans",
    price: 100,
    category: "breakfast",
    imageUrl: IMG.chapati,
    available: true,
  },

  // ── Lunch ─────────────────────────────────────────────────────────────────
  {
    name: "Rice & Beef Stew",
    description: "Steamed white rice topped with slow-cooked beef and vegetable stew",
    price: 150,
    category: "lunch",
    imageUrl: IMG.rice_bowl,
    available: true,
  },
  {
    name: "Ugali, Sukuma & Chicken",
    description: "USIU classic — firm ugali, sautéed kale and grilled quarter chicken",
    price: 180,
    category: "lunch",
    imageUrl: IMG.chicken,
    available: true,
  },
  {
    name: "Pilau",
    description: "Fragrant spiced rice slow-cooked with tender beef and whole spices",
    price: 160,
    category: "lunch",
    imageUrl: IMG.pilau,
    available: true,
  },
  {
    name: "Chapati & Chicken Curry",
    description: "Soft layered chapati served with creamy, mildly spiced chicken curry",
    price: 170,
    category: "lunch",
    imageUrl: IMG.curry,
    available: true,
  },
  {
    name: "Githeri",
    description: "Hearty mix of maize and beans simmered with tomatoes and onions",
    price: 120,
    category: "lunch",
    imageUrl: IMG.beans,
    available: true,
  },

  // ── Dinner ────────────────────────────────────────────────────────────────
  {
    name: "Spaghetti Bolognese",
    description: "Al-dente spaghetti with seasoned minced beef in rich tomato sauce",
    price: 150,
    category: "dinner",
    imageUrl: IMG.pasta,
    available: true,
  },
  {
    name: "Rice & Tilapia Fillet",
    description: "Golden-fried Nile tilapia fillet served with steamed rice and salad",
    price: 200,
    category: "dinner",
    imageUrl: IMG.fish,
    available: true,
  },
  {
    name: "Mukimo & Beef Stew",
    description: "Mashed potato, maize and green peas served with hearty beef stew",
    price: 140,
    category: "dinner",
    imageUrl: IMG.mashed,
    available: true,
  },
  {
    name: "Chips & Fried Chicken",
    description: "Crispy golden fries with a well-seasoned piece of fried chicken",
    price: 200,
    category: "dinner",
    imageUrl: IMG.chips,
    available: true,
  },
];

// Check existing items to avoid duplicates
const existing = await db.collection("menuItems").get();
const existingNames = new Set(existing.docs.map(d => d.data().name));

let added = 0;
for (const item of MENU) {
  if (existingNames.has(item.name)) {
    console.log(`  skip  ${item.name}`);
    continue;
  }
  const ref = db.collection("menuItems").doc();
  await ref.set({ ...item, itemId: ref.id, createdAt: Timestamp.now() });
  console.log(`  added  ${item.name}`);
  added++;
}

console.log(`\nDone — ${added} items added, ${MENU.length - added} skipped (already exist).`);
process.exit(0);
