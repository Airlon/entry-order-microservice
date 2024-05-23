package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4723472833277841252L;

    @NotNull (message = "Campo id do produto deve ser preenchido")
    private Long productId;
    private String entryOrderCode;
    private String type;
    private String classification;
}
