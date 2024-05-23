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
public class FindProductDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3328177508289020424L;

    private Long productId;
    private String title;
    private String registrationStatus;
    private String photographyStatus;
    private String entryOrderStatus;
    private Boolean sentToHJ;

}
