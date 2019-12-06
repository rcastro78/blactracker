package mx.com.blac.mobile.tracker;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;

import mx.com.blac.mobile.tracker.servicios.MonitoreoService;
import mx.com.blac.mobile.tracker.utilidades.Tracker;

/**
 * Created by RafaelCastro on 14/4/17.
 */

public class MonitoreoActivity extends AppCompatActivity{

    TextView lblLastReport,txtReporteHora,lblLat,txtLatitud,lblLongitud,
            txtLongitud,lblPanico,txtPanico;
    double lat,lng;
    Button btnPanico;
    Toolbar toolbar;
    LocationManager locationManager;
    Tracker tracker;
    boolean enviandoPanico=false;
    int tRastreo=0;
    String TAG="MonitoreoActivity";
    private Timer timer = new Timer();
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    public static String REPORTE_TIEMPO_FIJO="1";
    public static String PANICO_TACTIL="2";
    public static String PANICO_FIJO="3";
    public static String APAGADO="4";
    public static String ENCENDIDO="5";
    public static String UNINSTALL="6";
    public static String INSTALL="7";
    public static String BATT_30="8";
    public static String BATT_20="9";
    public static String BATT_10="10";

    public boolean checkLocationPermission()
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    tracker = new Tracker(this);
                    lat = tracker.getLatitude();
                    lng = tracker.getLongitude();
                    Log.d(TAG,String.valueOf(lat));
                    Log.d(TAG,String.valueOf(lng));
                    if (checkLocationPermission())
                    {

                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        switch (keyCode){
            case 267:

               /* editor.putString("evento",PANICO_FIJO);
                editor.commit();
                btnPanico.setText("ENVIANDO PÁNICO");
                return true;
            */
                if (btnPanico.getText().toString().equalsIgnoreCase("ENVIAR PÁNICO"))
                {
                    tRastreo = sharedpreferences.getInt("tiempoRastreo",30);
                    editor.putString("evento",PANICO_FIJO);
                    editor.putBoolean("panico",true);
                    btnPanico.setText("ENVIANDO PÁNICO");
                    //Cambiar el tiempo a 2 seg
                    editor.putInt("tRastreo",tRastreo);
                    editor.putInt("tiempoRastreo",2);
                    editor.commit();
                    stopService(new Intent(MonitoreoActivity.this, MonitoreoService.class));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                         startForegroundService(new Intent(MonitoreoActivity.this, MonitoreoService.class));//Do something after 100ms
                        Notification.Builder builder = new Notification.Builder(this, "blac_panico")
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText("Enviando pánico")
                                .setAutoCancel(true);

                        Notification notification = builder.build();
                        tracker.startForeground(1,notification);

                    }else{
                        startService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }



                }else{
                    int antTiempoRastreo = sharedpreferences.getInt("tRastreo",30);
                    editor.putInt("tiempoRastreo",antTiempoRastreo);
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.putBoolean("panico",false);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                    stopService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }
                }
                return  true;


            case 24:




                if (btnPanico.getText().toString().equalsIgnoreCase("ENVIAR PÁNICO"))
                {
                    editor.putString("evento",PANICO_FIJO);
                    editor.putBoolean("panico",true);
                    editor.commit();
                    btnPanico.setText("ENVIANDO PÁNICO");

                }else{
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.putBoolean("panico",false);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                }
                return  true;
            /*
            *  if (btnPanico.getText().toString().equalsIgnoreCase("ENVIAR PÁNICO"))
                {
                    editor.putString("evento",PANICO_TACTIL);
                    editor.commit();
                    btnPanico.setText("ENVIANDO PÁNICO");
                }else{
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                }
            *
            * */



        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoreo);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitoreoActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/PTS55F.ttf");
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        String ev = sharedpreferences.getString("evento","");
        lblLastReport=(TextView)findViewById(R.id.lblLastReport);
        txtReporteHora=(TextView)findViewById(R.id.txtReporteHora);
        lblLat=(TextView)findViewById(R.id.lblLat);
        txtLatitud=(TextView)findViewById(R.id.txtLatitud);
        lblLongitud=(TextView)findViewById(R.id.lblLongitud);
        txtLongitud=(TextView)findViewById(R.id.txtLongitud);
        lblPanico=(TextView)findViewById(R.id.lblPanico);
        txtPanico=(TextView)findViewById(R.id.txtPanico);


        btnPanico = (Button)findViewById(R.id.btnPanico);

        if (ev.equalsIgnoreCase(PANICO_TACTIL) || ev.equalsIgnoreCase(PANICO_FIJO))
        {
            btnPanico.setText("ENVIANDO PÁNICO");
        }else{
            btnPanico.setText("ENVIAR PÁNICO");
        }


        btnPanico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedpreferences.edit();

                if (btnPanico.getText().toString().equalsIgnoreCase("ENVIAR PÁNICO"))
                {
                    tRastreo = sharedpreferences.getInt("tiempoRastreo",30);
                    editor.putString("evento",PANICO_FIJO);
                    editor.putBoolean("panico",true);
                    btnPanico.setText("ENVIANDO PÁNICO");
                    //Cambiar el tiempo a 2 seg
                    editor.putInt("tRastreo",tRastreo);
                    editor.putInt("tiempoRastreo",2);
                    editor.commit();
                    stopService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }

                }else{
                    int antTiempoRastreo = sharedpreferences.getInt("tRastreo",30);
                    editor.putInt("tiempoRastreo",antTiempoRastreo);
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.putBoolean("panico",false);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                    stopService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(MonitoreoActivity.this, MonitoreoService.class));
                    }

                }
            }
        });


        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000*10);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtReporteHora.setText(sharedpreferences.getString("fechaActual","No hay datos..."));
                                txtPanico.setText(sharedpreferences.getString("fechaPanico","No hay datos..."));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();





        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 0, listener);



        lblLastReport.setTypeface(typeface);
        txtReporteHora.setTypeface(typeface);
        lblLat.setTypeface(typeface);
        txtLatitud.setTypeface(typeface);
        lblLongitud.setTypeface(typeface);
        txtLongitud.setTypeface(typeface);
        lblPanico.setTypeface(typeface);
        txtPanico.setTypeface(typeface);


        txtLongitud.setText(String.valueOf(lng));
        txtLatitud.setText(String.valueOf(lat));

        //TODO:Cargar datos del último reporte y último pánico

    }



    private android.location.LocationListener listener = new android.location.LocationListener() {




        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG,"La velocidad ha cambiado");
            double dSpeed = location.getSpeed() * 3.6;
            Log.d(TAG,String.valueOf(dSpeed));
            float bearing = location.getBearing();
            if(location.hasSpeed()) {
                //velocidad = (int)(dSpeed*3.6);
                //lblVelocidad.setText(String.valueOf((int)dSpeed));
                //lblGrados.setText(String.valueOf(bearing));
            } else {
                //lblVelocidad.setText("0");
            }

            //lblCoordenadas.setText(String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));

            txtLatitud.setText(String.valueOf(location.getLatitude()));
            txtLongitud.setText(String.valueOf(location.getLongitude()));

        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status)
            {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("DEBUG", provider + " out of service");
                    //Toast.makeText(this, provider + " fuera de servicio", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("DEBUG", provider + " temp. unavailable");
                    //Toast.makeText(this, provider + " no disponible", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.AVAILABLE:
                    Log.d("DEBUG", provider + " available");
                    //Toast.makeText(this, provider + " disponible", Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }

    };


    @Override
    public void onBackPressed() {

    }
}
