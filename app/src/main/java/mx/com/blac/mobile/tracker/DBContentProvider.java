package mx.com.blac.mobile.tracker;

import com.activeandroid.Configuration;
import com.activeandroid.content.ContentProvider;

import mx.com.blac.mobile.tracker.modelosDB.ComandoRecibido;
import mx.com.blac.mobile.tracker.modelosDB.Monitoreo;

/**
 * Created by RafaelCastro on 20/6/18.
 */

public class DBContentProvider extends ContentProvider {
    @Override
    protected Configuration getConfiguration() {
        Configuration.Builder builder = new Configuration.Builder(getContext());
        builder.addModelClass(Monitoreo.class);
        builder.addModelClass(ComandoRecibido.class);
        return builder.create();
    }
}
