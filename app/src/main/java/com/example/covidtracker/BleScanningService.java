package com.example.covidtracker;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class BleScanningService extends Service {
    LogRepo logRepo;
    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothLeScanner bleScanner;
    private UUID SERVICE_UUID = UUID.fromString("6c5489d0-ee11-49a8-b4ae-1ca649e605f6");
    private UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString("7a66525f-29b3-4546-8236-c4daa7f3bf9f");
    private UUID CHARACTERISTIC_TIME_UUID = UUID.fromString("8b7d0853-1312-4c7f-bd3d-37d7c962b137");
    private UUID CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString("618dc5b8-55e0-4e3b-8203-86d13f13bcca");

    public BleScanningService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ScanCallback scannerCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            log("Device found");
            log(result.getDevice().getName());
            log(String.valueOf(result.getRssi()));
            String deviceName = result.getDevice().getName();
            String deviceHardwareAddress = result.getDevice().getAddress(); // MAC address
            int rssi = result.getRssi();
            long timestamp = System.currentTimeMillis();
            bleScanner.stopScan(scannerCallback);
            super.onScanResult(callbackType, result);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bleScanner.startScan(scannerCallback);
                }
            }, 1000 * 60);
        }

        @Override
        public void onScanFailed(int errorCode) {
            log("Scan failed " + errorCode);
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        logRepo = new LogRepo(this);
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }

            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            GattServerCallback gattServerCallback = new GattServerCallback();
            mGattServer = mBluetoothManager.openGattServer(this, gattServerCallback);

            setupServer();
            startAdvertising();

            bleScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (bleScanner != null) {
                log("Scanning started");
                bleScanner.startScan(scannerCallback);
            } else {
                log("Scanner not available");
            }
        }
        else {
            log("Bluetooth not supported");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupServer() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Write characteristic
//        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(CHARACTERISTIC_ECHO_UUID,
//                BluetoothGattCharacteristic.PROPERTY_WRITE,
//                // Somehow this is not necessary, the client can still enable notifications
//                // | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
//                BluetoothGattCharacteristic.PERMISSION_WRITE);
//
//        // Characteristic with Descriptor
//        BluetoothGattCharacteristic notifyCharacteristic = new BluetoothGattCharacteristic(CHARACTERISTIC_TIME_UUID,
//                // Somehow this is not necessary, the client can still enable notifications
//                // BluetoothGattCharacteristic.PROPERTY_NOTIFY,
//                0, 0);
//
//        BluetoothGattDescriptor clientConfigurationDescriptor = new BluetoothGattDescriptor(CLIENT_CONFIGURATION_DESCRIPTOR_UUID,
//                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
//        clientConfigurationDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//
//        notifyCharacteristic.addDescriptor(clientConfigurationDescriptor);
//
//        service.addCharacteristic(writeCharacteristic);
//        service.addCharacteristic(notifyCharacteristic);

        mGattServer.addService(service);
        log("Server started");
    }

    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            log("Advertising not supported.");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(SERVICE_UUID);
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
//                .addServiceUuid(parcelUuid)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
        log("Started advertising");
    }

    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            log("Stopped advertising");
        }
    }

    private void stopServer() {
        if (mGattServer != null) {
            mGattServer.close();
            log("Server stopped");
        }
    }

    private void restartServer() {
        stopAdvertising();
        stopServer();
        setupServer();
        startAdvertising();
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            log("Peripheral advertising started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            log("Peripheral advertising failed: " + errorCode);
        }
    };

    private void log(String message) {

        if (message == null) {
            return;
        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        Log.d(this.getClass().getName(), message);
    }

    private class GattServerCallback extends BluetoothGattServerCallback {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            log("onConnectionStateChange " + device.getAddress()
                    + "\nstatus " + status
                    + "\nnewState " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // addDevice(device);
                log("New device connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // removeDevice(device);
                log("Device disconnected");
            }
        }

        // The Gatt will reject Characteristic Read requests that do not have the permission set,
        // so there is no need to check inside the callback
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId,
                                                int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            log("onCharacteristicReadRequest " + characteristic.getUuid().toString());

//            if (BluetoothUtils.requiresResponse(characteristic)) {
//                // Unknown read characteristic requiring response, send failure
//                sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
//            }
            // Not one of our characteristics or has NO_RESPONSE property set
        }

        // The Gatt will reject Characteristic Write requests that do not have the permission set,
        // so there is no need to check inside the callback
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            log("onCharacteristicWriteRequest" + characteristic.getUuid().toString()
                    + "\nReceived: ");

            if (CHARACTERISTIC_ECHO_UUID.equals(characteristic.getUuid())) {
//                sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
//
//                // Reverse message to differentiate original message & response
//                byte[] response = ByteUtils.reverse(value);
//                characteristic.setValue(response);
//                log("Sending: " + StringUtils.byteArrayInHexFormat(response));
//                notifyCharacteristicEcho(response);
            }
        }

        // The Gatt will reject Descriptor Read requests that do not have the permission set,
        // so there is no need to check inside the callback
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device,
                                            int requestId,
                                            int offset,
                                            BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            log("onDescriptorReadRequest" + descriptor.getUuid().toString());
        }

        // The Gatt will reject Descriptor Write requests that do not have the permission set,
        // so there is no need to check inside the callback
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device,
                                             int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite,
                                             boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            log("onDescriptorWriteRequest: " + descriptor.getUuid().toString()
                    + "\nvalue: ");

//            if (CLIENT_CONFIGURATION_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
//                addClientConfiguration(device, value);
//                sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
//            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            log("onNotificationSent");
        }
    }
}
