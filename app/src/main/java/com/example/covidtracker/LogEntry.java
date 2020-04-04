package com.example.covidtracker;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logs")
public class LogEntry {

    public LogEntry(long createdAt, String deviceName, String deviceAddress, int rssi, double distance) {
        this.createdAt = createdAt;
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.rssi = rssi;
        this.distance = distance;
    }

    @PrimaryKey(autoGenerate = true)
    protected long id;

    @ColumnInfo(name = "timestamp")
    private long createdAt;

    @ColumnInfo(name = "devicename")
    private String deviceName;

    @ColumnInfo(name = "deviceaddress")
    private String deviceAddress;

    @ColumnInfo(name = "rssi")
    private int rssi;

    @ColumnInfo(name = "distance")
    private double distance;

    public double getDistance() {
        return distance;
    }

    public int getRssi() {
        return rssi;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getId() {
        return id;
    }
}
