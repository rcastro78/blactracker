package mx.com.blac.mobile.tracker.adapter;

/**
 * Created by RafaelCastro on 14/4/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mx.com.blac.mobile.tracker.ComandoRecibidoActivity;
import mx.com.blac.mobile.tracker.MonitoreoActivity;
import mx.com.blac.mobile.tracker.R;

public class NavegacionAdapter extends RecyclerView.Adapter<NavegacionAdapter.ViewHolder>  {


public static  int NO_VALIDO=0;
public static int VALIDO=1;
        int valido;
private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
// IF the view under inflation and population is header or Item
private static final int TYPE_ITEM = 1;

private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java

private String name;        //String Resource for header View Name
private int profile;        //int Resource for header view profile picture
private String email;       //String Resource for header view email
static Context context;
static int ureg;



private static String TAG="NavigationHomeAdapter";


// Creating a ViewHolder which extends the RecyclerView View Holder
// ViewHolder are used to to store the inflated views in order to recycle them

public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    int Holderid;

    TextView textView;
    ImageView imageView;
    ImageView profile;
    TextView Name;
    TextView email;
    Context contxt;
    ArrayList<String> elementos = new ArrayList<>();
//"Monitoreo de vehículos","Control Remoto","Carga Combustible","Mi Posición","Rastreo","Mis contactos de emergencias","Contáctenos","Cerrar Sesión"


    public static int MONITOREO=1;
    public static int CONFIGURACION=2;
    public static int COMANDOS=3;

	/*
	* "Control Remoto",
                "Control de Mantenimientos",
                "Carga Combustible",
                "Asistencia Vial",
                "Proveedor de GPS",
                "Monitoreo de vehículos",
                "Mi Posición",
                "Rastreo",
                "Mis contactos de emergencias",
                "Cerrar Sesión"



             ""Catálogo de Productos",
            "Simulador",
            "Buscador",
            "Recargas Virtuales",
            "Venta de Seguros",
            "Pago de Servicios",
            "Depósitos",
            "Retiros",
            "Transferencias",
            "Desembolsos",
            "Giros",
            "Cerrar Sesión"
	*
	* */





    public ViewHolder(View itemView, int ViewType, Context c) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
        super(itemView);
        contxt = c;
         String PREFS_NAME = "mx.com.blac.mobile.track.pref";
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);



        itemView.setClickable(true);
        itemView.setOnClickListener(this);
        // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created

        if(ViewType == TYPE_ITEM) {
            textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml
            imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
            Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
        }
        else{


            Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
            Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
        }



    }


    @Override
    public void onClick(View v) {


        if (getPosition()==CONFIGURACION)
        {


        }

        if (getPosition()== MONITOREO)
        {

            Intent monitoreo = new Intent(contxt, MonitoreoActivity.class);
            contxt.startActivity(monitoreo);

        }



        if (getPosition()==COMANDOS)
        {
            Intent monitoreo = new Intent(contxt, ComandoRecibidoActivity.class);
            contxt.startActivity(monitoreo);
        }

        /*if (getPosition()==CERRAR)
        {


                contxt.stopService(new Intent(contxt, MonitoreoServicio.class));
                contxt.stopService(new Intent(contxt, NotificacionServicio.class));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("auth",0);
                editor.putString("login","");
                editor.putString("pass","");
                editor.commit();
                //editor.remove(PREFS_NAME).commit();
                if (ureg>0) {
                    new MonitoreoAsyncTask(EVENTO_LOGOUT).execute();
                }
                try{
                    Thread.sleep(1000);
                }catch (Exception ex){

                }

                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra("finish", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

                System.exit(0);


        }*/
    }

		/*
		if (getPosition()==1)
		{
			SharedPreferences sharedpreferences;
			String PREFS_NAME = "ar.com.empleosbuenosaires.pref";
			sharedpreferences = contxt.getSharedPreferences(PREFS_NAME, 0);
			Intent intent = new Intent(contxt,EmpleosActivity.class);
			SharedPreferences.Editor editor = sharedpreferences.edit();
			editor.putInt("flag", 1);
			editor.commit();
			contxt.startActivity(intent);
		}
*/

		/*
		if (getPosition()==CATALOGOS)
		{


			Log.d("MENU PPAL",txtItem.getText().toString());


		}

		if (getPosition()==SIMULADORES)
		{
			Intent intent = new Intent(contxt, SimuladoresActivity.class);
			contxt.startActivity(intent);

		}
		if (getPosition()==BUSCADOR)
		{
			if (valido==VALIDO) {
				//Intent intent = new Intent(contxt, BusquedaActivity.class);
				Intent intent = new Intent(contxt, BuscadorProductosActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==RECARGAS)
		{

			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, RecargasActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==PAGOS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, GruposRecaudacionActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==VENTA_SEGUROS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, VentaSegurosActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==DEPOSITOS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, DepositosActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==RETIROS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, RetiroActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==TRANSFERENCIAS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, TransferenciasActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}

		if (getPosition()==GIROS)
		{
			if (valido==VALIDO) {
				Intent intent = new Intent(contxt, GiroActivity.class);
				contxt.startActivity(intent);
			}else{
				Intent intent = new Intent(contxt, LoginActivity.class);
				contxt.startActivity(intent);
			}

		}
		if (getPosition()==CERRAR_SESION)
		{
			Intent intent = new Intent(contxt, CerrarSesionActivity.class);
			contxt.startActivity(intent);

		}
		*/


    }




    public NavegacionAdapter(String Titles[], int Icons[], String Name, String Email, int Profile, Context passedContext){ // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we
        mNavTitles = Titles;                //have seen earlier
        mIcons = Icons;
        name = Name;
        email = Email;
        profile = Profile;                     //here we assign those passed values to the values we declared here
        //in adapter
        this.context = passedContext;



    }
    public NavegacionAdapter(String Titles[], String Name, String Email, int Profile, Context passedContext){ // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we
        mNavTitles = Titles;                //have seen earlier

        name = Name;
        email = Email;
        profile = Profile;                     //here we assign those passed values to the values we declared here
        //in adapter
        this.context = passedContext;



    }


    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType,context); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_xml,parent,false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v,viewType,context); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
//			holder.imageView.setImageResource(mIcons[position -1]);// Settimg the image with array of our icons
        }
        else{

            // Similarly we set the resources for header view
            holder.Name.setText(name);

        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length+1; // the number of items in the list will be +1 the titles including the header view.
    }


    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }





}