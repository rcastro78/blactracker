package mx.com.blac.mobile.tracker.servicios;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import mx.com.blac.mobile.tracker.R;
import mx.com.blac.mobile.tracker.modelosDB.Monitoreo;

/**
 * Created by RafaelCastro on 20/4/17.
 */
//Este servicio es para enviar la data que se tenga almacenada
//cuando la app guardó sin tener internet

public class SincronizacionService extends Service{
   static String TAG="SincronizacionService";
    String BASE_URL,METODO="insertEvent";
    Context context;
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    int precision=0;
    String evento="",imei;
    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG,"Proceso iniciado...");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.d(TAG,"Proceso detenido...");
    }


    private boolean hayInternet() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = "";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // We do not have this permission. Let's ask the user
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //getDeviceId() is Deprecated so for android O we can use getImei() method
            imei = telephonyManager.getImei();
        }
        else {
            imei = telephonyManager.getDeviceId();
        }
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        if (sharedpreferences != null) {
            precision = sharedpreferences.getInt("tiempoRastreo",30);
            evento = sharedpreferences.getString("evento","1");

        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (hayInternet())
                    new SubirDataAsyncTask().execute();
            }
        }, 0, precision * 1000);



    }
    //Función para enviar datos que se tengan guardados
    public class SubirDataAsyncTask extends AsyncTask<Integer,Void,Integer>
    {
        ArrayList<Monitoreo> monitoreos;
        @Override
        protected Integer doInBackground(Integer... params) {
            llenaDatosMonitoreo();
            Log.d(TAG,"Total de monitoreos offline: "+String.valueOf(monitoreos.size()));

            return null;
        }





        public void llenaDatosMonitoreo()
        {
            monitoreos = new ArrayList<>();
            monitoreos.clear();
            //Seleccionar todos los datos del monitoreo guardados en la db
            List<Monitoreo> m = new Select().from(Monitoreo.class).execute();
            m.add(new Monitoreo());
            monitoreos.addAll(m);
            llamar();
        }

        public int llamar() {
            int r = 0;
            String token = sharedpreferences.getString("token","");
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(BASE_URL+METODO);
            //Select de los datos
            try {
            for (int i=0; i<monitoreos.size(); i++)
            {
                long id = monitoreos.get(i).getId();

                    String params = "{" +
                            "\"username\": \"" + monitoreos.get(i).username + "\"," +
                            "\"imei\": \""+imei+"\"," +
                            "\"evento\": \"" + monitoreos.get(i).evento + "\"," +
                            "\"latitud\": \"" + monitoreos.get(i).latitud + "\"," +
                            "\"longitud\": \"" + monitoreos.get(i).longitud + "\"," +
                            "\"altitud\": \"" + monitoreos.get(i).altitud + "\"," +
                            "\"velocidad\": \"" + String.valueOf(monitoreos.get(i).velocidad) + "\"," +
                            "\"direccion\": \"" + monitoreos.get(i).rumbo + "\"," +
                            "\"fechaHoraUTC\": \"" + monitoreos.get(i).fechaUTC + "\"" +
                            "}";
                    Log.d(TAG,params);

                    httppost.setHeader("token", token);
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
                    Log.d(TAG,resultado);
                    //Respuesta exitosa, borrar el objeto
                    if (stat_code.equalsIgnoreCase("201"))
                    {
                        Log.d(TAG,"Borrado: "+String.valueOf(id));
                        new Delete().from(Monitoreo.class).where("id="+id).execute();
                    }


                }

            }catch (Exception ex){

            }
            return r;
        }


    }

}
