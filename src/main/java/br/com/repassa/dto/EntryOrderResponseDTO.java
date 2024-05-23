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
public class EntryOrderResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1656045442157887541L;

    private String entryOrderCode;
    private String date;
    private String userEmail;
    private String volume;
    private String entryOrderStatus;
}
