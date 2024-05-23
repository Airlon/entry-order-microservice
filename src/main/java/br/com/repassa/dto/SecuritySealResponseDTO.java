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
public class SecuritySealResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2617891757252115063L;

    private String securitySealOk;
}
