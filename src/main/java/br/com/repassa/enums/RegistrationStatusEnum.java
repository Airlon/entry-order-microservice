package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegistrationStatusEnum {
    AWAITING_REGISTRATION("AWAITING_REGISTRATION"),
    IN_PROGRESS("IN_PROGRESS"),
    FINISHED("FINISHED");

    private final String status;
}
