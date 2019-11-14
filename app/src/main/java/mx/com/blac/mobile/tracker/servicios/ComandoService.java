package mx.com.blac.mobile.tracker.servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import mx.com.blac.mobile.tracker.R;
import mx.com.blac.mobile.tracker.receivers.ComandoReceiver;

/**
 * Created by RafaelCastro on 16/6/18.
 */

public class ComandoService extends Service {
    Context context;

    private String BASE_URL;
    SharedPreferences sharedpreferences;
    private static final String PREFS_NAME = "mx.com.blac.mobile.track.pref";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ComandoReceiver cmd = new ComandoReceiver();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cmd.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        BASE_URL = this.getString(R.string.BASE_URL_CONTROL);
        sharedpreferences = this.getSharedPreferences(PREFS_NAME, 0);




    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cmd.cancelAlarm(this);
    }




}
