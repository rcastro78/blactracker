package mx.com.blac.mobile.tracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

/**
 * Created by RafaelCastro on 13/4/17.
 */

public class LoginActivity extends AppCompatActivity{
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    String login;
    TextView txtUsuario;
    FloatingActionButton btnIngresar;
    String BASE_URL;
    String METODO="authenticate";
    String device_unique_id,IMEI;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
public static String TAG="LoginActivity";

    private static String[] PERMISOS = {
            Manifest.permission.READ_PHONE_STATE
    };

    private static String[] PERMISSIONS_LOCATION = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/PTS55F.ttf");
        verificaPermisos(this);
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);
        GPSActivo();
        txtUsuario = (TextView)findViewById(R.id.txtUsuario);

        btnIngresar = (FloatingActionButton)findViewById(R.id.btnIngresar);
        txtUsuario.setTypeface(typeface);


        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login = txtUsuario.getText().toString();
                trabajar();

                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);


                int permission = ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            LoginActivity.this,
                            PERMISOS,
                            1 );

                }else{
                    String imei = telephonyManager.getDeviceId();
                    //String imei="863286021048203";
                    //TODO:Métodos de validación
                    Log.d(TAG,login);
                    Log.d(TAG,imei);
                    if (login.length()>0)
                        autenticar(login,imei);
                }





            }
        });

    }


    @Override
    public void onBackPressed() {
    finish();
    }

    public void GPSActivo(){
        try{
            int gpsSignal = Settings.Secure.getInt(this.getContentResolver(),Settings.Secure.LOCATION_MODE);
            if(gpsSignal==0){
                showGPSAlert();
            }
        }catch (Settings.SettingNotFoundException ex){
            ex.printStackTrace();
        }
    }

    private void showGPSAlert(){
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Señal GPS")
                .setMessage("No tiene activo el GPS")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }


    public void trabajar() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else {

            TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = mngr.getDeviceId();
            device_unique_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);



        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_PHONE_STATE)) {
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                }

                IMEI = mngr.getDeviceId();
                device_unique_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);

                autenticar(login,IMEI);

            } else {
                //Toast.makeText(this,"ehgehfg",Toast.LENGTH_SHORT).show();
            }
        }
    }



    public void autenticar(final String username,final String imei) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,BASE_URL+METODO, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG,response.toString());
                        try {
                            Log.d(TAG, response.getString("message"));
                            String token = response.getString("message");

                            if (token.length()>0)
                            {
                                Long tsLong = System.currentTimeMillis()/1000;
                                String tsGeneracionToken = tsLong.toString();
                                //Capturar la hora de generación del token, utilizarlo para validar el tiempo
                                //Si el tiempo capturado es menor al ts actual, pedir generar de nuevo el mismo.
                                Log.w(TAG,token);


                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("token",token);
                                editor.putString("username",username);
                                editor.putString("imei",imei);
                                editor.putString("tsValidez",tsGeneracionToken);
                                editor.commit();
                                Intent home = new Intent(LoginActivity.this,HomeActivity.class);
                                startActivity(home);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG,e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                VolleyLog.e(TAG, error.getLocalizedMessage());

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


    public static void verificaPermisos(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_LOCATION,
                    1 );

        }
    }
}
