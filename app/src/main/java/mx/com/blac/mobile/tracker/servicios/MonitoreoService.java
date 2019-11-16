package mx.com.blac.mobile.tracker.servicios;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import mx.com.blac.mobile.tracker.AppController;
import mx.com.blac.mobile.tracker.R;
import mx.com.blac.mobile.tracker.modelosDB.Monitoreo;
import mx.com.blac.mobile.tracker.receivers.AlarmaReceiver;
import mx.com.blac.mobile.tracker.utilidades.Tracker;

/**
 * Created by RafaelCastro on 11/4/17.
 */

public class MonitoreoService extends Service implements SensorEventListener,GpsStatus.Listener{
    Context context;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    String imei;
    //enventos
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
    boolean FIRST_RUN_KEY=true;


    private Timer timer = new Timer();
    double lat,lng,latAnt,lngAnt;
    int _velocidad,_altitud;
    String BASE_URL,METODO="insertEvent",METODO_TOKEN="authenticate";
    SensorManager sensorManager;
    private static final String BATT_ACTION="android.intent.action.BATTERY_CHANGED";
    private static String[] PERMISSIONS_LOCATION = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    int idChofer=-1;
    int grados;
    String viajeActivo="";
    int totalSatelites=0;
    LocationManager locationManager;
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    private static String TAG="MonitoreoService";
    Tracker tracker;
    Location  net_loc;
    int precision=0;
    int NOTIF_RASTREO=998;
    double v2;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private BroadcastReceiver bateriaReceiver=null;
    int batLevel=0;
    String evento;
    AlarmaReceiver alarma = new AlarmaReceiver();





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Generar un token para comenzar el envio de la data
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = sharedpreferences.getString("imei","");
        autenticar("BLAC",imei);
        //Arrancar la alarma para obtener el token según el periodo
        alarma.setAlarm(this);
        return START_STICKY;
     }



    public void autenticar(final String username,final String imei) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,BASE_URL+METODO_TOKEN, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("autenticar", response.getString("message"));
                            String token = response.getString("message");

                            if (token.length()>0)
                            {
                                Long tsLong = System.currentTimeMillis()/1000;
                                String tsGeneracionToken = tsLong.toString();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("token",token);
                                editor.commit();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }


        }) {
            //En curl equivale a -H username:xxyyzz
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("username",username);
                params.put("imei", imei);

                return params;
            }
        };

