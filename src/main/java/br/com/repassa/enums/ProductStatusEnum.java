package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatusEnum {
    AWAITING("AWAITING"),
    SENT("SENT");

    private final String status;
}
