package com.tninteractive.socketcontroller.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface OneTimeTimerDao {

    @Query("SELECT * FROM one_time_timer WHERE device_id = :deviceId")
    OneTimeTimer getOneTimeTimerByDeviceId(int deviceId);

    @Insert(onConflict = REPLACE)
    long[] insertAll(OneTimeTimer... timers);

}
