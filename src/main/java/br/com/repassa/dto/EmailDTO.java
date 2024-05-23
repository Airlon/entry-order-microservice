package br.com.repassa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7307574516497776963L;

    private String recipient;
    private String subject;
    private String templateType;
    private String bagId;
    private String username;
    private String stage;
    private Map<String, String> attributesFields;

}
