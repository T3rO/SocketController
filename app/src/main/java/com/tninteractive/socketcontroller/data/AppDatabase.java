package com.tninteractive.socketcontroller.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Created by trist on 12/26/19.
 */

@Database(entities = {SocketDevice.class, OneTimeTimer.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase sInstance;

    public static final String DATABASE_NAME = "SocketController.db";

    public abstract SocketDeviceDao socketDeviceDao();

    public abstract OneTimeTimerDao oneTimeTimerDao();


    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `one_time_timer`  "
                    + " (`device_id` INTEGER, `timer_time` INTEGER, `action` INTEGER, "
                    + " PRIMARY KEY(`device_id`), "
                    + " FOREIGN KEY(`device_id`) REFERENCES `socket_device`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
    };


    public static AppDatabase getInstance(Context context){
        if(sInstance == null){
            synchronized (AppDatabase.class){
                if(sInstance == null){
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),AppDatabase.class,DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }



}
