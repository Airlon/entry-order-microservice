package br.com.repassa.service;


import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.EmailDTO;
import br.com.repassa.dto.EntryOrderDTO;
import br.com.repassa.dto.EntryOrderListDTO;
import br.com.repassa.dto.EntryOrderListResponseDTO;
import br.com.repassa.dto.EntryOrderProductDTO;
import br.com.repassa.dto.EntryOrderStatusDTO;
import br.com.repassa.dto.FilterDTO;
import br.com.repassa.dto.FindProductDTO;
import br.com.repassa.dto.RenovaProductDTO;
import br.com.repassa.dto.RegisterRequestDTO;
import br.com.repassa.dto.RegisterResponseDTO;
import br.com.repassa.dto.SecuritySealDTO;
import br.com.repassa.dto.SecuritySealUpdateDTO;
import br.com.repassa.dto.UpdateStatusDTO;
import br.com.repassa.entity.EntryOrder;
import br.com.repassa.entity.EntryOrderLog;
import br.com.repassa.entity.EntryOrderProduct;
import br.com.repassa.entity.EntryOrderProductLog;
import br.com.repassa.entity.SecuritySeal;
import br.com.repassa.entity.SecuritySealLog;
import br.com.repassa.enums.ClassificationEnum;
import br.com.repassa.enums.EntryOrderProductStatusEnum;
import br.com.repassa.enums.EntryOrderStatusEnum;
import br.com.repassa.enums.PhotographyStatusEnum;
import br.com.repassa.enums.ProductOriginEnum;
import br.com.repassa.enums.ProductStatusEnum;
import br.com.repassa.enums.RegistrationStatusEnum;
import br.com.repassa.enums.RenovaProductStatus;
import br.com.repassa.enums.TypeEnum;
import br.com.repassa.exception.EntryOrderError;
import br.com.repassa.mapper.EntryOrderMapper;
import br.com.repassa.mapper.EntryOrderStatusEnumMapper;
import br.com.repassa.repository.EntryOrderLogRepository;
import br.com.repassa.repository.EntryOrderProductLogRepository;
import br.com.repassa.repository.EntryOrderProductRepository;
import br.com.repassa.repository.EntryOrderRepository;
import br.com.repassa.repository.SecuritySealLogRepository;
import br.com.repassa.repository.SecuritySealRepository;
import br.com.repassa.resource.client.EmailRestClient;
import br.com.repassa.resource.client.ProductIntegrationRestClient;
import br.com.repassa.resource.client.ProductRestClient;
import br.com.repassa.sellercenter.lib.message.runtime.MessageQueueSender;
import br.com.repassa.sellercenter.lib.message.runtime.dto.HighjumpEntryOrderDTO;
import br.com.repassa.sellercenter.lib.message.runtime.dto.HighjumpEntryOrderItemDTO;
import br.com.repassa.sellercenter.lib.message.runtime.enums.TransactionCodeEnum;
import io.quarkus.runtime.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@ApplicationScoped
@Slf4j
public class EntryOrderService {

    @Inject
    EntryOrderRepository entryOrderRepository;

    @Inject
    EntryOrderMapper entryOrderMapper;

    @RestClient
    ProductRestClient productRestClient;

    @RestClient
    ProductIntegrationRestClient productIntegrationRestClient;

    @RestClient
    EmailRestClient emailRestClient;

    @Inject
    EntryOrderProductRepository entryOrderProductRepository;

    @Inject
    SecuritySealRepository securitySealRepository;

    @Inject
    SecuritySealLogRepository securitySealLogRepository;

    @Inject
    EntryOrderLogRepository entryOrderLogRepository;

    @Inject
    EntryOrderProductLogRepository entryOrderProductLogRepository;

    @Inject
    EntryOrderStatusEnumMapper entryOrderStatusEnumMapper;

    @Inject
    MessageQueueSender messageQueueSender;

    @Context
    HttpHeaders headers;

    @Inject
    JsonWebToken token;

    @ConfigProperty(name = "email.not-stored.recipient")
    String recipient;


