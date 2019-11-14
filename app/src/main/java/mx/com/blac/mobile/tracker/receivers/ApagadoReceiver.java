package mx.com.blac.mobile.tracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import mx.com.blac.mobile.tracker.servicios.MonitoreoService;
import mx.com.blac.mobile.tracker.servicios.SincronizacionService;

/**
 * Created by RafaelCastro on 22/4/17.
 */

public class ApagadoReceiver extends BroadcastReceiver{
    private static final String TAG = "ApagadoReceiver";
    public static String APAGADO="4";
    public static String ENCENDIDO="5";
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";

    @Override
    public void onReceive(Context context, Intent intent) {
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, 0);
        if("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {

            Log.d(TAG,"Apagando teléfono");
            String lat = sharedpreferences.getString("lat","0");
            String lng = sharedpreferences.getString("lng","0");
            String _altitud =  sharedpreferences.getString("altitud","0");
            String grados  =  sharedpreferences.getString("grados","0");
            String imei = sharedpreferences.getString("imei","");
            new MonitoreoAsyncTask(sharedpreferences.getString("token",""),
                   lat,
                   lng,
                    _altitud,
                    grados,
                    APAGADO,
                    imei).execute();


        }

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            String lat = sharedpreferences.getString("lat","0");
            String lng = sharedpreferences.getString("lng","0");
            String _altitud =  sharedpreferences.getString("altitud","0");
            String grados  =  sharedpreferences.getString("grados","0");
            String imei = sharedpreferences.getString("imei","");
            new MonitoreoAsyncTask(sharedpreferences.getString("token",""),
                    lat,
                    lng,
                    _altitud,
                    grados,
                    ENCENDIDO,
                    imei).execute();

            Intent monitoreoService = new Intent(context, MonitoreoService.class);
            Intent sincroService = new Intent(context, SincronizacionService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(monitoreoService);
                context.startForegroundService(sincroService);
            }else{
                context.startService(monitoreoService);
                context.startService(sincroService);
            }



        }
    }


    public class MonitoreoAsyncTask extends AsyncTask<Integer,Void,Integer>
    {


        private String token;
        private String _lat,_lng,_altitud,_rumbo;
        String _evento,_imei;

        public MonitoreoAsyncTask(String token, String _lat, String _lng, String _altitud, String _rumbo, String _evento, String _imei) {
            this.token = token;
            this._lat = _lat;
            this._lng = _lng;
            this._altitud = _altitud;
            this._rumbo = _rumbo;
            this._evento = _evento;
            this._imei = _imei;
        }

        public MonitoreoAsyncTask() {
        }



        public int llamar()
        {
            int r=0;






            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            DateFormat df2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            Calendar calendar = Calendar.getInstance(timeZone);



            df.setTimeZone(timeZone);
            String fecha =df.format(calendar.getTime());






                try {



                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://23.21.192.188:8080/BlacWSMobileTrack/blacsolws/mobileTrack/insertEvent");
                    String params = "{" +
                            "\"username\": \"BLAC\"," +
                            "\"imei\": \""+_imei+"\"," +
                            "\"evento\": \""+_evento+"\"," +
                            "\"latitud\": \""+_lat+"\"," +
                            "\"longitud\": \""+_lng+"\"," +
                            "\"altitud\": \""+_altitud+"\"," +
                            "\"velocidad\": \"0\"," +
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
                        Log.d(TAG, stat_code);
                    }

                } catch (Exception ex) {
                    Log.d(TAG,ex.getMessage());

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



        }
    }
}
