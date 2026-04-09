package com.example.a41p.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for the "events" table.
 * Defines the methods used to interact with the database.
 */
@Dao
public interface EventDao {
    /**
     * Retrieves all upcoming events from the database, ordered by their date and time.
     * @param currentTime The current time in milliseconds.
     * @return LiveData containing a list of all upcoming events.
     */
    @Query("SELECT * FROM events WHERE dateTime >= :currentTime ORDER BY dateTime ASC")
    LiveData<List<Event>> getUpcomingEvents(long currentTime);

    /**
     * Retrieves all events from the database, ordered by their date and time.
     * @return LiveData containing a list of all events.
     */
    @Query("SELECT * FROM events ORDER BY dateTime ASC")
    LiveData<List<Event>> getAllEvents();

    /**
     * Inserts a new event into the database. 
     * If an event with the same ID already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Event event);

    /**
     * Updates an existing event in the database.
     */
    @Update
    void update(Event event);

    /**
     * Deletes an event from the database.
     */
    @Delete
    void delete(Event event);

    /**
     * Retrieves a single event by its unique ID.
     * @param id The ID of the event to retrieve.
     * @return LiveData containing the requested event.
     */
    @Query("SELECT * FROM events WHERE id = :id")
    LiveData<Event> getEventById(long id);
}
