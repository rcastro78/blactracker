package mx.com.blac.mobile.tracker.modelos;

/**
 * Created by RafaelCastro on 20/6/18.
 */

public class ComandoRecibido {
    private int idComando;
    private String nomComando,fechaRecibido;

    public ComandoRecibido(int idComando, String nomComando, String fechaRecibido) {
        this.idComando = idComando;
        this.nomComando = nomComando;
        this.fechaRecibido = fechaRecibido;
    }

    public int getIdComando() {
        return idComando;
    }

    public void setIdComando(int idComando) {
        this.idComando = idComando;
    }

    public String getNomComando() {
        return nomComando;
    }

    public void setNomComando(String nomComando) {
        this.nomComando = nomComando;
    }

    public String getFechaRecibido() {
        return fechaRecibido;
    }

    public void setFechaRecibido(String fechaRecibido) {
        this.fechaRecibido = fechaRecibido;
    }
}
