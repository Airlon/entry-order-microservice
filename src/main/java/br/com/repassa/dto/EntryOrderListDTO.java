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
public class EntryOrderListDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1656045442157887541L;

    private String registrationdate;
    private String updateddate;
    private String type;
    private String code;
    private String registrationuserid;
    private String registrationuseremail;
    private String updateduserid;
    private String updateduseremail;
    private Integer sent;
    private Integer received;
    private String countproducts;
    private String status;
    private String classification;
}
