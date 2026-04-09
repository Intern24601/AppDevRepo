package com.example.a41p.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.a41p.data.Event;
import com.example.a41p.data.EventDao;
import com.example.a41p.data.EventDatabase;
import java.util.List;

/**
 * ViewModel for managing event-related data.
 * It provides a way for the UI to interact with the database using the DAO.
 * This class survives configuration changes (like screen rotations).
 */
public class EventViewModel extends AndroidViewModel {
    // Reference to the Data Access Object
    private final EventDao eventDao;
    // LiveData containing the list of all events
    private final LiveData<List<Event>> allEvents;

    public EventViewModel(@NonNull Application application) {
        super(application);
        // Initialize the database and DAO
        EventDatabase db = EventDatabase.getDatabase(application);
        eventDao = db.eventDao();
        // Retrieve all events from the database
        allEvents = eventDao.getAllEvents();
    }

    /**
     * Retrieves all upcoming events from the database.
     * @param currentTime The current time in milliseconds.
     * @return LiveData containing the list of upcoming events.
     */
    public LiveData<List<Event>> getUpcomingEvents(long currentTime) {
        return eventDao.getUpcomingEvents(currentTime);
    }

    /**
     * @return LiveData containing the list of all events.
     */
    public LiveData<List<Event>> getAllEvents() {
        return allEvents;
    }

    /**
     * Inserts a new event into the database on a background thread.
     */
    public void insert(Event event) {
        EventDatabase.databaseWriteExecutor.execute(() -> eventDao.insert(event));
    }

    /**
     * Updates an existing event in the database on a background thread.
     */
    public void update(Event event) {
        EventDatabase.databaseWriteExecutor.execute(() -> eventDao.update(event));
    }

    /**
     * Deletes an event from the database on a background thread.
     */
    public void delete(Event event) {
        EventDatabase.databaseWriteExecutor.execute(() -> eventDao.delete(event));
    }

    /**
     * Retrieves an event by its ID.
     */
    public LiveData<Event> getEventById(long id) {
        return eventDao.getEventById(id);
    }
}
