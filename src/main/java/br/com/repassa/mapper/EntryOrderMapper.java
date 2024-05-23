package br.com.repassa.mapper;

import br.com.repassa.dto.EntryOrderResponseDTO;
import br.com.repassa.entity.EntryOrder;
import br.com.repassa.enums.ClassificationEnum;
import br.com.repassa.enums.EntryOrderStatusEnum;
import br.com.repassa.enums.TypeEnum;
import br.com.repassa.service.EntryOrderService;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class EntryOrderMapper {

    @Inject
    JsonWebToken token;

    @Inject
    EntryOrderService entryOrderService;

    public EntryOrder toEntity(String code, String type, String classification) {
        return EntryOrder.builder()
                .code(code)
                .type(TypeEnum.getByType(type).getType())
                .classification(ClassificationEnum.getByClassification(classification).getClassification())
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .userCreatedId(token.getClaim(Claims.sub))
                .userCreatedEmail(token.getClaim(Claims.email))
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedId(token.getClaim(Claims.sub))
                .userUpdatedEmail(token.getClaim(Claims.email))
                .dateUpdatedAt(LocalDateTime.now())
                .build();
    }

    public EntryOrderResponseDTO toEntryOrderResponseDTO(EntryOrder entryOrder) {
        return EntryOrderResponseDTO.builder()
                .entryOrderCode(entryOrder.getCode())
                .date(entryOrderService.getFormattedDate(entryOrder.getDateUpdatedAt()))
                .userEmail(entryOrder.getUserUpdatedEmail())
                .volume("1/1")
                .entryOrderStatus(entryOrder.getStatus())
                .build();
    }

}
