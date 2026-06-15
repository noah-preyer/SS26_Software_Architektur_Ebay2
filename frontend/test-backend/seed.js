// seed-daten für test-backend.

const KATEGORIEN = ["Elektronik", "Kleidung", "Möbel", "Sport", "Fahrzeuge"];

// zum login reicht eine dieser e-mails + ein beliebiges passwort ab 8 zeichen
function seedUsers() {
  return [
    { id: 1, username: "max_mueller", email: "max@example.com", password: null },
    { id: 2, username: "anna_schmidt", email: "anna@example.com", password: null },
    { id: 3, username: "tom_wagner", email: "tom@example.com", password: null },
    { id: 4, username: "lisa_braun", email: "lisa@example.com", password: null },
    { id: 5, username: "demo_user", email: "demo@example.com", password: null },
  ];
}

function daysAgoIso(days) {
  return new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString();
}

// loremflickr sollte eigentlich pro keyword thematisch passende fotos liefern. Leider unzuverlässig
function flickr(keyword, count) {
  const urls = [];
  for (let i = 1; i <= count; i++) {
    urls.push(`https://loremflickr.com/400/300/${keyword}?lock=${i}`);
  }
  return urls;
}

function seedProducts() {
  return [
    { id: 1, title: "iPhone 15 Pro", description: "iPhone 15 Pro, 256GB, Titan Natur. Akku bei 92%, originalverpackung und kabel dabei.", price: 950, category: "Elektronik", sellerId: 1, status: "AVAILABLE", imageUrls: flickr("iphone", 2), createdAt: daysAgoIso(0) },
    { id: 2, title: "MacBook Air M2", description: "MacBook Air M2, 8GB RAM, 256GB SSD. Läuft einwandfrei, leichte gebrauchsspuren am gehäuse.", price: 1100, category: "Elektronik", sellerId: 2, status: "AVAILABLE", imageUrls: flickr("macbook", 2), createdAt: daysAgoIso(2) },
    { id: 3, title: "Sony Kopfhörer WH-1000XM4", description: "Noise-cancelling kopfhörer, kaum genutzt. Tasche und kabel dabei.", price: 280, category: "Elektronik", sellerId: 3, status: "SOLD", imageUrls: flickr("headphones", 1), createdAt: daysAgoIso(4) },
    { id: 4, title: "Nintendo Switch", description: "Switch standard-modell mit zwei joy-cons und dock. Mario Kart 8 liegt bei.", price: 220, category: "Elektronik", sellerId: 4, status: "AVAILABLE", imageUrls: flickr("nintendo", 2), createdAt: daysAgoIso(8) },
    { id: 5, title: "Winterjacke Größe M", description: "Warme winterjacke, gr. M, dunkelblau. Nur eine saison getragen.", price: 45, category: "Kleidung", sellerId: 5, status: "AVAILABLE", imageUrls: flickr("jacket", 1), createdAt: daysAgoIso(6) },
    { id: 6, title: "Nike Sneaker Größe 42", description: "Nike Air Max, gr. 42, weiß/grau. Wenige male getragen, karton vorhanden.", price: 75, category: "Kleidung", sellerId: 1, status: "AVAILABLE", imageUrls: flickr("sneakers", 2), createdAt: daysAgoIso(12) },
    { id: 7, title: "Lederhandtasche braun", description: "Lederhandtasche braun, mehrere innenfächer. Sehr guter zustand.", price: 120, category: "Kleidung", sellerId: 2, status: "SOLD", imageUrls: flickr("handbag", 1), createdAt: daysAgoIso(3) },
    { id: 8, title: "IKEA Regal Kallax", description: "Kallax regal weiß, 4x4 fächer. Nur selbstabholung in der innenstadt.", price: 60, category: "Möbel", sellerId: 3, status: "AVAILABLE", imageUrls: flickr("shelf", 2), createdAt: daysAgoIso(9) },
    { id: 9, title: "Schreibtisch höhenverstellbar", description: "Elektrisch höhenverstellbarer schreibtisch, 140x70cm, helles holzdekor. Geht einwandfrei.", price: 85, category: "Möbel", sellerId: 4, status: "AVAILABLE", imageUrls: flickr("desk", 1), createdAt: daysAgoIso(22) },
    { id: 10, title: "Küchenhocker 2er Set", description: "Zwei stapelbare küchenhocker aus holz, leichte gebrauchsspuren. Auch einzeln.", price: 30, category: "Möbel", sellerId: 5, status: "AVAILABLE", imageUrls: flickr("stool", 1), createdAt: daysAgoIso(5) },
    { id: 11, title: "Fahrrad Trek FX 2", description: "Trek FX 2, rahmengröße M, 21 gänge. Kürzlich gewartet, neue bremsbeläge.", price: 350, category: "Sport", sellerId: 1, status: "AVAILABLE", imageUrls: flickr("bicycle", 2), createdAt: daysAgoIso(18) },
    { id: 12, title: "Yogamatte rutschfest", description: "Yogamatte rutschfest, 6mm, mit trageriemen. Fast neu.", price: 25, category: "Sport", sellerId: 2, status: "AVAILABLE", imageUrls: flickr("yoga", 1), createdAt: daysAgoIso(27) },
    { id: 13, title: "Hantelset 20kg", description: "Verstellbares hantelset, 20kg gesamt. Gut fürs training zuhause.", price: 40, category: "Sport", sellerId: 3, status: "SOLD", imageUrls: flickr("dumbbell", 1), createdAt: daysAgoIso(1) },
    { id: 14, title: "VW Golf VII 1.6 TDI", description: "VW Golf VII, bj 2014, 145.000km, diesel, scheckheft gepflegt. TÜV neu.", price: 14500, category: "Fahrzeuge", sellerId: 4, status: "AVAILABLE", imageUrls: flickr("car", 3), createdAt: daysAgoIso(14) },
    { id: 15, title: "Honda Roller PCX 125", description: "Honda PCX 125, bj 2019, 8.500km, regelmäßig gewartet. Sparsam und zuverlässig.", price: 3200, category: "Fahrzeuge", sellerId: 5, status: "AVAILABLE", imageUrls: flickr("scooter", 2), createdAt: daysAgoIso(31) },
  ];
}

export { KATEGORIEN, seedUsers, seedProducts };
