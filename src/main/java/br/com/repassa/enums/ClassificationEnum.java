package br.com.repassa.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum ClassificationEnum {

    NORMAL("NORMAL"),
    PRIORITY("PRIORITY");

    private final String classification;

    public static ClassificationEnum getByClassification(String classification) {
        return Arrays.stream(ClassificationEnum.values())
                .filter(c -> c.getClassification().equals(classification))
                .findFirst()
                .orElse(null);
    }
}
