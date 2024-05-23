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
public class EntryOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6947916829995897235L;

    private String registrationDate;
    private String entryOrderCode;
    private String entryOrderStatus;
    private String entryOrderClassification;
    private String userUpdatedEmail;
    private String volume;
    private String securitySealCode;
    private Integer countEntryOrderProduct;
    private List<EntryOrderProductDTO> entryOrderProductList;

}
