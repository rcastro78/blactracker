package mx.com.blac.mobile.tracker.utilidades;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by RafaelCastro on 17/4/17.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "blac.db";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // crear la tabla del monitoreo
              String CREATE_MONITOREO_TABLE="CREATE TABLE MONITOREO(username TEXT,imei TEXT, evento int,  longitud DOUBLE,latitud DOUBLE, altitud int, velocidad int, rumbo int, fechaUTC text)";


        db.execSQL(CREATE_MONITOREO_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Borrar tablas anteriores

        db.execSQL("DROP TABLE IF EXISTS MONITOREO");

        // create fresh tables
        this.onCreate(db);
    }

}