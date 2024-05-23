package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PhotographyStatusEnum {
    AWAITING_PHOTOGRAPHY("AWAITING_PHOTOGRAPHY"),
    FINISHED("FINISHED");

    private final String status;
}
