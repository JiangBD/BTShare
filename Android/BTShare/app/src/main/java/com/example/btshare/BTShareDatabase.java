package com.example.btshare;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {BTDevice.class,ThisDevice.class,BTTask.class} , version = 1,exportSchema = false)
public abstract class BTShareDatabase extends RoomDatabase {
public abstract BTShareDAO BTShareDAO();
}
