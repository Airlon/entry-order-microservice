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
public class SecuritySealDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = -5826946215802790179L;

    private String newSecuritySealCode;
    private String entryOrderCode;

}
