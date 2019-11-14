package mx.com.blac.mobile.tracker;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
TextView lnkCrearCuenta,lblHeader;
Button btnEmpezar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/PTS55F.ttf");
        lblHeader = (TextView)findViewById(R.id.lblHeader);
        lnkCrearCuenta = (TextView)findViewById(R.id.lnkCrearCuenta);
        btnEmpezar = (Button)findViewById(R.id.btnEmpezar);

        lblHeader.setTypeface(typeface);
        lnkCrearCuenta.setTypeface(typeface);
        btnEmpezar.setTypeface(typeface);

        lnkCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent monitoreo = new Intent(SplashActivity.this, RegistroActivity.class);
                startActivity(monitoreo);
                finish();
            }
        });

        btnEmpezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
}
