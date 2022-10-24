package com.kygsz.javamaps.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.kygsz.javamaps.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {
    /** @Query("SELECT * FROM Place WHERE name=:name")// no need ';' and can use name or id etc. as a paramater*/
    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getComplete();// no need ';'
    //it is better to implement using getAll
    //List<Place> places(String name)
    @Insert
    Completable insert(Place place);
    @Delete
    Completable delete(Place place);
}
