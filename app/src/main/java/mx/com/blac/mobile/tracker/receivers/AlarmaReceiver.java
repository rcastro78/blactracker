package mx.com.blac.mobile.tracker.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mx.com.blac.mobile.tracker.AppController;
import mx.com.blac.mobile.tracker.R;

/**
 * Created by RafaelCastro on 18/4/17.
 */

public class AlarmaReceiver extends BroadcastReceiver{

    String BASE_URL,METODO="authenticate";
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        BASE_URL = context.getString(R.string.BASE_URL_CONTROL);
        sharedpreferences = context.getSharedPreferences(PREFS_NAME, 0);
        String imei = sharedpreferences.getString("imei","");

        autenticar("BLAC",imei);

        wl.release();
    }



    public void autenticar(final String username,final String imei) {



        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,BASE_URL+METODO, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("token", response.getString("message"));
                            String token = response.getString("message");

                            if (token.length()>0)
                            {
                                Long tsLong = System.currentTimeMillis()/1000;
                                String tsGeneracionToken = tsLong.toString();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("token",token);
                                Log.d("AlarmaReceiver","Se ha generado un nuevo token: "+token);
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


    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmaReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmaReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }



}
