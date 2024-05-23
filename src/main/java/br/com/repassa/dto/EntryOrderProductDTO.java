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
public class EntryOrderProductDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2180968241839601454L;

    private Long productId;
    private String registeredUserName;
    private String title;
    private String status;
}
