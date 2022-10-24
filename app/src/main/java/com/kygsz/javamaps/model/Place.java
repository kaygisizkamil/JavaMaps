package com.kygsz.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public
    int id;
    @ColumnInfo(name = "name")
    public
    String name;
    @ColumnInfo (name="latitude")
    public
    Double latitude;
    @ColumnInfo(name="longitude")
    public
    Double longitude;
    public Place(String name,double latitude,double longitude){
        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;
    }
}
