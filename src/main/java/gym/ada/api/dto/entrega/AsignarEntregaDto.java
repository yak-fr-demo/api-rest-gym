
package gym.ada.api.dto.entrega;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AsignarEntregaDto {

    @NotBlank(message = "El UID del repartidor es obligatorio")
    @Size(max = 128)
    private String repartidorUid;

    public String getRepartidorUid() {
        return repartidorUid;
    }

    public void setRepartidorUid(String repartidorUid) {
        this.repartidorUid = repartidorUid;
    }
}
