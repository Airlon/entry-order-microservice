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
public class EntryOrderRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7127477209234019820L;

    @NotEmpty (message = "Campo type deve ser preenchido")
    private String type;
    @NotEmpty (message = "Campo classification deve ser preenchido")
    private String classification;

}
