package gym.ada.api.dto.entrega;

import jakarta.validation.constraints.NotNull;

public class UbicacionEntregaDto {

    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}
