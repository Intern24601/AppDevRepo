# Lost and Found Application (71P)

A modern Android application designed to help users report lost and found items. The app provides a centralized platform for creating advertisements, viewing a list of all reported items, and filtering them by category.

## Features

- Create Lost/Found Adverts: Users can post details about lost or found items, including name, phone number, description, date, and location.
- Category Management: Support for multiple categories such as Electronics, Pets, Wallets, Clothing, and Keys.
- Image Support: Users can upload and preview images for their advertisements.
- Item Listing: A comprehensive list view of all active advertisements, sorted by date.
- Detail View and Removal: Users can view specific item details and remove advertisements once the item is recovered.
- Date Selection: Integrated date picker for accurate reporting.

## Technical Stack

- Language: Java
- Database: SQLite (for local data persistence)
- UI Framework: Android Jetpack (Navigation Component, View Binding)
- Layout: XML with Material Design components
- Compatibility: Supports modern Android Edge-to-Edge display

## Architecture

The project follows a centralized data management pattern:

- MainActivity: Acts as the central orchestration hub. It manages the database connection and provides public methods (insert, delete, query) that fragments use to interact with the data layer.
- Fragments: The UI is modularized into fragments (HomeFragment, CreateAdvertFragment, ItemListFragment, RemoveItemFragment) that communicate with the database through the MainActivity to keep UI logic decoupled from data persistence.
- DatabaseHelper: A dedicated helper class for managing SQLite operations, table creation, and migrations.
- LostFoundItem: A model class representing the data structure of an advertisement.

## Project Structure

- app/src/main/java/com/example/a71p/
    - MainActivity.java: Entry point and data controller.
    - DatabaseHelper.java: SQLite database management.
    - LostFoundItem.java: Data model for items.
    - HomeFragment.java: Main landing screen.
    - CreateAdvertFragment.java: Form for adding new advertisements.
    - ItemListFragment.java: Display list of all advertisements.
    - RemoveItemFragment.java: Detail view and deletion logic.
- app/src/main/res/
    - layout/: XML layout files for activities and fragments.
    - navigation/: Navigation graph defining app flow.

## Installation and Setup

1. Clone the repository to your local machine.
2. Open the project in Android Studio (Ladybug or later recommended).
3. Ensure you have the Android SDK installed for API level 34 or higher.
4. Sync the project with Gradle files.
5. Run the application on an emulator or a physical Android device.
