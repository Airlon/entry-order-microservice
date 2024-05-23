package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortingEnum {

    DATE_CREATE ("CREATE_DATE"),
    DATE_UPDATE("UPDATE_DATE"),
    ASC ("A"),
    DESC ("D");

    private final String sorting;
}
