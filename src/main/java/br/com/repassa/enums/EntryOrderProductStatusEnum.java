package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EntryOrderProductStatusEnum {
    IN_PROGRESS("IN_PROGRESS"),
    SENDING("SENDING"),
    SENT("SENT"),
    RECEIVED("RECEIVED"),
    NOT_RECEIVED("NOT_RECEIVED"),
    REMOVED("REMOVED");

    private final String status;
}
