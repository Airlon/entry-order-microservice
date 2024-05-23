package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStatusDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7461493173517845812L;

    @NotNull(message = "status da ordem de entrada é obrigatório")
    private String entryOrderStatus;
    @NotNull(message = "código da ordem de entrada é obrigatório")
    private String entryOrderCode;
    private List<Long> receivedProducts;
}
