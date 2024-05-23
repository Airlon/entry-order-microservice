package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecuritySealUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -625355544259882739L;

    @NotEmpty(message = "Campo de código da ordem de entrada deve ser preenchido")
    private String entryOrderCode;
    @NotEmpty(message = "Campo da novo código do lacre deve ser preenchido")
    private String newSecuritySealCode;
}
