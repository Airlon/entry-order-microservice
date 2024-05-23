package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntryOrderStatusEnum {
    PENDING("PENDING"),
    SENDING("SENDING"),
    SENT("SENT"),
    CLOSED("CLOSED"),
    CANCELED("CANCELED");

    private final String status;
}