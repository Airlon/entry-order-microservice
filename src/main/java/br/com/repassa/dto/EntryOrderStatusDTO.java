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
public class EntryOrderStatusDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7717789873309734811L;

    private Long productId;
    private String entryOrderStatus;
}
