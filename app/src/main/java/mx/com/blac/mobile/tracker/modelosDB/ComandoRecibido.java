package mx.com.blac.mobile.tracker.modelosDB;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by RafaelCastro on 20/6/18.
 */
@Table(name="comandosRecibidos")
public class ComandoRecibido extends Model {


    public ComandoRecibido() {
    }

    @Column(name="idComando")
    public int idComando;

    @Column(name="comando")
    public String comando;

    @Column(name="fechaRec")
    public String fechaRec;

}
