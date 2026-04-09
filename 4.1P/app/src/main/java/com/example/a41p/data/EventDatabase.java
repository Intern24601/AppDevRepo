package com.example.a41p.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Room database for this app.
 * It provides the main access point to the persisted data.
 */
@Database(entities = {Event.class}, version = 1, exportSchema = false)
public abstract class EventDatabase extends RoomDatabase {
    
    /**
     * @return The Data Access Object for the "events" table.
     */
    public abstract EventDao eventDao();

    // Singleton instance to prevent multiple instances of the database being opened at once.
    private static volatile EventDatabase INSTANCE;
    
    // Executor service with a fixed thread pool to run database operations in the background.
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Gets the singleton instance of the EventDatabase.
     * If the database doesn't exist, it creates it using the Room database builder.
     */
    public static EventDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EventDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    EventDatabase.class, "event_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
