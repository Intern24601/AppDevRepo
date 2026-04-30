# Test Results Report

This document outlines the testing performed on the Lost and Found application, including automated database verification and manual UI walkthrough plans.

## Automated Testing

The core data persistence layer was verified using Android Instrumented Tests.

### DatabaseHelperTest
- testInsertAndRetrieve: PASSED
- testDelete: PASSED
- testGetAllItems: PASSED

**Summary:** The SQLite database correctly handles insertion, retrieval, and deletion of lost and found items.

## Manual UI Verification Plan

The following manual tests were planned to verify the user interface and navigation.

### 1. App Launch and Home Screen
- Verify that the application launches to the Home screen.
- Verify that "CREATE A NEW ADVERT" and "SHOW ALL LOST & FOUND ITEMS" buttons are visible and functional.

### 2. Create New Advert
- Navigate to the "Create New Advert" screen.
- Enter details for a new item (Name, Phone, Description, Date, Location).
- Select a category (e.g., Electronics, Clothing).
- Save the advert and verify that it persists.

### 3. List and Details View
- Navigate to the "Lost & Found Items" list.
- Verify that the previously created item appears in the list.
- Click on an item to view its details.
- Verify that the details screen displays the correct information.

### 4. Item Removal
- From the item details screen, trigger the removal action.
- Verify that the item is successfully removed from the database and the list view.

## Conclusion

The application demonstrates a functional data layer with passing automated tests. The UI structure is correctly defined in the navigation graph and layout files to support the full lifecycle of lost and found advertisements.
