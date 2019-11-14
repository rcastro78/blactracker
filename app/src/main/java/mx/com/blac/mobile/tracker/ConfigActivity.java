package mx.com.blac.mobile.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import mx.com.blac.mobile.tracker.servicios.MonitoreoService;

/**
 * Created by RafaelCastro on 18/4/17.
 */

public class ConfigActivity extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    Toolbar toolbar;
    TextView lblTiempo,txtTiempo;
    Button btnGuardar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfigActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/PTS55F.ttf");
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);

        lblTiempo = (TextView)findViewById(R.id.lblTiempo);
        txtTiempo = (TextView)findViewById(R.id.txtTiempo);
        btnGuardar = (Button)findViewById(R.id.btnGuardar);
        lblTiempo.setTypeface(typeface);
        txtTiempo.setTypeface(typeface);
        btnGuardar.setTypeface(typeface);

        txtTiempo.setText(String.valueOf(sharedpreferences.getInt("tiempoRastreo",30)));
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    int tiempo = Integer.parseInt(txtTiempo.getText().toString());
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("tiempoRastreo",tiempo);
                    editor.commit();
                    stopService(new Intent(ConfigActivity.this, MonitoreoService.class));

                    //Agregado 16/10/2019
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(ConfigActivity.this, MonitoreoService.class));
                    }else{
                        startService(new Intent(ConfigActivity.this, MonitoreoService.class));
                    }


                    Toast.makeText(getApplicationContext(),"Establecido tiempo de rastreo a "+txtTiempo.getText()+" segundos",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ConfigActivity.this,HomeActivity.class);
                    startActivity(intent);

                }catch (Exception ex){
                    Toast.makeText(getApplicationContext(),"Debe introducir un tiempo válido",Toast.LENGTH_LONG).show();
                }
            }
        });


    }


}
