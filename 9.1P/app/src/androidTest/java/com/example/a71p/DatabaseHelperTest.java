package com.example.a71p;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private DatabaseHelper dbHelper;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
    }

    @After
    public void closeDb() {
        dbHelper.close();
    }

    @Test
    public void testInsertAndRetrieve() {
        long id = dbHelper.insertItem("Lost", "Test Item", "123456", "Desc", "Apr 27", "Library", "Electronics", "");
        assertTrue(id != -1);

        Cursor cursor = dbHelper.getAllItems();
        assertTrue(cursor.moveToFirst());
        assertEquals("Test Item", cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
        cursor.close();
    }

    @Test
    public void testDelete() {
        long id = dbHelper.insertItem("Lost", "To Delete", "123456", "Desc", "Apr 27", "Library", "Electronics", "");
        dbHelper.deleteItem((int)id);

        Cursor cursor = dbHelper.getAllItems();
        boolean found = false;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)) == id) {
                    found = true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        assertTrue(!found);
    }
}
