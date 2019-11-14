package mx.com.blac.mobile.tracker.modelos;

/**
 * Created by RafaelCastro on 12/6/18.
 */

public class Comando {
    private int idEvento;
    private String eventoNombre;

    public Comando(int idEvento, String eventoNombre) {
        this.idEvento = idEvento;
        this.eventoNombre = eventoNombre;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public String getEventoNombre() {
        return eventoNombre;
    }

    public void setEventoNombre(String eventoNombre) {
        this.eventoNombre = eventoNombre;
    }
}
