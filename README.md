# Kassenapp 🛒

Eine moderne, effiziente und benutzerfreundliche Kassenanwendung für Android, entwickelt mit **Jetpack Compose** und **Material 3**.

## 📝 Projektbeschreibung
Kassenapp ist eine Android-basierte Anwendung, die speziell zur schnellen und einfachen Abwicklung von Verkäufen (z. B. für Kioske, Vereinsfeste, Kaffeeausschank oder Veranstaltungen) entwickelt wurde. Sie bietet eine intuitive Benutzeroberfläche zur schnellen Artikelauswahl, Warenkorbverwaltung mit Rückgeldrechner, Transaktionsspeicherung sowie zur Auswertung und zum Multiformat-Export von Verkaufsstatistiken.

---

## ✨ Hauptfunktionen

### 1. Artikelverwaltung & Kachel-Grid
*   **Artikel anlegen:** Neues Artikel-Quadrat (`+ Artikel`) direkt am Ende des Grids zum schnellen Hinzufügen von Artikeln mit Name und Preis.
*   **Artikel bearbeiten & löschen:** Langes Drücken auf ein Artikel-Quadrat öffnet ein Kontextmenü zum Anpassen von Name/Preis oder zum Entfernen aus der Preisliste.
*   **Proportionale Kachel-Skalierung:** Skaliert Kachelgröße, Schriftgrößen, Abstände und Icons im Artikel-Grid synchron.
*   **Artikel-Datenbank leeren:** Vollflächig roter Button im Seitenmenü mit Sicherheitsbestätigung.

### 2. Warenkorb & Rückgeldrechner
*   **Warenkorb-System:** Artikel durch einfaches Antippen der Kacheln direkt zum Warenkorb hinzufügen.
*   **Optimierte Buttons:** Komfortabel breite, gut tippbare Schaltflächen für **`-`** (Menge verringern) und **`✕`** (Zeile entfernen).
*   **Rückgeldrechner:** Ein Antippen des **Gesamtpreis-Feldes** öffnet den Rückgeldrechner (adaptiv für Hoch- und Querformat):
    *   Kacheln für Geldscheine und Münzen (`50 €` bis `0,10 €`) mit Zähler-Schildchen (`1x`, `2x`...).
    *   Schnellauswahl-Buttons für *„Passt genau“* und *„Gegeben zurücksetzen“*.
    *   Automatische Berechnung des Rückgelds bzw. des noch zu zahlenden Restbetrags.
*   **„Nächster Kunde“ Button:** Speichert die aktuelle Bestellung mit Zeitstempel, Einzelpreisen und Gesamtsumme in die lokale Datenbank, schließt den Verkauf ab und leert den Warenkorb.

### 3. Responsive Layout, Rotationsschutz & Display-Verhalten
*   **Hoch- & Querformat-Anpassung:**
    *   *Portrait (Vertikal)*: Artikel-Grid oben, Warenkorb unten in voller Bildschirmbreite.
    *   *Landscape (Horizontal)*: Nebeneinanderliegendes 2/3 (Grid) zu 1/3 (Warenkorb) Layout.
    *   *Rückgeldrechner*: Zweispaltiges Layout im Querformat (Geld-Kacheln links, Beträge & Checkout rechts).
*   **Rotationsschutz:** Beim Drehen des Smartphones bleiben geöffnete Fenster (Rückgeldrechner, Statistik, Menü, Dialoge) erhalten und passen ihr Layout nahtlos an.
*   **Display wach halten (Keep Screen On):** Verhindert das automatische Sperren/Dunkelwerden des Bildschirms, solange die Kassenapp aktiv genutzt wird.

### 4. Vollbild-Statistik, Tagesbearbeitung & Export
*   **Tagesstatistik:** Vollbildfenster mit Gesamteinnahmen und automatischer Gruppierung der Verkäufe nach Datum (neueste Tage zuerst).
*   **Tagesdaten bearbeiten & löschen (Langes Drücken):**
    *   *Tag löschen*: Entfernt nach Bestätigungsabfrage alle Verkäufe eines einzelnen Tages aus der Statistik.
    *   *Tag bearbeiten*: Anpassen der verkauften Mengen pro Artikel oder nachträgliches Hinzufügen weiterer Datenbank-Artikel (`+ Artikel hinzufügen`).
*   **Echtzeit-Aktualisierung:** Änderungen in der Statistik werden sofort beim Speichern oder Löschen reflektiert.
*   **Export in den Download-Ordner:** Per Klick auf den **„Export“**-Button im Statistik-Header kann die Statistik direkt im Android **Download-Ordner** abgelegt werden:
    *   📄 **PDF-Dokument (`.pdf`)**: A4-formatiertes PDF-Dokument mit Tabelle und Einnahmen.
    *   📊 **Excel-Tabelle (`.csv`)**: UTF-8 BOM-kodierte CSV-Datei zur direkten Weiterverarbeitung in Microsoft Excel oder Google Sheets.
    *   💾 **Native Datenbank (`.json`)**: JSON-Export aller Statistikdaten.
*   **Statistik-Datenbank leeren:** Vollflächig roter Button im Seitenmenü mit Sicherheitsbestätigung.

### 5. Seitenmenü & Personalisierung
*   **Erscheinungsbild:** Umschalten zwischen **Hell (Light Mode)**, **Dunkel (Dark Mode)** und **Systemstandard**.
*   **UI-Skalierung:** Stufenloser Slider von **1,0x bis 2,0x** in 0,1-Schritten zur Skalierung der gesamten Benutzeroberfläche.
*   **Artikelquadrat-Skalierung:** Eigener Slider von **1,0x bis 2,0x** in 0,1-Schritten.

---

## 🛠️ Technische Details
*   **UI Framework:** 100% Jetpack Compose mit Material 3.
*   **Architektur:** MVVM (Model-View-ViewModel) mit `AndroidViewModel`.
*   **Datenpersistenz:** Lokale Datenspeicherung über `SharedPreferences` und `JSON`-Serialisierung.
*   **State-Retention:** Kaskadierender Rotationsschutz via `rememberSaveable` und Android-Config-Changes.
*   **PDF & Datei-Export:** Native Android `PdfDocument` & `MediaStore` Integration ohne schwere Drittanbieter-Bibliotheken.
*   **App-Icon:** Eigenes vektorbasiertes Adaptive Icon im grünen Verlauf.
*   **Min SDK:** API 27 (Android 8.1) | **Target SDK:** API 37 (Android 15/16).

---

## 🚀 Installation & Start
1. Repository klonen oder als ZIP herunterladen.
2. Projekt in **Android Studio** öffnen.
3. Gradle-Sync durchführen.
4. Auf ein Android-Gerät oder einen Emulator (ab API 27) deployen und starten.
