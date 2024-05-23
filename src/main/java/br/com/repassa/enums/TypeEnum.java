package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum TypeEnum {

    NEW_PRODUCT ("NEW_PRODUCT", "E"),
    REVERSE ("REVERSE", "R");

    private final String type;
    private final String entryOrderCodeLetter;

    public static TypeEnum getByType(String type) {
        return Arrays.stream(TypeEnum.values())
                .filter(t -> t.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    public static String getLetterByType(String type) {
        return Arrays.stream(TypeEnum.values())
                .filter(t -> t.getType().equals(type))
                .map(TypeEnum::getEntryOrderCodeLetter)
                .findFirst()
                .orElse(null);
    }
}
