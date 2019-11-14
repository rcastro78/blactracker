package mx.com.blac.mobile.tracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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

import mx.com.blac.mobile.tracker.adapter.NavigationHomeAdapter;
import mx.com.blac.mobile.tracker.servicios.ComandoService;
import mx.com.blac.mobile.tracker.servicios.MonitoreoService;
import mx.com.blac.mobile.tracker.servicios.SincronizacionService;

/**
 * Created by RafaelCastro on 13/4/17.
 */

public class HomeActivity extends AppCompatActivity{
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout Drawer;
    Toolbar toolbar;
    Button btnPanico;
    ActionBarDrawerToggle mDrawerToggle;
    SharedPreferences sharedpreferences;
    public static String PANICO_TACTIL="2";
    public static String PANICO_FIJO="3";
    public static String REPORTE_TIEMPO_FIJO="1";
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    String login,pass;
    int auth=0;
    int idUsuario=0;
    String BASE_URL;
    String METODO="authenticate";
    boolean enviandoPanico;
    TextView txtQueTecla;
    int tRastreo=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);
        GPSActivo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService(new Intent(HomeActivity.this, ComandoService.class));
            startForegroundService(new Intent(HomeActivity.this, MonitoreoService.class));
            //startForegroundService(new Intent(HomeActivity.this, SincronizacionService.class));
        }else{
            //startService(new Intent(HomeActivity.this, ComandoService.class));
            startService(new Intent(HomeActivity.this, MonitoreoService.class));
            //startService(new Intent(HomeActivity.this, SincronizacionService.class));
        }





        String ev = sharedpreferences.getString("evento","");
        String MENU[]={"Monitoreo","Configuración"};
        int iconos[]={
                R.drawable.ic_place_white_36dp,
                R.drawable.ic_settings_applications_white_36dp
        };
        if (sharedpreferences!=null) {
            login = sharedpreferences.getString("login", "");
            pass = sharedpreferences.getString("pass", "");
            idUsuario = sharedpreferences.getInt("idusuario",-1);
            auth = sharedpreferences.getInt("auth",-1);
            enviandoPanico = sharedpreferences.getBoolean("panico",false);
        }
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


        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("imei",imei);
        editor.commit();
        autenticar("BLAC",imei);
        if (auth==0)
        {
            finish();
        }

        //Toast.makeText(getApplicationContext(),login,Toast.LENGTH_LONG).show();
        mAdapter = new NavigationHomeAdapter(MENU,iconos,"Menú Principal","",0,this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);
        // Letting the system know that the list objects are of fixed size


        // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture
        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView
        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manage
        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();



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
                    stopService(new Intent(HomeActivity.this, MonitoreoService.class));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }



                }else{
                    int antTiempoRastreo = sharedpreferences.getInt("tRastreo",30);
                    editor.putInt("tiempoRastreo",antTiempoRastreo);
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.putBoolean("panico",false);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                    stopService(new Intent(HomeActivity.this, MonitoreoService.class));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }


                }
            }
        });

     /*    txtQueTecla = (TextView)findViewById(R.id.txtQueTecla);

        txtQueTecla.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                txtQueTecla.setText(String.valueOf(keyCode));
                return true;
            }
        });

    */
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


    @Override
    protected void onPause() {
        super.onPause();
        finish();
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
                    stopService(new Intent(HomeActivity.this, MonitoreoService.class));

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }


                }else{
                    int antTiempoRastreo = sharedpreferences.getInt("tRastreo",30);
                    editor.putInt("tiempoRastreo",antTiempoRastreo);
                    editor.putString("evento",REPORTE_TIEMPO_FIJO);
                    editor.putBoolean("panico",false);
                    editor.commit();
                    btnPanico.setText("ENVIAR PÁNICO");
                    stopService(new Intent(HomeActivity.this, MonitoreoService.class));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(HomeActivity.this, MonitoreoService.class));
                    }else{

                        startService(new Intent(HomeActivity.this, MonitoreoService.class));
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
    public void onBackPressed() {

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
                                //Capturar la hora de generación del token, utilizarlo para validar el tiempo
                                //Si el tiempo capturado es menor al ts actual, pedir generar de nuevo el mismo.



                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("token",token);
                                editor.putString("tsValidez",tsGeneracionToken);
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

}
