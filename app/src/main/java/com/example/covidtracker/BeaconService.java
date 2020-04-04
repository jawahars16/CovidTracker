package com.example.covidtracker;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BeaconService extends Service implements BeaconConsumer {
    private BeaconManager beaconManager;
    private BeaconTransmitter beaconTransmitter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private Beacon beacon;
    private LogRepo logRepo;
    final Handler handler = new Handler();
    private String UUID = "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6";
    private static String deviceID = java.util.UUID.randomUUID().toString();
    private static DecimalFormat df2 = new DecimalFormat("#.#");

    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logRepo = new LogRepo(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }

            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            if (mBluetoothLeAdvertiser != null) {
                initializeAdvertisment();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initializeAdvertisment() {
        beacon = new Beacon.Builder()
                .setId1(UUID)
                .setId2(deviceID)
                .setId3("2")
                .setBluetoothName("Name")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l}))
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        advertiseInLoop();
    }

    private void startAdvertising() {
        if (beaconTransmitter != null) {
            beaconTransmitter.startAdvertising(beacon);
        }
    }

    private void advertiseInLoop() {

        if (beaconTransmitter == null) {
            return;
        }

        try {
            if (beaconTransmitter.isStarted()) {
                stopAdvertising();
                loop(1000 * 20);
            } else {
                startAdvertising();
                loop(1000 * 5);
            }
        } catch (Exception e) {
            Log.e("BeaconService", e.getMessage());
            log("Advertising not supported");
        }
    }

    private void loop(long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                advertiseInLoop();
            }
        }, delay);
    }

    private void stopAdvertising() {
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    List<Beacon> beaconsList = new ArrayList<Beacon>(beacons);
                    long timestamp = System.currentTimeMillis();
                    for (Beacon beacon : beaconsList) {
//                        log("Device found about " + df2.format(beacon.getDistance()) + " meters away.");
                        LogEntry entry = new LogEntry(
                                timestamp,
                                beacon.getBluetoothName(),
                                beacon.getId2().toString(),
                                beacon.getRssi(),
                                beacon.getDistance()
                        );
                        logRepo.insert(entry);
                    }
                }
            }
        });

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
//                log("I just saw a device!");
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
//                log("I no longer see a device");
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {

            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", Identifier.fromUuid(java.util.UUID.fromString(UUID)), null, null));
        } catch (RemoteException e) {
        }
    }


    private void log(String message) {
        if (message == null) {
            return;
        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        Log.d(this.getClass().getName(), message);
    }

}
