package com.example.julisarrelli.findmycar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.Set;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static android.support.v4.content.ContextCompat.createDeviceProtectedStorageContext;

/**
 * Created by julisarrelli on 10/16/16.
 */
public class AutomaticMode extends BroadcastReceiver {
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static int REQUEST_ENABLE_BT = 1;
    private Set<String>array;
    private MainActivity mainActivity;
    private String ConnectedDevice=null;
    private Set<BluetoothDevice>connectedDevices;



    public AutomaticMode() {




        if (mBluetoothAdapter == null) {
            //((MainActivity)context).alertView("Tu dispositivo no se banca bluetooth", "Error");
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.v("tag", "Connected to " + device.getName());
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //Done searching
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.v("tag", "Disconnected to " + device.getName());
    /*
    lo que no tengo ni idea es como llamar a este metodo (SaveLocation) desde aca sin que me tire un nuller pointer excepcion. Lo
  lo que tambien me indiga es que el el broadcastReciber anda sin necesidad de que yo instancie esta clase
   y como si no fuera lo suficientemente cornudo, cuando conecta, aparece como que al segundo se desconta, por ende se vuelve a romper
            */
            mainActivity.saveLocation();

        }

    }



    private void showAvailableDevices() {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        adapter.startDiscovery();




    }

    public String getConnectedDevice() {
        return ConnectedDevice;
    }
}
