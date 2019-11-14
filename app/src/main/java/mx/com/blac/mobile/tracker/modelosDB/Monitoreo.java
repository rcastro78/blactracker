package mx.com.blac.mobile.tracker.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by RafaelCastro on 17/4/17.
 */
@Table(name="monitoreo", id="id")
public class Monitoreo extends Model {

    /*
    @Column(name = "id")
    public long id;
*/

    public Monitoreo() {
    }

    public Monitoreo(String username, String imei, String evento,
                     double longitud, double latitud, int altitud, int velocidad,
                     int rumbo, String fechaUTC) {
        this.username = username;
        this.imei = imei;
        this.evento = evento;
        this.longitud = longitud;
        this.latitud = latitud;
        this.altitud = altitud;
        this.velocidad = velocidad;
        this.rumbo = rumbo;
        this.fechaUTC = fechaUTC;
    }

    @Column(name="username")
    public String username;

    @Column(name="imei")
    public String imei;

    @Column(name="evento")
    public String evento;

    @Column(name="longitud")
    public double longitud;

    @Column(name="latitud")
    public double latitud;

    @Column(name="altitud")
    public int altitud;

    @Column(name="velocidad")
    public int velocidad;

    @Column(name="rumbo")
    public int rumbo;

    @Column(name="fechaUTC")
    public String fechaUTC;
}
