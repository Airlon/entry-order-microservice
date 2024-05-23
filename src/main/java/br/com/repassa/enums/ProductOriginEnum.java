package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductOriginEnum {
    SELLER_CENTER("SELLER_CENTER"),
    RENOVA("RENOVA");

    private final String origin;
}
