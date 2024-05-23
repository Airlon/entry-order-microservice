package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RenovaProductDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2310750256522238723L;

    private String title;
    private Long productId;
    private int currentStatus;
}
