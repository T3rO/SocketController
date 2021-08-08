package com.tninteractive.socketcontroller.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Created by trist on 12/26/19.
 */

@Dao
public interface SocketDeviceDao {

    @Query("SELECT * FROM socket_device")
    List<SocketDevice> getAll();

    @Query("SELECT * FROM socket_device WHERE id = :deviceId")
    SocketDevice getById(int deviceId);

    @Insert(onConflict = REPLACE)
    long[] insertAll(SocketDevice... devices);

    @Query("DELETE FROM socket_device WHERE id = :deviceId")
    int deleteDeviceById(int deviceId);

}
