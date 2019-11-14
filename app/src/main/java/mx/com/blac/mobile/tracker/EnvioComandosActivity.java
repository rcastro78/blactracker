package mx.com.blac.mobile.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import mx.com.blac.mobile.tracker.adapter.ComandosAdapter;
import mx.com.blac.mobile.tracker.modelos.Comando;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class EnvioComandosActivity extends AppCompatActivity {
ArrayList<Comando> eventos = new ArrayList<>();
ListView lstComandos;
Toolbar toolbar;
private String METODO="insertEvent";
private String BASE_URL;
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
static String TAG="EnvioComandosActivity";
int OK=201;


int cmd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_comandos);
        lstComandos = (ListView)findViewById(R.id.lstEventos);
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EnvioComandosActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        final String token = sharedpreferences.getString("token","");
        final String username = sharedpreferences.getString("username","");
        final String imei = sharedpreferences.getString("imei","");

        Log.d("BLAC",token);
        Log.d("BLAC",username);
        Log.d("BLAC",imei);


        //new CmdAsyncTask(username,imei,token).execute();
        ComandosAdapter adapter = new ComandosAdapter(EnvioComandosActivity.this,llenaComandos());
        lstComandos.setAdapter(adapter);

        lstComandos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = lstComandos.getItemAtPosition(position);
                Comando comando = (Comando)o;
                cmd = comando.getIdEvento();
                Log.d("ENVIOEVENTOS",String.valueOf(cmd));
                //Codificar en base64

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String fechaUTC = dateFormat.format(cal.getTime());

                String body = "{\n" +
                        "\"username\": \""+username+"\",\n" +
                        "\"imei\": \""+imei+"\",\n" +
                        "\"evento\": \""+cmd+"\",\n" +
                        "\"latitud\": \""+sharedpreferences.getString("lat","0.0")+"\",\n" +
                        "\"longitud\": \""+sharedpreferences.getString("lng","0.0")+"\",\n" +
                        "\"altitud\": \""+sharedpreferences.getString("altitud","0.0")+"\",\n" +
                        "\"velocidad\": \""+sharedpreferences.getString("velocidad","0")+"\",\n" +
                        "\"direccion\": \""+sharedpreferences.getString("grados","0.0")+"\",\n" +
                        "\"fechaHoraUTC\":\""+fechaUTC+"\"\n" +
                        "}";

                try {
                    byte[] data = body.getBytes("UTF-8");
                    String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
                    Log.d("ENVIOEVENTOS",base64.trim());
                    new CmdAsyncTask(token,base64.trim()).execute();

                }catch (Exception ex){

                }

            }
        });
    }





    public ArrayList<Comando> llenaComandos(){



        //curl -H "Content-Type: application/json" -H "token:eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjpudWxsLCJjdHkiOiJKV1QifQ..vQ2e8POubrCAuc9ZrOKk3w.0lYImzWOdr2kcZUuintwvJQCV5Jvkyu4KgrYhaMpoQPuzdfExUGfuKOkkxTcGBAY_wDnPT4LP-4HkqjN0fxdGjN2NtL86IH0PLaLGLJ4I7dn0BdacYM5Mz3OOozQchYXLIyWo4BEo7agOFyLDNZxPRlTbDX94XGwJ2p71ZzXpa5sTt47ND-_wRFpnywihQEd77VZnNBN-Hkv_EftWiOU93NS2iyfUHDK5w9NgI3ImdCu6n5eb4NqvkgwMVs_nBSqFKogC1zd-nz_y8sFUufZrpHFPfi6rafmbT2FglHbx9bNbzDz509JLqENxExU_ugH.R3E01in6Llx2Ox_IYJ4yYw" -H "username: BLAC" -H "imei: 352740070277392" -v -X POST http://23.21.192.188:8080/BlacWSMobileTrack/blacsolws/mobileTrack/getCommand



        eventos.add(new Comando(101,"Ignición Encendida"));
        eventos.add(new Comando(102,"Ignición Apagada"));
        eventos.add(new Comando(103,"Reporte en movimiento"));
        eventos.add(new Comando(104,"Reporte en detenido"));
        eventos.add(new Comando(105,"Reporte por distancia"));
        eventos.add(new Comando(106,"Idle"));
        eventos.add(new Comando(107,"Reporte en movimiento con ignición apagada"));
        eventos.add(new Comando(108,"Exceso de Velocidad"));
        eventos.add(new Comando(109,"Desconexión de Batería Principal"));
        eventos.add(new Comando(110,"Reconexión de Batería Principal"));
        eventos.add(new Comando(111,"Batería de Respaldo Baja"));
        eventos.add(new Comando(112,"Detección de Jammer"));
        eventos.add(new Comando(113,"Equipo Encendido"));
        eventos.add(new Comando(114,"Equipo Apagado"));
        eventos.add(new Comando(115,"Entrando a Geocerca"));
        eventos.add(new Comando(116,"Saliendo de Geocerca"));
        eventos.add(new Comando(117,"Pérdida de señal GPS"));
        eventos.add(new Comando(118,"Puerta abierta"));
        eventos.add(new Comando(119,"Puerta cerrada"));
        eventos.add(new Comando(120,"Consumo inusual de combustible"));
        eventos.add(new Comando(121,"Temperatura alta"));
        eventos.add(new Comando(122,"Temperatura baja"));
        eventos.add(new Comando(123,"Crash incident report"));
        eventos.add(new Comando(124,"Harsh behavior detected"));
        eventos.add(new Comando(125,"Low speed harsh breaking"));
        eventos.add(new Comando(126,"Low speed harsh acceleration"));
        eventos.add(new Comando(127,"Low speed harsh turn behavior"));
        eventos.add(new Comando(128,"Low speed harsh turn and breaking"));
        eventos.add(new Comando(129,"Low speed harsh turn and acceleration"));
        eventos.add(new Comando(130,"Low speed unknown behavior"));
        eventos.add(new Comando(131,"Medium speed harsh breaking"));
        eventos.add(new Comando(132,"Medium speed harsh acceleration"));
        eventos.add(new Comando(133,"Medium speed harsh turn"));
        eventos.add(new Comando(134,"Medium speed harsh turn and breaking"));
        eventos.add(new Comando(135,"Medium speed harsh turn and acceleration"));
        eventos.add(new Comando(136,"Medium speed unknown behavior"));
        eventos.add(new Comando(137,"High speed harsh breaking"));
        eventos.add(new Comando(138,"High speed harsh acceleration"));
        eventos.add(new Comando(139,"High speed harsh turn"));
        eventos.add(new Comando(140,"High speed harsh turn and breaking"));
        eventos.add(new Comando(141,"High speed harsh turn and acceleration"));
        eventos.add(new Comando(142,"High speed unknown behavior"));
        eventos.add(new Comando(143,"Voice monitoring"));
        eventos.add(new Comando(144,"Disconnected from OBD Port"));
        eventos.add(new Comando(145,"Connected from OBD Port"));
        eventos.add(new Comando(146,"SIM insertada"));
        eventos.add(new Comando(147,"SIM retirada"));
        eventos.add(new Comando(148,"GPRS connection established"));
        eventos.add(new Comando(149,"Power OFF alarm"));
        eventos.add(new Comando(150,"Power ON alarm"));
        eventos.add(new Comando(151,"Tow alarm"));
        eventos.add(new Comando(152,"Input 1"));
        eventos.add(new Comando(153,"Input 2"));
        eventos.add(new Comando(154,"Input 3"));
        eventos.add(new Comando(155,"Input 4"));
        eventos.add(new Comando(156,"Input 5"));
        eventos.add(new Comando(157,"Input 6"));
        eventos.add(new Comando(158,"Input 7"));
        eventos.add(new Comando(159,"Input 8"));
        eventos.add(new Comando(160,"Input 9"));
        eventos.add(new Comando(161,"Input 10"));
        eventos.add(new Comando(162,"Engine RPM"));
        eventos.add(new Comando(163,"Engine coolant temperature"));
        eventos.add(new Comando(164,"Fuel consumption"));
        eventos.add(new Comando(165,"Number of DTC"));
        eventos.add(new Comando(166,"Engine load"));
        eventos.add(new Comando(167,"Fuel level input"));

/*
        comandos.add(new Comando(1,"Paro de Motor Activación"));
        comandos.add(new Comando(2,"Paro de Motor Desactivación"));
        comandos.add(new Comando(3,"Cambio de Tiempo de Reporteo"));
        comandos.add(new Comando(4,"Activación de Claxon"));
        comandos.add(new Comando(5,"Desactivación de Claxon"));
        comandos.add(new Comando(6,"Activación de Sirena"));
        comandos.add(new Comando(7,"Desactivación de Sirena"));
        comandos.add(new Comando(8,"Activación de Buzzer"));
        comandos.add(new Comando(9,"Desactivación de Buzzer"));
        comandos.add(new Comando(10,"Cerrado de Chapa"));
        comandos.add(new Comando(11,"Apertura de Chapa"));
        comandos.add(new Comando(12,"Encendido de ThermoKing"));
        comandos.add(new Comando(13,"Apagado de Thermoking"));
        comandos.add(new Comando(14,"Activacion de Direccionales"));
        comandos.add(new Comando(15,"Desactivacion de Direccionales"));
        comandos.add(new Comando(16,"Reset"));
*/
        return eventos;


    }








    public class CmdAsyncTask extends AsyncTask<String,Integer,String> {

        String  token, cuerpo;

        public CmdAsyncTask(String token, String cuerpo) {
            this.token = token;
            this.cuerpo = cuerpo;
        }

        public String obtenerComandos() {
            String resultado = "";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json"),cuerpo);

            final Request request = new Request.Builder()
                    .url(BASE_URL + METODO)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("token", token)
                    .build();


            try {
                Response response = client.newCall(request).execute();
                resultado = response.body().string();


            } catch (Exception ex) {

            }


            return resultado;
        }




        @Override
        protected String doInBackground(String... strings) {
            return obtenerComandos();
        }


        @Override
        protected void onPostExecute(String s) {
            Log.d("ENVIOEVENTOS",s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                int status = jsonObject.getInt("status_code");
                if (status == OK) {
                    String msj = jsonObject.getString("message");
                    Toast.makeText(getApplicationContext(), "Comando enviado", Toast.LENGTH_LONG).show();
                } else {
                    String msj = jsonObject.getString("message");
                    Toast.makeText(getApplicationContext(), "Alerta:" + msj, Toast.LENGTH_LONG).show();
                }


            }catch (Exception ex){
                Log.d("ENVIOEVENTOS",ex.getMessage());
            }

        }
    }










    /*
    * curl -H "Content-Type: application/json" -H "token:eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2Iiwia2lkIjpudWxsLCJjdHkiOiJKV1QifQ..q9Olx0NvXT4sWZ7s4AYT4g.sb-pgqtmtodP6AQfcmYRfY3x7IY1ed4n6bM-R9GRWW0oHalIrmEzF6YJg6vZSz4d7B4IIzbF0V-sGrLmlNjQuZlv6UV56VYzbw_ZZJbnAzyM-pjr_bHQzHRQ38uz09aV6DE3XwMIxpq527WwwZ2cZOUVCZva4DwM5BKPazvZ3FnEqkO90hijPRXsAfAJ73XMek1Re0bc92xb-feInk_SaboR0Sm27GNwpPP_NTD2QyvlLD9xShlNQQ7nCKRDefy-xN41o4KYJVTMuBq03s3AG7z1wrStYDy-ArDGeRObh0mS9eVqTY5L1GaDUHo79uQ3.H474iZr4p0SyQvbXTIm11A" -H "username: BLAC" -H "imei: 352740070277392" -v -X POST http://23.21.192.188:8080/BlacWSMobileTrack/blacsolws/mobileTrack/getCommand
    *
    * */


}
