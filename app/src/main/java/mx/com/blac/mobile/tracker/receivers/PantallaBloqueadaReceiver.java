package mx.com.blac.mobile.tracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by RafaelCastro on 22/4/17.
 */

public class PantallaBloqueadaReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {


                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event.getAction()==KeyEvent.KEYCODE_VOLUME_UP){
                    Log.d("BLOQ","Presionado boton en bloqueo");
                }




    }








}
