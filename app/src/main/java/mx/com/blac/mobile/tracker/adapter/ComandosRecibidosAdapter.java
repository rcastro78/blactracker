package mx.com.blac.mobile.tracker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mx.com.blac.mobile.tracker.R;
import mx.com.blac.mobile.tracker.modelos.ComandoRecibido;

/**
 * Created by RafaelCastro on 20/6/18.
 */

public class ComandosRecibidosAdapter extends BaseAdapter {

    protected Activity activity;
    protected ArrayList<ComandoRecibido> items;
    ComandoRecibido comando;
    static SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";
    Typeface typeface,typeface1;
    ViewHolder holder=new ViewHolder();
    String TAG="ComandoRecibido";

    public ComandosRecibidosAdapter(Activity activity, ArrayList<ComandoRecibido> items) {
        super();
        this.activity = activity;
        this.items = items;
        sharedpreferences = activity.getSharedPreferences(PREFS_NAME, 0);
        typeface1 = Typeface.createFromAsset(activity.getAssets(),"fonts/PTS55F.ttf");

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        comando = items.get(position);
        ViewHolder holder = null;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_comando_recibido, null);
            holder = new ViewHolder();

            holder.lblNombre = (TextView) convertView.findViewById(R.id.lblItem);
            holder.lblRecibido = (TextView) convertView.findViewById(R.id.lblRecibido);
            holder.lblNombre.setTypeface(typeface1);
            holder.lblRecibido.setTypeface(typeface1);



/*
            LayerDrawable stars = (LayerDrawable) holder.rbCalif.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(Color.parseColor("#F4B400"), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(Color.parseColor("#6F6F6E"), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(Color.parseColor("#6F6F6E"), PorterDuff.Mode.SRC_ATOP);
            */

            convertView.setTag(holder);

        }else {
            holder = (ViewHolder) convertView.getTag();

        }




        holder.lblNombre.setText(comando.getNomComando());
        holder.lblRecibido.setText("Recibido: "+comando.getFechaRecibido());
        //holder.rlContainer.setBackgroundColor(Color.parseColor(menuPrincipal.getColor()));





        /*
        holder.fabInfo.setOnClickListener(this);
        holder.fabCorazon.setOnClickListener(this);
*/


        return convertView;
    }



    static class ViewHolder {
        TextView lblNombre,lblRecibido;

    }




}


