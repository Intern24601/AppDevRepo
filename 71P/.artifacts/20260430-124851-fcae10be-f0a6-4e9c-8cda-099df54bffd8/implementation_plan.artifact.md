# Audit and Fix for README.md Correctness

The `README.md` file was audited against the current implementation. While most features are correctly documented, a few discrepancies and a minor code bug were identified.

## Proposed Changes

### [Core Logic]

#### [MainActivity.java](file:///home/jerickson/AndroidStudioProjects/71P/app/src/main/java/com/example/a71p/MainActivity.java)

- Fix the bitwise OR operator `|` to a logical OR `||` in `onSupportNavigateUp`.

```java
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
- | super.onSupportNavigateUp();
+ || super.onSupportNavigateUp();
    }
```

#### [CreateAdvertFragment.java](file:///home/jerickson/AndroidStudioProjects/71P/app/src/main/java/com/example/a71p/CreateAdvertFragment.java)

- Enforce the image requirement mentioned in `README.md` ("Each posting requires an image").

```java
-            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
+            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty() || selectedImageUri == null) {
                 Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                 return;
             }
```

## Verification Plan

### Manual Verification
- **Code Audit**: Verify that `MainActivity.java` and `CreateAdvertFragment.java` are the only files needing changes for parity with `README.md`.
- **UI Check**: Verify `item_layout.xml` uses `MaterialCardView` (already confirmed).
- **Functional Check**: (Optional, if I could run the app) I would verify that clicking 'Save' without an image now shows the "Please fill all fields" Toast.
