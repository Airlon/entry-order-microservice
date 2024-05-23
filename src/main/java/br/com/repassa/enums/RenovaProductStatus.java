package br.com.repassa.enums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RenovaProductStatus {
    NEW(0),
    CART(1),
    ACTIVE(2),
    PENDING(3),
    CANCELED(4),
    RETURNED(5),
    IN_PAYMENT(6),
    IN_TRANSIT(7),
    IN_RETURN(8),
    IN_ORDER(9),
    IN_PAYOUT(10),
    DONATED(11),
    SHORTAGE(12);

    private final int value;
}
