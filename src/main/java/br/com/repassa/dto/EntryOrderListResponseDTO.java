package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryOrderListResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7017545227257061864L;

    private Integer quantity;
    private List<EntryOrderListDTO> list;
}
