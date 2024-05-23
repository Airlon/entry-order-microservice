package br.com.repassa.mapper;

import br.com.repassa.enums.EntryOrderStatusEnum;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EntryOrderStatusEnumMapper {
    public String toPtBr(EntryOrderStatusEnum entryOrderStatusEnum) {
        return switch (entryOrderStatusEnum) {
            case PENDING -> "Pendente";
            case SENDING -> "Enviando";
            case SENT -> "Enviada";
            case CLOSED -> "Fechada";
            case CANCELED -> "Cancelada";
        };
    }
}
