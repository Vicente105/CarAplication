package com.example.caraplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class SincronizarBluethooth extends AppCompatActivity {

    private LinearLayout ll;
    private BluetoothAdapter bluetoothAdapter;
    private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar_bluethooth);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        ll = (LinearLayout)findViewById(R.id.ll);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no cuenta con bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // If bluetooth is off, then request to turn on
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }

        // Look for knowed devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Button button = new Button(getApplicationContext());
                button.setText(device.getName());

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        Toast.makeText(SincronizarBluethooth.this, "Sincronizando...", Toast.LENGTH_SHORT).show();

                        MainActivity.hc06 = device;

                        try {
                            MainActivity.bluetoothSocket = MainActivity.hc06.createInsecureRfcommSocketToServiceRecord(UUID_SERIAL_PORT);
                            MainActivity.bluetoothSocket.connect();

                            MainActivity.outputStream = MainActivity.bluetoothSocket.getOutputStream();

                            Toast.makeText(SincronizarBluethooth.this, "Conexión exitosa", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SincronizarBluethooth.this, MainActivity.class);
                            startActivity(intent);
                        } catch (IOException e) {
                            Toast.makeText(SincronizarBluethooth.this, "Falló en la conexión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                ll.addView(button);
            }
        }
    }
}