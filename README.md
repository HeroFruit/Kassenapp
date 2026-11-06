# Kassenapp 🛒

Eine moderne, effiziente und benutzerfreundliche Kassenanwendung für Android, entwickelt mit **Jetpack Compose** und **Material 3**.

## 📝 Projektbeschreibung
Kassenapp ist eine Android-basierte App, die entwickelt wurde, um den Kaffee/Kuchen verkauf zu vereinfachen. Sie bietet eine intuitive Benutzeroberfläche zur schnellen Artikelwahl, Verwaltung des Warenkorbs, Abwicklung von Verkäufen sowie zur Überwachung von Umsätzen und Statistiken.

## ✨ Hauptfunktionen

### 1. Artikelverwaltung & Kachel-Grid
*   **Artikel anlegen:** Neues Artikel-Quadrat (`+ Artikel`) direkt am Ende des Rasts zum schnellen Hinzufügen mit Name und Preis.
*   **Artikel bearbeiten & löschen:** Langes Drücken auf ein Artikel-Quadrat öffnet ein Menü zum Bearbeiten von Name/Preis oder zum Löschen aus der Preisliste.
*   **Artikel-Datenbank leeren:** Vollflächig roter Button im Seitenmenü mit Sicherheitsbestätigung.

### 2. Warenkorb & „Nächster Kunde“-Checkout
*   **Warenkorb-System:** Artikel durch einfaches Antippen der Kacheln zum Warenkorb hinzufügen.
*   **Positionen anpassen:** Erhöhen (`+`), Verringern (`-`) oder komplette Zeilen entfernen (`✕`).
*   **„Nächster Kunde“ Button:** Speichert die aktuelle Bestellung mit Zeitstempel, Einzelpreisen und Gesamtsumme in die lokale Datenbank und leert den Warenkorb für den nächsten Verkauf.

### 3. Responsive Layout & Display-Verhalten
*   **Hoch- & Querformat-Anpassung:**
    *   *Portrait (Vertikal)*: Artikel-Grid oben, Warenkorb unten in voller Bildschirmbreite für optimale Lesbarkeit auf Smartphones.
    *   *Landscape (Horizontal)*: Nebeneinanderliegendes 2/3 (Grid) zu 1/3 (Warenkorb) Layout.
*   **Display wach halten (Keep Screen On):** Verhindert das automatische Sperren/Dunkelwerden des Bildschirms, solange die App aktiv genutzt wird.

### 4. Vollbild-Statistik & Multiformat-Export
*   **Tagesstatistik:** Vollbildfenster mit Anzeige der Gesamteinnahmen und automatische Gruppierung nach Datum (neueste Tage zuerst).
*   **Artikel-Aufschlüsselung:** Detaillierte Übersicht pro Tag, welcher Artikel wie oft verkauft wurde sowie der Tagesumsatz.
*   **Export in den Download-Ordner:** Per Klick auf den Button **„Export“** im Statistik-Header kann die Statistik im wählbaren Format direkt im Android **Download-Ordner** abgelegt werden:
    *   📄 **PDF-Dokument (`.pdf`)**: A4-formatiertes PDF-Dokument mit Tabelle und Einnahmen.
    *   📊 **Excel-Tabelle (`.csv`)**: UTF-8 BOM-kodierte CSV-Datei zur direkten Weiterverarbeitung in Microsoft Excel oder Google Sheets.
    *   💾 **Native Datenbank (`.json`)**: JSON-Export aller Statistikdaten.
*   **Statistik-Datenbank leeren:** Vollflächig roter Button im Seitenmenü mit Sicherheitsbestätigung.

### 5. Seitenmenü & Personalisierung
*   **Erscheinungsbild:** Umschalten zwischen **Hell (Light Mode)**, **Dunkel (Dark Mode)** und **Systemstandard**.
*   **UI-Skalierung:** Stufenloser Slider von **1,0x bis 2,0x** in 0,1-Schritten zurSkalierung der gesamten Benutzeroberfläche.
*   **Artikelquadrat-Skalierung:** Eigener Slider von **1,0x bis 2,0x** in 0,1-Schritten. Skaliert Kachelgröße, Schriftgrößen und Icons im Artikel-Grid proportional.

---

## 🛠️ Technische Details
*   **UI Framework:** 100% Jetpack Compose mit Material 3.
*   **Architektur:** MVVM (Model-View-ViewModel) mit `AndroidViewModel`.
*   **Datenpersistenz:** Lokale Datenspeicherung über `SharedPreferences` und `JSON`-Serialisierung.
*   **PDF & Datei-Export:** Native Android `PdfDocument` & `MediaStore` Integration ohne schwere Drittanbieter-Bibliotheken.
*   **App-Icon:** Eigenes vektorbasiertes Adaptive Icon im grünen Verlauf.
*   **Min SDK:** API 27 (Android 8.1) | **Target SDK:** API 37 (Android 15/16).

---

## 🚀 Installation & Start
1. Repository klonen oder als ZIP herunterladen.
2. Projekt in **Android Studio** öffnen.
3. Gradle-Sync durchführen.
4. Auf ein Android-Gerät oder einen Emulator (ab API 27) deployen und starten.
