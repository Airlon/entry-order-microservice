package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SortingDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2152577986772863460L;

    private String sorting;
    private String direction;

}
