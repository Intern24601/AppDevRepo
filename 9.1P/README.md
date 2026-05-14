# Lost and Found Application (9.1P)

A modern, feature-rich Android application designed to help users report and discover lost or found items. This version (9.1P) focuses on advanced location-based services, including interactive map filtering and real-time address autocomplete.

## Key Features

- **Google Places Autocomplete**: integrated real-time search for addresses when creating advertisements, ensuring accurate location data.
- **Interactive Map with Radius Filtering**:
  - View all reported items as markers on a Google Map.
  - Filter items based on proximity to the user's current GPS location using a custom `NumberPicker` radius selector (e.g., "Show items within 10km").
- **Manual Map Pinning**: Use the `PickLocationFragment` to manually select a location by clicking anywhere on the map.
- **My Adverts Management**: A centralized dashboard to view, edit, and delete personal advertisements.
- **Image Support**: Attach and display identifying photos for any item.
- **Smart Filtering**: Browse the item feed with category-based filtering (e.g., Electronics, Pets, Wallets).
- **Edge-to-Edge UI**: Implements modern Android display standards for a full-screen, immersive experience.

## Technical Stack

- **Language**: Java 17 / Kotlin (StdLib support)
- **Database**: SQLite (Version 2) with a centralized data orchestration layer in `MainActivity`.
- **Maps & Location**: 
  - **Google Maps SDK for Android**: v18.2.0
  - **Google Places SDK (New)**: v3.3.0
  - **Fused Location Provider**: For precise GPS distance calculations.
- **UI Framework**: 
  - Android Jetpack (Navigation Component, Fragments)
  - **View Binding**: For type-safe view interaction.
  - **Material Design 3**: Modern UI components and styling.

## Architecture

The project follows a **Centralized Data Layer** design pattern:
- **`MainActivity`** acts as the primary orchestration hub. It handles all database operations (CRUD) and provides a clean API for Fragments to interact with data.
- **Decoupled Fragments**: UI logic is isolated within Fragments, which communicate with the activity to persist or retrieve data, ensuring a single source of truth and reducing code duplication.

## Project Structure

- **`MainActivity.java`**: The core controller. Manages navigation, database lifecycle, and shared data logic.
- **`CreateAdvertFragment.java`**: Handles new item creation with Google Places search and image selection.
- **`MapsFragment.java`**: Implements the main map view and proximity-based filtering logic.
- **`PickLocationFragment.java`**: A helper fragment for manual map-based location selection.
- **`MyAdvertsFragment.java`**: Dedicated management interface for user-created items.
- **`ItemListFragment.java`**: The main browsing feed with search and categorization.
- **`RemoveItemFragment.java`**: Item detail view with administrative actions (Delete/Edit).
- **`DatabaseHelper.java`**: Manages the SQLite schema and underlying queries.
- **`LostFoundItem.java`**: The primary data model (POJO) representing a lost or found item.

## Setup Requirements

1. **Google Cloud Console**:
   - Enable **Maps SDK for Android**.
   - Enable **Places API (New)**.
2. **API Key**: 
   - Create a valid API Key and add it to `app/src/main/java/com/app/appdev/Config.java`.
3. **Permissions**: 
   - The app requires `ACCESS_FINE_LOCATION` to calculate distances for the map radius filter.
4. **Environment**:
   - Android Studio (latest stable version recommended).
   - Minimum SDK: 24 | Target SDK: 35+

## How to Run

1. Clone the repository to your local machine.
2. Open the project in Android Studio.
3. Configure your Google Maps API Key in `Config.java`.
4. Perform a **Gradle Sync**.
5. Deploy to a physical device or an emulator with **Google Play Services** enabled.
