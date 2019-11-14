package mx.com.blac.mobile.tracker.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;

import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import mx.com.blac.mobile.tracker.HomeActivity;
import mx.com.blac.mobile.tracker.R;
import mx.com.blac.mobile.tracker.modelos.Comando;
import mx.com.blac.mobile.tracker.modelosDB.ComandoRecibido;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Created by RafaelCastro on 16/6/18.
 */

public class ComandoReceiver extends BroadcastReceiver {

    String BASE_URL,METODO="getCommand";
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    ArrayList<Comando> comandos = new ArrayList<>();
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        BASE_URL = context.getString(R.string.BASE_URL_CONTROL);
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, 0);
        String token = sharedpreferences.getString("token","");
        String username = sharedpreferences.getString("username","");
        String imei = sharedpreferences.getString("imei","");

        new CmdAsyncTask(context,username,imei,token).execute();

        wl.release();
    }



    public class CmdAsyncTask extends AsyncTask<String,Integer,ArrayList<Comando>> {
        Context ctx;
        String username, imei, token;

        public CmdAsyncTask(Context ctx,String username, String imei, String token) {
            this.username = username;
            this.imei = imei;
            this.token = token;
            this.ctx = ctx;
        }

        public ArrayList<Comando> obtenerComandos(){
            String resultado="";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(null, new byte[]{});

            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(BASE_URL+METODO)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("username", username)
                    .addHeader("imei", imei)
                    .addHeader("token", token)
                    .build();



            try {
                okhttp3.Response response = client.newCall(request).execute();

                String decoded = new String(Base64.decode(response.body().string(),Base64.DEFAULT));
                Log.d("COMANDOS",decoded);

                JSONObject jsonObject = new JSONObject(decoded);
                String listCommand = jsonObject.getString("list_command");
                JSONArray jsonArray = new JSONArray(listCommand);
                if (jsonArray.length()>0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        int cmdId = jsonObject1.getInt("id");
                        String cmdName = jsonObject1.getString("command");
                        comandos.add(new Comando(cmdId, cmdName));
                        //Insertarlo en la base
                        Calendar cal = Calendar.getInstance();

                        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        String date = df.format(cal.getTime());


                        ComandoRecibido comandoRecibido = new ComandoRecibido();
                        comandoRecibido.idComando = cmdId;
                        comandoRecibido.comando = cmdName;
                        comandoRecibido.fechaRec = date;
                        comandoRecibido.save();
                        Log.d("COMANDOS", "Guardando data..." + String.valueOf(comandoRecibido.save()));
                    }

                }


            }catch (Exception ex){
                Log.d("COMANDOS",ex.getMessage());
            }
            return comandos;
        }


        @Override
        protected ArrayList<Comando> doInBackground(String... strings) {
            return obtenerComandos();
        }


        private void mostrarNotificacion(String texto) {
            PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                    new Intent(ctx, HomeActivity.class), 0);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Mobile Tracker BLAC")
                            .setAutoCancel(true)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText(texto);

            mBuilder.setContentIntent(contentIntent);
            mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            mBuilder.setAutoCancel(true);
            NotificationManager mNotificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

        }




        @Override
        protected void onPostExecute(ArrayList<Comando> c) {
            //ComandosAdapter adapter = new ComandosAdapter(EnvioComandosActivity.this,c);
            //lstComandos.setAdapter(adapter);
            if(c.size()>0) {
                for (int i = 0; i < c.size(); i++) {
                    //Toast.makeText(ctx, "Comandos recibidos: " + c.get(i).getEventoNombre(), Toast.LENGTH_LONG).show();
                    //Mostrar notificacion
                    mostrarNotificacion(c.get(i).getEventoNombre());
                }
            }


        }
    }



    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ComandoReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60*2, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, ComandoReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }



}

