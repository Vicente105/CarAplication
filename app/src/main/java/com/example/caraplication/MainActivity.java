package com.example.caraplication;


import androidx.appcompat.app.AppCompatActivity;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;


import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    ImageView fondo, carro;
    TextView coordenada;

    public static BluetoothDevice hc06;
    public static BluetoothSocket bluetoothSocket;
    public static OutputStream outputStream;

    private float initialX; // Posición inicial X del carro
    private float initialY; // Posición inicial Y del carro
    private float initialZ; // Posición inicial Z del carro

    private static final float THRESHOLD = 1.5f;
    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int RELEASED = 4;


    private float sensitivity = 10.0f; // Sensibilidad ajustable

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private int lastDirection = RELEASED;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        coordenada = (TextView) findViewById(R.id.TVcoor);
        carro = (ImageView) findViewById(R.id.IMcarro);
        fondo = (ImageView) findViewById(R.id.IVfondo);

        Glide.with(this)
                .asGif()
                .load(R.drawable.pista)
                .into(fondo);



// Obtener las dimensiones de la pantalla
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Obtener el tamaño de la imagen del carro
        int carroWidth = carro.getDrawable().getIntrinsicWidth();
        int carroHeight = carro.getDrawable().getIntrinsicHeight();

        // Calcular la posición inicial del carro en la mitad de la pantalla
        initialX = (screenWidth - carroWidth) / 2;
        initialY = (screenHeight - carroHeight) / 2;

        // Obtener la posición inicial Z del carro
        initialZ = carro.getTranslationZ();

        // Inicializar el SensorManager y el gyroscopeSensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el SensorEventListener para el gyroscopeSensor
        if (sensorManager != null && gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener la actualización del gyroscopeSensor al poner en pausa la actividad
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Obtener los valores del giroscopio
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            String coordinates = "X: " + x + "\nY: " + y + "\nZ: " + z;
            // coordenada.setText(coordinates);


            // Calcular el desplazamiento en función de los valores del giroscopio y la sensibilidad ajustable
            float displacementX = x * sensitivity;
            float displacementY = y * sensitivity;
            float displacementZ = z * sensitivity;

            // Calcular la nueva posición del carro
            float newX = carro.getX() + displacementX;
            float newY = carro.getY() + displacementY;
            float newZ = initialZ + displacementZ;

            // Actualizar la posición de la imagen del carro
            carro.setX(newX);
            carro.setY(newY);
            carro.setTranslationZ(newZ);

            // Obtener la dirección basada en los valores del giroscopio
           // int direction = getDirectionFromGyroscope(x, y, z);

            // Verificar si la dirección ha cambiado desde la última vez
            int direction = getDirectionFromGyroscope(x,y,z);
            if (lastDirection != direction) {
                switch (direction) {
                    case UP:
                        coordenada.setText("ARRIBA");
                        enviarMsjBt((byte) 52);
                        break;
                    case DOWN:
                        coordenada.setText("ABAJO");
                        enviarMsjBt((byte) 51);
                        break;
                    case LEFT:
                        coordenada.setText("IZQUIERDA");
                        enviarMsjBt((byte) 49);
                        break;
                    case RIGHT:
                        coordenada.setText("DERECHA");
                        enviarMsjBt((byte) 50);
                        break;
                    case RELEASED:
                        coordenada.setText("ALTO");
                        enviarMsjBt((byte) 53);
                        break;
                }
                lastDirection = direction;
            }



        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No es necesario implementar esto en este caso
    }



    private int getDirectionFromGyroscope(float x, float y, float z) {

        if (x > 1.0f){
            coordenada.setText("ARRIBA");
            enviarMsjBt((byte) 52);
        }else if(x < -1.0f){
            coordenada.setText("IZQUIERDA");
            enviarMsjBt((byte) 49);
        }else if(y > 1.0f){
            coordenada.setText("ARRIBA");
            enviarMsjBt((byte) 52);
        }else if(y < -1.0f){
            coordenada.setText("ABAJO");
            enviarMsjBt((byte) 51);
        }else {
            coordenada.setText("ALTO");
            enviarMsjBt((byte) 53);
        }
        return lastDirection;
    }



    private void enviarMsjBt(byte b) {
        if (MainActivity.outputStream != null) {
            try {
                MainActivity.outputStream.write(b);
            } catch (IOException e) {
                Toast.makeText(this, "Fallo al enviar el mensaje", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No existe conexión con el dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }


}