    public EntryOrderDTO getEntryOrderWithProducts(String entryOrderCode) throws RepassaException {
        EntryOrder entryOrder = entryOrderRepository.findByCodeOptional(entryOrderCode)
                .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));

        String securitySealCode = securitySealRepository.findByEntryOrderOptional(entryOrder)
                .map(SecuritySeal::getSecuritySealCode)
                .orElse(null);

        List<EntryOrderProductDTO> entryOrderProductList = entryOrderProductRepository.findByEntryOrder(entryOrder)
                .stream()
                .sorted(Comparator.comparing(EntryOrderProduct::getRegistrationDate).reversed())
                .map(e ->
                        EntryOrderProductDTO.builder()
                                .registeredUserName(e.getRegisteredUserName())
                                .title(e.getTitle())
                                .productId(e.getProductId())
                                .status(e.getEntryOrderProductStatus())
                                .build()
                )
                .toList();

        return EntryOrderDTO.builder()
                .registrationDate(getFormattedDate(entryOrder.getDateCreatedAt()))
                .entryOrderCode(entryOrder.getCode())
                .volume("1/1")
                .securitySealCode(securitySealCode)
                .userUpdatedEmail(entryOrder.getUserUpdatedEmail())
                .entryOrderStatus(entryOrder.getStatus())
                .entryOrderClassification(entryOrder.getClassification())
                .entryOrderProductList(entryOrderProductList)
                .countEntryOrderProduct(entryOrderProductList.size())
                .build();
    }

    @Transactional(rollbackOn = Exception.class)
    public RegisterResponseDTO registerProductOnEntryOrder(RegisterRequestDTO registerRequestDTO) throws RepassaException {
        String userId = token.getClaim(Claims.sub);
        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        String title;
        Long productId;
        EntryOrder entryOrder;
        ProductOriginEnum productOriginEnum;

        if (Objects.isNull(registerRequestDTO.getEntryOrderCode())) {
            entryOrder = createEntryOrder(registerRequestDTO);
            registerRequestDTO.setEntryOrderCode(entryOrder.getCode());
        } else {
            entryOrder = entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode())
                    .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));
        }

        if (!EntryOrderStatusEnum.PENDING.getStatus().equals(entryOrder.getStatus())) {
            throw new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ABERTA);
        }

        if (TypeEnum.NEW_PRODUCT.getType().equals(entryOrder.getType())) {
            log.info("Registrando produto em uma ordem de entrada");
            FindProductDTO findProductDTO = productRestClient
                    .findProductById(registerRequestDTO.getProductId(), tokenAuth);

            if (isNull(findProductDTO)) {
                log.info("Produto {} nao encontrado", registerRequestDTO.getProductId());
                throw new RepassaException(EntryOrderError.ID_PRODUTO_NAO_ENCONTRADO);
            }

            validateRegistrationNewProduct(registerRequestDTO, findProductDTO);
            productId = findProductDTO.getProductId();
            title = findProductDTO.getTitle();
            productOriginEnum = ProductOriginEnum.SELLER_CENTER;
        } else if (TypeEnum.REVERSE.getType().equals(entryOrder.getType())) {
            log.info("Registrando produto em uma ordem de entrada reversa");
            RenovaProductDTO renovaProductDTO = productIntegrationRestClient
                    .findProductById(registerRequestDTO.getProductId(), tokenAuth);

            if (isNull(renovaProductDTO)) {
                log.info("Produto {} nao encontrado no Renova", registerRequestDTO.getProductId());
                throw new RepassaException(EntryOrderError.ID_PRODUTO_NAO_ENCONTRADO);
            }

            validateRegistrationReverse(registerRequestDTO, renovaProductDTO);
            productId = renovaProductDTO.getProductId();
            title = renovaProductDTO.getTitle();
            productOriginEnum = ProductOriginEnum.RENOVA;
        } else {
            log.error("Registro desse produto para esse tipo de ordem de entrada {} ainda nao foi implementado",
                    registerRequestDTO.getType());
            throw new RepassaException(EntryOrderError.TIPO_ORDEM_ENTRADA_NAO_IMPLEMENTADO);
        }

        EntryOrderProduct entryOrderProduct = EntryOrderProduct.builder()
                .entryOrder(entryOrder)
                .productId(productId)
                .title(title)
                .registeredUserName(token.getClaim("name"))
                .registeredUserEmail(token.getClaim(Claims.email))
                .entryOrderProductStatus(EntryOrderProductStatusEnum.IN_PROGRESS.getStatus())
                .registeredUserId(userId)
                .registrationDate(LocalDateTime.now())
                .productOrigin(productOriginEnum.getOrigin())
                .build();

        entryOrder.setUserUpdatedId(userId);
        entryOrder.setUserUpdatedEmail(token.getClaim(Claims.email));
        entryOrder.setDateUpdatedAt(LocalDateTime.now());

        try {
            entryOrderProductRepository.persist(entryOrderProduct);
            entryOrderRepository.persist(entryOrder);

            updateEntryOrderProductLog(entryOrderProduct);

            return RegisterResponseDTO.builder()
                    .entryOrderCode(entryOrder.getCode())
                    .build();
        } catch (PersistenceException e) {
            log.error("Erro ao salvar registro de produto na ordem de entrada", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
        } catch (Exception e) {
            log.error("Ocorreu um erro ao registrar o produto", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_REGISTRAR_PRODUTO);
        }
    }

    private EntryOrder createEntryOrder(RegisterRequestDTO registerRequestDTO) throws RepassaException {

        validateEntryOrderValues(registerRequestDTO);
        String code = generateEntryOrderCode(registerRequestDTO.getType());
        if (entryOrderRepository.countByCode(code) > 0) {
            log.info("O codigo da ordem de entrada ja existe.");
            throw new RepassaException(EntryOrderError.CODIGO_ORDEM_DE_ENTRADA_JA_EXISTE);
        }

        log.info("Montando entidade da ordem de entrada");
        EntryOrder entryOrder = entryOrderMapper.toEntity(code, registerRequestDTO.getType(), registerRequestDTO.getClassification());

        try {
            log.info("Realizando persistencia da ordem de entrada");
            entryOrderRepository.persist(entryOrder);
        } catch (PersistenceException e) {
            log.error(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO.getErrorMessage());
            throw new RepassaException(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
        }

        updateEntryOrderLog(entryOrder.getCode(), EntryOrderStatusEnum.PENDING);

        return entryOrder;
    }

    private void validateRegistrationNewProduct(RegisterRequestDTO registerRequestDTO, FindProductDTO product)
            throws RepassaException {
        log.info("Iniciando validacao de registro do produto " + registerRequestDTO.getProductId());

        validateEntryOrder(registerRequestDTO);

        EntryOrderProduct productAssociated =
                entryOrderProductRepository.
                        findProductByProductIdAndStatusExcludingNotReceived(registerRequestDTO.getProductId()).orElse(null);

        if (Objects.nonNull(productAssociated) &&
                !Objects.equals(productAssociated.getEntryOrderProductStatus(), EntryOrderProductStatusEnum.IN_PROGRESS.name())) {
            log.error("Essa ordem de entrada so permite o registro de produtos novos.");
            throw new RepassaException(EntryOrderError.PRODUTO_NAO_E_NOVO);
        }

        if (!RegistrationStatusEnum.FINISHED.getStatus().equals(product.getRegistrationStatus())) {
            throw new RepassaException(EntryOrderError.PRODUTO_NAO_FINALIZADO);
        }

        if (!PhotographyStatusEnum.FINISHED.getStatus().equals(product.getPhotographyStatus())) {
            throw new RepassaException(EntryOrderError.FOTOGRAFIA_NAO_FINALIZADA);
        }

        if (Boolean.FALSE.equals(product.getSentToHJ())) {
            throw new RepassaException(EntryOrderError.PRODUTO_NAO_ENVIADO_AO_HJ);
        }
    }

    private void validateRegistrationReverse(RegisterRequestDTO registerRequestDTO, RenovaProductDTO product)
            throws RepassaException {
        validateEntryOrder(registerRequestDTO);

        RenovaProductStatus status = RenovaProductStatus.values()[product.getCurrentStatus()];

        switch (status) {
            case NEW:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_NOVO));
            case CART:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_CARRINHO));
            case ACTIVE:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_ATIVO));
            case PENDING:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_PENDENTE));
            case IN_PAYMENT:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_AGUARDANDO_PAGAMENTO));
            case IN_TRANSIT:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_VENDIDO_ENTREGA));
            case IN_RETURN:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_VENDIDO_DEVOLUCAO));
            case IN_ORDER:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_VENDIDO_FINALIZACAO));
            case DONATED:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_DOADO));
            case IN_PAYOUT:
                log.error("Produto " + product.getProductId() + " com status inválido: " + product.getCurrentStatus());
                throw new RepassaException((EntryOrderError.PRODUTO_RENOVA_VENDIDO_FINALIZADO));
        }

    }

    private void validateEntryOrder(RegisterRequestDTO registerRequestDTO) throws RepassaException {
        EntryOrderProduct entryOrderProduct = entryOrderProductRepository
                .findByIdProductAndStatus(registerRequestDTO.getProductId(), EntryOrderStatusEnum.PENDING.name())
                .orElse(null);

        if (Objects.nonNull(entryOrderProduct) && Objects.nonNull(registerRequestDTO.getEntryOrderCode()) &&
                (registerRequestDTO.getEntryOrderCode().equals(entryOrderProduct.getEntryOrder().getCode()))) {
            throw new RepassaException(EntryOrderError.ID_PRODUTO_ORDEM_DE_ENTRADA_DUPLICADO);
        }

        if (Objects.nonNull(entryOrderProduct)) {
            throw new RepassaException(EntryOrderError.PRODUTO_JA_EXISTENTE_EM_ORDEM_DE_ENTRADA);
        }
    }

    private void validateEntryOrderValues(RegisterRequestDTO registerRequestDTO) throws RepassaException {
        log.info("Iniciando a validacao dos valores da ordem de entrada");
        validateType(registerRequestDTO.getType());
        validateClassification(registerRequestDTO.getClassification());
    }

    private void validateType(String type) throws RepassaException {
        log.info("Iniciando a validacao do tipo");
        if (isNull(TypeEnum.getByType(type))) {
            throw new RepassaException(EntryOrderError.TIPO_INVALIDO);
        }
    }

    private void validateClassification(String classification) throws RepassaException {
        log.info("Iniciando a validacao da classificacao");
        if (isNull(ClassificationEnum.getByClassification(classification))) {
            throw new RepassaException(EntryOrderError.CLASSIFICACAO_INVALIDA);
        }
    }

    private void validateDateCreated(String date) throws RepassaException {
        if (!StringUtil.isNullOrEmpty(date)) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                LocalDateTime.parse(date, formatter);
            } catch (Exception e) {
                throw new RepassaException(EntryOrderError.DATA_INVALIDA);
            }
        }
    }

    private void validateDatesCreated(String datePrimary, String dateSecundary) throws RepassaException {
        validateDateCreated(datePrimary);
        validateDateCreated(dateSecundary);

        if (!StringUtil.isNullOrEmpty(datePrimary) && !StringUtil.isNullOrEmpty(dateSecundary)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            LocalDateTime localDatePrimary = LocalDateTime.parse(datePrimary, formatter);
            LocalDateTime localDateSecundary = LocalDateTime.parse(dateSecundary, formatter);

            if (localDatePrimary.isAfter(localDateSecundary)) {
                throw new RepassaException(EntryOrderError.ERRO_INTERVALO_DATAS);
            }
        }
    }

    private void validateCountProducts(String countProducts) throws RepassaException {
        if (nonNull(countProducts)) {
            try {
                int i = Integer.parseInt(countProducts);
                if (i < 0)
                    throw new RepassaException(EntryOrderError.QUANTIDADE_INVALIDA);
            } catch (Exception e) {
                throw new RepassaException(EntryOrderError.QUANTIDADE_INVALIDA);
            }
        }
    }

    private String generateEntryOrderCode(String entryOrderType) {
        log.info("Iniciando a geracao do codigo da ordem de entrada");
        String codeLetter = TypeEnum.getLetterByType(entryOrderType);

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

        return codeLetter + now.format(formatter);
    }

    public String getFormattedDate(LocalDateTime date) {
        log.info("Formatando o LocalDateTime para formato String dd/MM/yyyy");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return date.format(formatter);
    }

    @Transactional
    public void deleteProductsEntryOrder(List<Long> productIds) throws RepassaException {
        log.info("Iniciando metodo de remover o produto da ordem de entrada");
        for (Long productId : productIds) {
            Optional<EntryOrderProduct> productOptional = entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.getStatus());

            if (productOptional.isEmpty()) {
                throw new RepassaException(EntryOrderError.ITEM_NAO_ENCONTRADO);
            }
            if (!EntryOrderProductStatusEnum.IN_PROGRESS.getStatus().equals(productOptional.get().getEntryOrderProductStatus())) {
                throw new RepassaException(EntryOrderError.ERRO_REMOVER_PRODUTO_STATUS_DIFERENTE_DE_EM_PROGRESSO);
            }

            try {
                entryOrderProductRepository.delete(productOptional.get());
                updateEntryOrderProductLogRemoved(productOptional.get());
            } catch (PersistenceException e) {
                log.error("Erro ao conectar no banco de dados: ", e);
                throw new RepassaException(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
            } catch (Exception e) {
                log.error("Ocorreu um erro ao remover produtos da ordem de entrada.", e);
                throw new RepassaException(EntryOrderError.ERRO_REMOVER_PRODUTO_OE);
            }
        }
    }

    public EntryOrderListResponseDTO getOrdersList(FilterDTO filterDTO) throws RepassaException {
        validateCountProducts(filterDTO.getCountProducts());
        validateDatesCreated(filterDTO.getPrimaryUpdatedDate(), filterDTO.getSecundaryUpdatedDate());

        List<EntryOrderListDTO> listReturn = entryOrderRepository.listEntryOrders(filterDTO).stream().peek(dto -> dto.setCountproducts(dto.getSent() + "/" + dto.getReceived())).toList();
        Integer quantity = entryOrderRepository.listEntryOrdersQuantity(filterDTO);

        return new EntryOrderListResponseDTO(quantity, listReturn);
    }

    @Transactional
    public void createSecuritySeal(SecuritySealDTO securitySealDTO) throws RepassaException {
        EntryOrder entryOrder = entryOrderRepository.findByCodeOptional(securitySealDTO.getEntryOrderCode())
                .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));

        validateSecuritySeal(securitySealDTO.getNewSecuritySealCode());

        Optional<SecuritySeal> entryOrderOptional = securitySealRepository.findByEntryOrderOptional(entryOrder);

        if (entryOrderOptional.isPresent()) {
            throw new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_JA_POSSUI_LACRE);
        }

        securitySealRepository.persist(SecuritySeal.builder()
                .securitySealCode(securitySealDTO.getNewSecuritySealCode())
                .entryOrder(entryOrder)
                .userCreatedId(token.getClaim(Claims.sub))
                .userCreatedEmail(token.getClaim(Claims.email))
                .dateCreatedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void updateSecuritySeal(SecuritySealUpdateDTO securitySealUpdateDTO) throws RepassaException {
        EntryOrder entryOrder = entryOrderRepository.findByCodeOptional(securitySealUpdateDTO.getEntryOrderCode())
                .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));

        SecuritySeal securitySeal = securitySealRepository
                .findByEntryOrderOptional(entryOrder)
                .orElseThrow(() -> new RepassaException(EntryOrderError.LACRE_NAO_ECONTRADO));

        if (securitySeal.getSecuritySealCode().equals(securitySealUpdateDTO.getNewSecuritySealCode())) {
            throw new RepassaException(EntryOrderError.ALTERAR_MESMO_CODIGO_LACRE);
        }

        validateSecuritySeal(securitySealUpdateDTO.getNewSecuritySealCode());

        LocalDateTime lastDateAt = isNull(securitySeal.getDateUpdatedAt()) ?
                securitySeal.getDateCreatedAt() : securitySeal.getDateUpdatedAt();

        String lastUserEmail = isNull(securitySeal.getUserUpdatedEmail()) ?
                securitySeal.getUserCreatedEmail() : securitySeal.getUserUpdatedEmail();

        String lastUserId = isNull(securitySeal.getUserUpdatedId()) ?
                securitySeal.getUserCreatedId() : securitySeal.getUserUpdatedId();

        SecuritySealLog securitySealLog = SecuritySealLog.builder()
                .securitySeal(securitySeal)
                .securitySealCode(securitySeal.getSecuritySealCode())
                .entryOrder(securitySeal.getEntryOrder())
                .lastDateAt(lastDateAt)
                .lastUserEmail(lastUserEmail)
                .lastUserId(lastUserId)
                .build();

        securitySealLogRepository.persist(securitySealLog);

        securitySeal.setSecuritySealCode(securitySealUpdateDTO.getNewSecuritySealCode());
        securitySeal.setDateUpdatedAt(LocalDateTime.now());
        securitySeal.setUserUpdatedEmail(token.getClaim(Claims.email));
        securitySeal.setUserUpdatedId(token.getClaim(Claims.sub));

        securitySealRepository.persist(securitySeal);
    }

    @Transactional
    public void finishEntryOrder(String entryOrderCode) throws RepassaException {
        try {
            EntryOrder entryOrder = entryOrderRepository.findByCodeOptional(entryOrderCode)
                    .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));

            if (!EntryOrderStatusEnum.PENDING.getStatus().equals(entryOrder.getStatus())) {
                throw new RepassaException(EntryOrderError.FINALIZACAO_NAO_PERMITIDA_OE_NAO_ESTA_ABERTA);
            }

            List<EntryOrderProduct> entryOrderProductList = entryOrderProductRepository.findByEntryOrder(entryOrder);
            if (entryOrderProductList.isEmpty()) {
                throw new RepassaException(EntryOrderError.FINALIZACAO_NAO_PERMITIDA_OE_NAO_TEM_ITENS);
            }

            entryOrder.setStatus(EntryOrderStatusEnum.SENDING.getStatus());
            entryOrder.setUserUpdatedId(token.getClaim(Claims.sub));
            entryOrder.setUserUpdatedEmail(token.getClaim(Claims.email));
            entryOrder.setDateUpdatedAt(LocalDateTime.now());
            entryOrderRepository.persist(entryOrder);
            updateEntryOrderLog(entryOrder.getCode(), EntryOrderStatusEnum.SENDING);

            entryOrderProductList.forEach(entryOrderProduct -> {
                entryOrderProduct.setEntryOrderProductStatus(EntryOrderProductStatusEnum.SENDING.getStatus());
                entryOrderProductRepository.persist(entryOrderProduct);
                updateEntryOrderProductLog(entryOrderProduct);
            });

            sendMessageToHighjumpQueue(entryOrder, entryOrderProductList, TransactionCodeEnum.NEW);

        } catch (RepassaException e) {
            log.error("Ocorreu um erro: ", e);
            throw e;
        } catch (PersistenceException e) {
            log.error("Erro ao conectar no banco de dados: ", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
        } catch (Exception e) {
            log.error("Ocorreu um erro ao finalizar a ordem de entrada.", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_FINALIZAR_OE);
        }
    }

    private void validateSecuritySeal(String securitySealCode) throws RepassaException {
        boolean isSecuritySealCodeRegistered = securitySealRepository.findBySecuritySealCodeOptional(securitySealCode)
                .isPresent();

        if (isSecuritySealCodeRegistered) {
            throw new RepassaException(EntryOrderError.CODIGO_LACRE_JA_EXISTE);
        }

        if (!StringUtils.isAlphanumeric(securitySealCode)) {
            throw new RepassaException(EntryOrderError.CODIGO_LACRE_INVALIDO);
        }
    }

    private void updateEntryOrderLog(String entryOrderCode, EntryOrderStatusEnum entryOrderStatusEnum) {
        EntryOrderLog entryOrderLog = EntryOrderLog.builder()
                .entryOrderCode(entryOrderCode)
                .status(entryOrderStatusEnum.getStatus())
                .userStatusUpdateId(token.getClaim(Claims.sub))
                .userStatusUpdateEmail(token.getClaim(Claims.email))
                .dateStatusUpdate(LocalDateTime.now())
                .build();

        entryOrderLogRepository.persist(entryOrderLog);
    }

    private void updateEntryOrderProductLog(EntryOrderProduct entryOrderProduct) {
        EntryOrder entryOrder = entryOrderProduct.getEntryOrder();
        entryOrderProductRepository.findByEntryOrderAndProductId(entryOrder, entryOrderProduct.getProductId())
                .ifPresent((e) -> {
                    EntryOrderProductLog entryOrderProductLog = EntryOrderProductLog.builder()
                            .entryOrderCode(entryOrderProduct.getEntryOrder().getCode())
                            .productId(entryOrderProduct.getProductId())
                            .entryOrderProductStatus(entryOrderProduct.getEntryOrderProductStatus())
                            .userStatusUpdateId(token.getClaim(Claims.sub))
                            .userStatusUpdateEmail(token.getClaim(Claims.email))
                            .dateStatusUpdate(LocalDateTime.now())
                            .build();

                    entryOrderProductLogRepository.persist(entryOrderProductLog);
                });
    }

    private void updateEntryOrderProductLogRemoved(EntryOrderProduct entryOrderProduct) {
        EntryOrderProductLog entryOrderProductLog = EntryOrderProductLog.builder()
                .productId(entryOrderProduct.getProductId())
                .entryOrderCode(entryOrderProduct.getEntryOrder().getCode())
                .entryOrderProductStatus(EntryOrderProductStatusEnum.REMOVED.getStatus())
                .dateStatusUpdate(LocalDateTime.now())
                .userStatusUpdateId(token.getClaim(Claims.sub))
                .userStatusUpdateEmail(token.getClaim(Claims.email))
                .build();

        entryOrderProductLogRepository.persist(entryOrderProductLog);
    }

    @Transactional
    public void updateEntryOrderStatus(UpdateStatusDTO updateStatusDTO) throws RepassaException {
        try {
            String tokenAuth = headers.getHeaderString(AUTHORIZATION);

            EntryOrderStatusEnum entryOrderStatusEnum = EntryOrderStatusEnum
                    .valueOf(updateStatusDTO.getEntryOrderStatus());

            EntryOrder entryOrder = entryOrderRepository.findByCodeOptional(updateStatusDTO.getEntryOrderCode())
                    .orElseThrow(() -> new RepassaException(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA));

            log.info("Alterando status da ordem de entrada para: " + updateStatusDTO.getEntryOrderStatus());

            entryOrder.setStatus(entryOrderStatusEnum.getStatus());

            entryOrderRepository.persist(entryOrder);

            updateEntryOrderLog(entryOrder.getCode(), entryOrderStatusEnum);

            if (EntryOrderStatusEnum.SENT.equals(entryOrderStatusEnum)) {
                log.info("Alterando produtos da ordem de entrada para enviado");

                entryOrderProductRepository.findByEntryOrder(entryOrder)
                        .forEach(e -> {
                            e.setEntryOrderProductStatus(EntryOrderProductStatusEnum.SENT.getStatus());
                            entryOrderProductRepository.persist(e);
                            updateEntryOrderProductLog(e);
                            if (TypeEnum.NEW_PRODUCT.getType().equals(entryOrder.getType())) {
                                productRestClient.updateProductOrderStatus(
                                        new EntryOrderStatusDTO(e.getProductId(), ProductStatusEnum.SENT.getStatus()), tokenAuth);
                            }
                        });
            }

            if (EntryOrderStatusEnum.CLOSED.equals(entryOrderStatusEnum)) {
                log.info("Alterando produtos da ordem de entrada para recebido ou nao recebido");

                validateIfAllProductsAreSent(entryOrder);
                updateStatusDTO.getReceivedProducts().stream()
                        .map(p -> validateIfProductIsPresent(entryOrder, p))
                        .filter(Objects::nonNull)
                        .forEach(e -> {
                            e.setEntryOrderProductStatus(EntryOrderProductStatusEnum.RECEIVED.getStatus());
                            entryOrderProductRepository.persist(e);
                            updateEntryOrderProductLog(e);
                        });

                List<EntryOrderProduct> entryOrderProductList = entryOrderProductRepository
                        .findByEntryOrderAndStatus(entryOrder, EntryOrderProductStatusEnum.SENT);

                entryOrderProductList.forEach(e -> {
                    e.setEntryOrderProductStatus(EntryOrderProductStatusEnum.NOT_RECEIVED.getStatus());
                    entryOrderProductRepository.persist(e);
                    updateEntryOrderProductLog(e);
                });

                sendNotStoredItemsToEmail(entryOrder, entryOrderProductList);
            }
        } catch (RepassaException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Erro status da ordem de entrada invalido", e);
            throw new RepassaException(EntryOrderError.STATUS_ORDEM_ENTRADA_INVALIDO);
        } catch (PersistenceException e) {
            log.error("Erro ao conectar no banco de dados: ", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
        } catch (Exception e) {
            log.error("Ocorreu um erro ao atualizar status da ordem de entrada", e);
            throw new RepassaException(EntryOrderError.ERRO_AO_ATUALIZAR_STATUS_OE);
        }
    }

    private void validateIfAllProductsAreSent(EntryOrder entryOrder) throws RepassaException {
        boolean hasEntryOrderProductNotSent = entryOrderProductRepository
                .findByEntryOrder(entryOrder).stream()
                .anyMatch(e -> !EntryOrderProductStatusEnum.SENT.getStatus().equals(e.getEntryOrderProductStatus()));

        if (hasEntryOrderProductNotSent) {
            log.error("Falha atualizar status, existe produto nessa ordem de entrada ainda nao enviado ao highjump");
            throw new RepassaException(EntryOrderError.PRODUTO_NAO_ENVIADO_AO_HJ);
        }
    }

    private EntryOrderProduct validateIfProductIsPresent(EntryOrder entryOrder, Long productId) {
        return entryOrderProductRepository.findByEntryOrderAndProductId(entryOrder, productId)
                .orElseGet(() -> {
                    log.error("Produto da ordem de entrada não encontrado para o id: " + productId);

                    return null;
                });
    }

    private void sendMessageToHighjumpQueue(EntryOrder entryOrder, List<EntryOrderProduct> entryOrderProductList, TransactionCodeEnum transactionCode) {
        AtomicInteger sequencial = new AtomicInteger(1);

        log.info("Montando lista de produtos da OE para envio");
        List<HighjumpEntryOrderItemDTO> highjumpEntryOrderItemDTOList = entryOrderProductList.stream()
                .map(entryOrderProduct -> HighjumpEntryOrderItemDTO.builder()
                        .lineNumber(sequencial.getAndIncrement())
                        .transactionCode(transactionCode)
                        .itemNumber(entryOrderProduct.getProductId().toString())
                        .build())
                .toList();

        log.info("Montando objeto para envio à fila do Highjump");
        HighjumpEntryOrderDTO highjumpEntryOrderDTO = HighjumpEntryOrderDTO.builder()
                .inboundOrderNumber(entryOrder.getCode())
                .hostControlNumber(entryOrder.getId().toString())
                .transactionCode(transactionCode)
                .highjumpEntryOrderItemDTOList(highjumpEntryOrderItemDTOList)
                .build();

        log.info("Iniciando o envio à fila do Highjump");
        messageQueueSender.sendEntryOrderToHighjump(highjumpEntryOrderDTO);
        log.info("Produto foi enviado à fila do Highjump");
    }

    private void sendNotStoredItemsToEmail(EntryOrder entryOrder, List<EntryOrderProduct> entryOrderProductList) {
        if (entryOrderProductList.isEmpty()) {
            log.info("Ordem de entrada com todos os itens armazenados com sucesso");
            return;
        }

        log.info("Enviando email com os itens nao armazenados");

        EmailDTO emailDTO = EmailDTO.builder()
                .subject("Produtos Não Recebidos: " + entryOrder.getCode())
                .stage("Armazenamento")
                .recipient(recipient)
                .templateType("NOT_STORED")
                .attributesFields(fillAttributesFields(entryOrder, entryOrderProductList))
                .build();

        emailRestClient.sendEmail(emailDTO);

        log.info("Email enviado com sucesso");
    }

    private Map<String, String> fillAttributesFields(EntryOrder entryOrder,
                                                     List<EntryOrderProduct> entryOrderProductList) {
        Map<String, String> maps = new HashedMap<>();

        int countTotalExpected = entryOrder.getEntryOrderProductList().size();
        int countNotStored = entryOrderProductList.size();
        int countStored = countTotalExpected - countNotStored;

        EntryOrderStatusEnum entryOrderStatusEnum = EntryOrderStatusEnum.valueOf(entryOrder.getStatus());

        maps.put("#Codigo#", entryOrder.getCode());
        maps.put("#Status#", entryOrderStatusEnumMapper.toPtBr(entryOrderStatusEnum));
        maps.put("#Data#", formatDateEmail(entryOrder.getDateCreatedAt()));
        maps.put("#Esperada#", String.valueOf(countTotalExpected));
        maps.put("#Recebida#", String.valueOf(countStored));
        maps.put("#Diferenca#", String.valueOf(countNotStored));
        maps.put("#Produtos_nao_recebidos#", fillNotStoredProducts(entryOrderProductList));

        return maps;
    }

    private String fillNotStoredProducts(List<EntryOrderProduct> entryOrderProductList) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
                <style>
                    .products {
                        border-collapse: collapse;
                        width: 100%;
                    }
                            
                    .products th, .products td {
                        border: 1px solid black;
                        padding: 8px;
                        text-align: left;
                    }
                </style>""");

        sb.append("<table class=\"products\">")
                .append("<tr>")
                .append("<th>Código</th>")
                .append("<th>Título</th>")
                .append("</tr>");


        for (EntryOrderProduct entryOrderProduct : entryOrderProductList) {
            sb.append("<tr>")
                    .append("<td>")
                    .append(entryOrderProduct.getProductId())
                    .append("</td>")
                    .append("<td>")
                    .append(entryOrderProduct.getTitle())
                    .append("</td>")
                    .append("</tr>");
        }

        sb.append("</table>");

        return sb.toString();
    }

    private String formatDateEmail(LocalDateTime inputDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return inputDate.format(dateTimeFormatter);
    }
}