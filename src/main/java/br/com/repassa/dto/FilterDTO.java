package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5576827449576827110L;

    private int page;
    private int size;
    private String primaryUpdatedDate;
    private String secundaryUpdatedDate;
    private String type;
    @Size(min = 5, message = "Informe pelo menos 5 caracteres para realizar a pesquisa")
    private String code;
    @Size(min = 5, message = "Informe pelo menos 5 caracteres para realizar a pesquisa")
    private String user;
    private String countProducts;
    private String status;
    private String classification;
    private SortingDTO sort;

}