// add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);


    }



    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG,"Proceso iniciado...");
        imei=sharedpreferences.getString("imei","");
        new MonitoreoAsyncTask(sharedpreferences.getString("token",""),String.valueOf(lat),String.valueOf(lng),String.valueOf(_altitud),String.valueOf(grados),ENCENDIDO).execute();
    }

    @Override
    public void onDestroy()
    {
        timer.cancel();
        Log.d(TAG,"Proceso detenido...");
        new MonitoreoAsyncTask(sharedpreferences.getString("token",""),String.valueOf(lat),String.valueOf(lng),String.valueOf(_altitud),String.valueOf(grados),APAGADO).execute();
        super.onDestroy();
        alarma.cancelAlarm(this);
        unregisterReceiver(bateriaReceiver);
        //Cancelar la notificacion
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIF_RASTREO);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(BATT_ACTION);
        this.bateriaReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                batLevel = intent.getIntExtra( BatteryManager.EXTRA_LEVEL, 0 );

            }
        };
        // Registers the receiver so that your service will listen for
        // broadcasts
        this.registerReceiver(this.bateriaReceiver, theFilter);



        /*
        tracker = new Tracker(context);
        lat = tracker.getLatitude();
        lng = tracker.getLongitude();
*/

        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        if (sharedpreferences != null) {
            precision = sharedpreferences.getInt("tiempoRastreo",30);
            evento = sharedpreferences.getString("evento",REPORTE_TIEMPO_FIJO);
            imei = sharedpreferences.getString("imei","");
        }





        final String token = sharedpreferences.getString("token","");
        new MonitoreoAsyncTask(token,String.valueOf(lat),String.valueOf(lng),String.valueOf(_altitud),String.valueOf(grados),evento).execute();


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);



        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);



        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    (Activity)context,
                    PERMISSIONS_LOCATION,
                    1 );

        }


        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, precision*1000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, precision*1000, 0, listener);
        locationManager.addGpsStatusListener(this);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        locationManager.requestSingleUpdate(criteria, listener, null);


        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && network_enabled)
        {
            net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }



        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {




                if (hayInternet() ) {
                    if (lat!=0 && lng!=0) {

                        if (sharedpreferences.getBoolean("primeraCorrida",true)) {
                            sharedpreferences.edit().putBoolean("primeraCorrida",false).commit();
                            evento = INSTALL;
                        }

                        Log.d("EVENTOS",evento);
                        new MonitoreoAsyncTask(token, String.valueOf(lat), String.valueOf(lng), String.valueOf(_altitud), String.valueOf(grados), evento).execute();
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("lat",String.valueOf(lat));
                        editor.putString("lng",String.valueOf(lng));
                        editor.putString("altitud",String.valueOf(_altitud));
                        editor.putString("grados",String.valueOf(grados));


                        editor.commit();
                    }else {
                        Log.d(TAG,"Data no enviada, coordenadas 0,0");
                    }

                }else{


                    DateFormat df2= new SimpleDateFormat("yyyyMMddHHmmss");
                    TimeZone timeZone = TimeZone.getTimeZone("UTC");
                    Calendar calendar = Calendar.getInstance(timeZone);




                    String fecha =df2.format(calendar.getTime());



                    if (lat!=0 && lng!=0) {
                        Log.d("PROCESO", "Guardando data...");
                        Monitoreo monitoreo = new Monitoreo();
                        monitoreo.username = "BLAC";
                        monitoreo.imei = sharedpreferences.getString("imei", "");
                        monitoreo.evento = evento;
                        monitoreo.latitud = lat;
                        monitoreo.longitud = lng;
                        monitoreo.velocidad = _velocidad;
                        monitoreo.altitud = _altitud;
                        monitoreo.rumbo = grados;
                        monitoreo.fechaUTC = fecha;
                        Log.d("PROCESO", "Guardando data..." + String.valueOf(lat));
                        Log.d("PROCESO", "Guardando data..." + String.valueOf(monitoreo.save()));
                    }

                }
            }
        }, 0, precision * 1000);


        int NOTIFICATION_ID = 1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            //startForeground(NOTIFICATION_ID, new Notification.Builder(this).build());
            String NOTIFICATION_CHANNEL_ID = "mx.com.blac.mobile.tracker";
            String channelName = "Mi Canal";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.notif_icon)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);


        }

        mostrarNotificacion(NOTIF_RASTREO,context,"Su posición está siendo monitoreada");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        grados = (int) degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private boolean hayInternet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Si se quiere que la app lance una notificación en la barra superior
    //habilitar esta función.
    private void mostrarNotificacion(int numero, Context context, String mensaje) {


        /*
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ConfirmaRastreoActivity.class), 0);
        PendingIntent dismissIntent = NotificationActivity.getDismissIntent(numero, context);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logix_notificacion)
                        .setContentTitle("El monitoreo está activo")

                        .setContentText(mensaje);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();


        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(false);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(numero, mBuilder.build());

*/
    }




    @Override
    public void onGpsStatusChanged(int event) {
        int satellites = 0;
        int satellitesInFix = 0;

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    (Activity)context,
                    PERMISSIONS_LOCATION,
                    1 );

        }


            int timetofix = locationManager.getGpsStatus(null).getTimeToFirstFix();
            Log.i(TAG, "Time to first fix = " + timetofix);
            for (GpsSatellite sat : locationManager.getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }

        totalSatelites = satellitesInFix;
        Log.d(TAG,String.valueOf(totalSatelites));

    }



    public class MonitoreoAsyncTask extends AsyncTask<Integer,Void,Integer>
    {


        private String token;
        private String _lat,_lng,_altitud,_rumbo;
        String _evento;

        public MonitoreoAsyncTask(String token, String _lat, String _lng, String _altitud, String _rumbo, String _evento) {
            this.token = token;
            this._lat = _lat;
            this._lng = _lng;
            this._altitud = _altitud;
            this._rumbo = _rumbo;
            this._evento = _evento;
        }

        public MonitoreoAsyncTask() {
        }



        public int llamar()
        {
            int r=0;


            int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        (Activity)context,
                        PERMISSIONS_LOCATION,
                        1 );

            }


            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            DateFormat df2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);



            df.setTimeZone(timeZone);
            String fecha =df.format(calendar.getTime());





            if (totalSatelites>0) {
                try {
                    Log.d(TAG,sharedpreferences.getString("evento","0"));


                        if (batLevel >= 10 && batLevel < 20) {
                            _evento = BATT_10;
                        }
                        if (batLevel >= 20 && batLevel < 30) {
                            _evento = BATT_20;
                        }
                        if (batLevel >= 30 && batLevel < 40) {
                            _evento = BATT_30;
                        }

                    if (sharedpreferences.getString("evento","")==PANICO_TACTIL)
                    {
                        _evento=PANICO_TACTIL;
                        //Guardar hora para el panico
                        DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String fec1 = df1.format(Calendar.getInstance().getTime());
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("fechaPanico",fec1);
                        editor.commit();
                    }

                    if (sharedpreferences.getString("evento","")==PANICO_FIJO)
                    {
                        _evento=PANICO_FIJO;
                        DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String fec1 = df1.format(Calendar.getInstance().getTime());
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("fechaPanico",fec1);
                        editor.commit();
                    }



                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(BASE_URL+METODO);
                    String params = "{" +
                            "\"username\": \"BLAC\"," +
                            "\"imei\": \""+sharedpreferences.getString("imei","")+"\"," +
                            "\"evento\": \""+_evento+"\"," +
                            "\"latitud\": \""+_lat+"\"," +
                            "\"longitud\": \""+_lng+"\"," +
                            "\"altitud\": \""+_altitud+"\"," +
                            "\"velocidad\": \""+String.valueOf(_velocidad)+"\"," +
                            "\"direccion\": \""+_rumbo+"\"," +
                            "\"fechaHoraUTC\": \""+fecha+"\"" +
                            "}";


                    //Calendar calLocal = Calendar.getInstance();
                    //String fechaActual = df2.format(calLocal);
                    //SharedPreferences.Editor editor = sharedpreferences.edit();
                    //editor.putString("fechaActual",fechaActual);
                    if (_lat!="0" && _lng!="0") {
                        Log.d("MonitoreoService", params);
                        //-H en curl
                        httppost.setHeader("token", sharedpreferences.getString("token",""));
                        httppost.setHeader("Content-Type", "application/json");


                        String encoded = Base64.encodeToString(
                                params.getBytes(),
                                Base64.NO_WRAP);
                        StringEntity entity = new StringEntity(encoded);
                        //-d en curl
                        httppost.setEntity(entity);


                        HttpResponse response = httpclient.execute(httppost);
                        String resultado = EntityUtils.toString(response.getEntity());
                        JSONObject jsonObject = new JSONObject(resultado);
                        String stat_code = jsonObject.getString("status_code");
                        Log.d(TAG, stat_code);
                    }

                } catch (Exception ex) {
                   Log.d(TAG,ex.getMessage());

                }

            }

            if (network_enabled && !gps_enabled)
            {
                Log.d("NETWORK",String.valueOf(net_loc.getLatitude())+","+String.valueOf(net_loc.getLatitude()));
            }

            return r;
        }



        @Override
        protected Integer doInBackground(Integer... params) {
            return llamar();
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String fec = df.format(Calendar.getInstance().getTime());
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("fechaActual",fec);
            editor.commit();


        }
    }




    private android.location.LocationListener listener = new android.location.LocationListener() {

        private Location mLastLocation;


        @Override
        public void onLocationChanged(Location pCurrentLocation) {
            lat = pCurrentLocation.getLatitude();
            lng = pCurrentLocation.getLongitude();
            _altitud = (int)pCurrentLocation.getAltitude();
            double speed = 0;
            if (this.mLastLocation != null)
                speed = Math.sqrt(
                        Math.pow(pCurrentLocation.getLongitude() - mLastLocation.getLongitude(), 2)
                                + Math.pow(pCurrentLocation.getLatitude() - mLastLocation.getLatitude(), 2)
                ) / (pCurrentLocation.getTime() - this.mLastLocation.getTime());
            //if there is speed from location
            if (pCurrentLocation.hasSpeed())
                //get location speed
                speed = pCurrentLocation.getSpeed();
            this.mLastLocation = pCurrentLocation;
            Location loc1 = new Location("");
            loc1.setLatitude(mLastLocation.getLatitude());
            loc1.setLongitude(mLastLocation.getLongitude());

            Location loc2 = new Location("");
            loc2.setLatitude(pCurrentLocation.getLatitude());
            loc2.setLongitude(pCurrentLocation.getLongitude());

            float distanceInMeters = loc1.distanceTo(loc2);

            //velocidad = (int)(3.6*(distanceInMeters/precision));

            float v = Math.round(3.6*speed);
            //float v1 = Math.round(3.6*(distanceInMeters/precision));
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("velocidad",String.valueOf((int)v));
            editor.commit();

            _velocidad = (int)v;


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
            // TODO Auto-generated method stub

        }

    };



    public float getNivelBat()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int nivel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int escala = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float pctBateria = nivel *100f/ (float)escala;

        return pctBateria;
    }

    public String getIMEI()
    {

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    (Activity)context,
                    PERMISSIONS_LOCATION,
                    1 );

        }

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        return imei;
    }
}
