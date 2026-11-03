# Kassenapp 🛒

Eine moderne, effiziente und benutzerfreundliche Kassenanwendung für Android, entwickelt mit Jetpack Compose.

## 📝 Projektbeschreibung
Kassenapp ist eine Android-basierte App, die entwickelt wurde, um den Kaffee/Kuchen verkauf zu vereinfachen. Sie bietet eine intuitive Benutzeroberfläche zur schnellen Artikelwahl, Verwaltung des Warenkorbs, Abwicklung von Verkäufen sowie zur Überwachung von Umsätzen und Statistiken.

## ✨ Hauptfunktionen

### 1. Produkt- und Artikelverwaltung
*   **Artikel anlegen:** Schnelles Hinzufügen neuer Artikel mit Name und Preis.
*   **Artikel bearbeiten & löschen:** Flexibles Anpassen bestehender Artikelnamen und -preise sowie Entfernen nicht mehr benötigter Artikel.
*   **Datenbank-Verwaltung:** Option zum vollständigen Zurücksetzen der Artikel-Datenbank.

### 2. Warenkorb & Kassenprozess (Checkout)
*   **Warenkorb-System:** Artikel durch einfaches Antippen zum Warenkorb hinzufügen.
*   **Mengenanpassung:** Erhöhen, Verringern oder Entfernen von Positionen im Warenkorb.
*   **Checkout:** Schnelles Abschließen des Verkaufs mit automatischer Berechnung der Gesamtsumme und Erstellung einer Transaktionshistorie.

### 3. Transaktionen & Statistik
*   **Umsatzübersicht:** Anzeige des Gesamtumsatzes und der durchgeführten Verkäufe.
*   **Verlauf:** Detaillierte Auflistung vergangener Transaktionen mit Zeitstempel, gekauften Artikeln und Einzel-/Gesamtpreisen.
*   **Statistik-Verwaltung:** Option zum Zurücksetzen der Transaktionshistorie.

### 4. Einstellungen & Anpassbarkeit
*   **Design-Modi:** Unterstützung für Hellmodus, Dunkelmodus (Dark Mode) sowie den Systemstandard.
*   **UI-Skalierung:** Stufenlose Anpassung der Benutzeroberflächen-Skalierung für optimale Lesbarkeit und Bedienbarkeit.
*   **Artikel-Kachel-Skalierung:** Individuelle Größenanpassung der Artikelkacheln.

## 🛠️ Technische Details & Architektur
*   **UI Framework:** 100% Jetpack Compose für moderne, deklarative Benutzeroberflächen.
*   **Architektur:** MVVM (Model-View-ViewModel) mit AndroidViewModel.
*   **Datenpersistenz:** Lokale Datenspeicherung über SharedPreferences und JSON-Serialisierung.
*   **Sprache:** Kotlin.

## 🚀 Erste Schritte
APK in Releases herunterladen.

Oder
1. Projekt herunterladen und in Android Studio öffnen.
2. Gradle-Sync durchführen.
3. App auf einem Emulator oder einem physischen Android-Gerät (ab API-Level 27) starten.
