package mx.com.blac.mobile.tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;


import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import mx.com.blac.mobile.tracker.adapter.ComandosRecibidosAdapter;
import mx.com.blac.mobile.tracker.modelos.ComandoRecibido;

public class ComandoRecibidoActivity extends AppCompatActivity {
ArrayList<ComandoRecibido> comandosRecibidos = new ArrayList<>();
ComandosRecibidosAdapter adapter;
ListView mListView;
Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comando_recibido);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ComandoRecibidoActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mListView = (ListView)findViewById(R.id.lstComandos);
        getComandosRecibidos();
    }



    public void getComandosRecibidos(){
        ArrayList<mx.com.blac.mobile.tracker.modelosDB.ComandoRecibido> dbComandos =
                new ArrayList<>();
        List<mx.com.blac.mobile.tracker.modelosDB.ComandoRecibido> list =
                new Select().from(mx.com.blac.mobile.tracker.modelosDB.ComandoRecibido.class).execute();
        dbComandos.addAll(list);
        for (int j = 0; j < dbComandos.size(); j++) {
            comandosRecibidos.add(new ComandoRecibido(
                    dbComandos.get(j).idComando,
                    dbComandos.get(j).comando,
                    dbComandos.get(j).fechaRec));
        }

        Log.d("COMM",String.valueOf(dbComandos.size()));
        Log.d("COMM",String.valueOf(comandosRecibidos.size()));

        adapter = new ComandosRecibidosAdapter(ComandoRecibidoActivity.this,comandosRecibidos);
        mListView.setAdapter(adapter);

    }
}
