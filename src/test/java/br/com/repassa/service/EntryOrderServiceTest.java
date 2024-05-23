package br.com.repassa.service;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.EmailDTO;
import br.com.repassa.dto.EntryOrderListDTO;
import br.com.repassa.dto.EntryOrderListResponseDTO;
import br.com.repassa.dto.FilterDTO;
import br.com.repassa.dto.FindProductDTO;
import br.com.repassa.dto.RegisterRequestDTO;
import br.com.repassa.dto.RenovaProductDTO;
import br.com.repassa.dto.SecuritySealDTO;
import br.com.repassa.dto.SecuritySealUpdateDTO;
import br.com.repassa.dto.UpdateStatusDTO;
import br.com.repassa.entity.EntryOrder;
import br.com.repassa.entity.EntryOrderProduct;
import br.com.repassa.entity.SecuritySeal;
import br.com.repassa.enums.ClassificationEnum;
import br.com.repassa.enums.EntryOrderProductStatusEnum;
import br.com.repassa.enums.EntryOrderStatusEnum;
import br.com.repassa.enums.PhotographyStatusEnum;
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
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.PersistenceException;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntryOrderServiceTest {

    @Mock
    EntryOrderRepository entryOrderRepository;

    @Mock
    ProductRestClient productRestClient;

    @Mock
    ProductIntegrationRestClient productIntegrationRestClient;

    @Mock
    EntryOrderMapper entryOrderMapper;

    @Mock
    EntryOrderProductRepository entryOrderProductRepository;

    @Mock
    SecuritySealRepository securitySealRepository;

    @Mock
    SecuritySealLogRepository securitySealLogRepository;

    @Mock
    EntryOrderLogRepository entryOrderLogRepository;

    @Mock
    EntryOrderProductLogRepository entryOrderProductLogRepository;

    @Mock
    HttpHeaders headers;

    @Mock
    EmailRestClient emailRestClient;

    @Mock
    JsonWebToken token;

    @Mock
    EntryOrderStatusEnumMapper entryOrderStatusEnumMapper;

    @InjectMocks
    EntryOrderService entryOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetFormatedDateWithSuccess() {
        String formatedDate = entryOrderService.getFormattedDate(LocalDateTime.of(2023, 11, 14, 15, 30));

        assertEquals("14/11/2023", formatedDate);
    }

    @Test
    void shouldRegisterProductOnEntryOrder() throws RepassaException {
        Long productId = 123L;

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type("NEW_PRODUCT")
                .classification("NORMAL")
                .productId(productId)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .code("E14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .type(TypeEnum.NEW_PRODUCT.getType())
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        when(entryOrderMapper.toEntity(any(), any(), any())).thenReturn(entryOrder);

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(any()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        entryOrderService.registerProductOnEntryOrder(registerRequestDTO);

        verify(entryOrderProductRepository, times(1)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowCodigoOrdemDeEntradaJaExisteErrorInRegisterProductOnEntryOrder() {
        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type("NEW_PRODUCT")
                .classification("NORMAL")
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.countByCode(any())).thenReturn(1L);

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(EntryOrderError.CODIGO_ORDEM_DE_ENTRADA_JA_EXISTE, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldThrowTipoInvalidoErrorInInRegisterProductOnEntryOrder() {
        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type("NEW_PRODUCT_ERROR")
                .classification("NORMAL")
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.countByCode(any())).thenReturn(1L);

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(EntryOrderError.TIPO_INVALIDO, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldThrowClassificacaoInvalidaErrorInRegisterProductOnEntryOrder() {
        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type("NEW_PRODUCT")
                .classification("NORMAL_ERROR")
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.countByCode(any())).thenReturn(1L);

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(EntryOrderError.CLASSIFICACAO_INVALIDA, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldThrowErroAoConectarNoBancoErrorInRegisterProductOnEntryOrder() {

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type("NEW_PRODUCT")
                .classification("NORMAL")
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .code("E14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);
        when(entryOrderRepository.countByCode(any())).thenReturn(0L);
        when(entryOrderMapper.toEntity(any(), any(), any())).thenReturn(entryOrder);
        doThrow(PersistenceException.class).when(entryOrderRepository).persist(entryOrder);

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldThrowExceptionWhenProductPhotoStatusNotFinished() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.AWAITING_PHOTOGRAPHY.getStatus())
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        verify(entryOrderProductRepository, times(0)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowExceptionWhenProductRegistrationStatusNotFinished() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.IN_PROGRESS.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        verify(entryOrderProductRepository, times(0)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowExceptionWhenExistsOrderEntryWithProductId() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.IN_PROGRESS.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        EntryOrderProduct entryOrderProduct = EntryOrderProduct.builder()
                .entryOrder(EntryOrder.builder().code("XWZ").build())
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.of(entryOrderProduct));

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        verify(entryOrderProductRepository, times(0)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowExceptionWhenExistsOrderEntryWithProductIdAndSameCode() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.IN_PROGRESS.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        EntryOrderProduct entryOrderProduct = EntryOrderProduct.builder()
                .entryOrder(EntryOrder.builder().code(entryOrderCode).build())
                .build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.of(entryOrderProduct));

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        verify(entryOrderProductRepository, times(0)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowExceptionWhenPersistenceError() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(true)
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        doThrow(PersistenceException.class).when(entryOrderProductRepository).persist(any(EntryOrderProduct.class));

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenProductIsNull() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        EntryOrder entryOrder = EntryOrder.builder().code("CODE").build();

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(null);

        when(entryOrderRepository.findByCodeOptional(registerRequestDTO.getEntryOrderCode()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));
    }

    @Test
    void shouldThrowProdutoNaoEnviadoAoHJErrorWhenRegisterProductOnEntryOrder() {
        Long productId = 123L;
        String entryOrderCode = "ABC";

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .productId(productId)
                .entryOrderCode(entryOrderCode)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .type(TypeEnum.NEW_PRODUCT.getType())
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .build();

        FindProductDTO findProductDTO = FindProductDTO.builder()
                .registrationStatus(RegistrationStatusEnum.FINISHED.getStatus())
                .photographyStatus(PhotographyStatusEnum.FINISHED.getStatus())
                .sentToHJ(false)
                .build();

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));

        when(productRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(findProductDTO);

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(EntryOrderError.PRODUTO_NAO_ENVIADO_AO_HJ, repassaException.getRepassaUtilError());
    }

    @Test
    void deleteProductsEntryOrderTest() throws RepassaException {
        Long productId = 1L;

        List<Long> productIds = List.of(productId);

        FindProductDTO product = new FindProductDTO();
        product.setProductId(productId);
        product.setEntryOrderStatus(EntryOrderStatusEnum.PENDING.name());

        EntryOrderProduct entryOrderProduct = new EntryOrderProduct();
        entryOrderProduct.setId(1L);
        entryOrderProduct.setEntryOrderProductStatus(EntryOrderProductStatusEnum.IN_PROGRESS.name());
        EntryOrder entryOrder1 = new EntryOrder();
        entryOrder1.setId(1L);
        entryOrder1.setStatus(EntryOrderStatusEnum.PENDING.name());
        entryOrderProduct.setEntryOrder(entryOrder1);

        when(productRestClient.findProductById(eq(productId), anyString())).thenReturn(product);

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.getStatus()))
                .thenReturn(Optional.of(entryOrderProduct));

        entryOrderService.deleteProductsEntryOrder(productIds);

        verify(entryOrderProductRepository, times(1)).findByIdProductAndStatus(any(), any());
        verify(entryOrderProductRepository, times(1)).delete(any(EntryOrderProduct.class));
    }

    @Test
    void shouldGetEntryOrderWithProducts() throws RepassaException {
        String entryOrderCode = "E21112023151617";

        EntryOrder entryOrder = EntryOrder.builder()
                .code("E14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .build();

        when(entryOrderRepository.findByCodeOptional(entryOrderCode)).thenReturn(Optional.of(entryOrder));
        when(entryOrderProductRepository.findByEntryOrder(any())).thenReturn(new ArrayList<>());

        entryOrderService.getEntryOrderWithProducts(entryOrderCode);

        verify(entryOrderRepository, times(1)).findByCodeOptional(entryOrderCode);
        verify(entryOrderProductRepository, times(1)).findByEntryOrder(entryOrder);
    }

    @Test
    void getOrdersListValidFiltersShouldReturnListOfDTOs() throws RepassaException {

        EntryOrderListDTO entryOrderListDTO1 = new EntryOrderListDTO();
        EntryOrderListDTO entryOrderListDTO2 = new EntryOrderListDTO();

        entryOrderListDTO1.setUpdateddate("01/01/2000");
        entryOrderListDTO2.setUpdateddate("02/01/2000");

        FilterDTO filter = new FilterDTO();
        when(entryOrderRepository.listEntryOrders(filter)).thenReturn(Arrays.asList(
                entryOrderListDTO1, entryOrderListDTO2
        ));
        when(entryOrderRepository.listEntryOrdersQuantity(filter)).thenReturn(2);

        EntryOrderListResponseDTO result = entryOrderService.getOrdersList(filter);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void getOrdersListInvalidCountProductsShouldThrowRepassaException() {

        FilterDTO filter = new FilterDTO();
        filter.setCountProducts("abc");

        RepassaException exception = assertThrows(RepassaException.class, () -> entryOrderService.getOrdersList(filter));

        assertEquals(EntryOrderError.QUANTIDADE_INVALIDA, exception.getRepassaUtilError());

    }

    @Test
    void getOrdersListInvalidDateCreatedShouldThrowRepassaException() {

        FilterDTO filter = new FilterDTO();
        filter.setPrimaryUpdatedDate("data_invalida");

        RepassaException exception = assertThrows(RepassaException.class, () -> entryOrderService.getOrdersList(filter));

        assertEquals(EntryOrderError.DATA_INVALIDA, exception.getRepassaUtilError());
    }


    @Test
    void shouldCreateSecuritySealSucess() throws RepassaException {
        SecuritySealDTO testSecuritySeal = SecuritySealDTO.builder()
                .newSecuritySealCode("L151515151")
                .entryOrderCode("E5151515151")
                .build();

        when(entryOrderRepository.findByCodeOptional(testSecuritySeal.getEntryOrderCode())).thenReturn(Optional.of(new EntryOrder()));

        entryOrderService.createSecuritySeal(testSecuritySeal);

        verify(entryOrderRepository, times(1)).findByCodeOptional(testSecuritySeal.getEntryOrderCode());
        verify(securitySealRepository, times(1)).findBySecuritySealCodeOptional(testSecuritySeal.getNewSecuritySealCode());
    }

    @Test
    void shouldCreateSecurityNotAlphanumeric() {
        SecuritySealDTO testSecuritySeal = SecuritySealDTO.builder()
                .newSecuritySealCode("L151515151*")
                .entryOrderCode("E5151515151")
                .build();

        when(entryOrderRepository.findByCodeOptional(testSecuritySeal.getEntryOrderCode())).thenReturn(Optional.of(new EntryOrder()));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.createSecuritySeal(testSecuritySeal));
        assertEquals(EntryOrderError.CODIGO_LACRE_INVALIDO, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldCreateSecuritySealErrorEntryOrder() {
        SecuritySealDTO testSecuritySeal = SecuritySealDTO.builder()
                .newSecuritySealCode("L151515151")
                .entryOrderCode("E5151515151")
                .build();

        when(securitySealRepository.findBySecuritySealCodeOptional(testSecuritySeal.getNewSecuritySealCode())).thenReturn(Optional.of(new SecuritySeal()));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.createSecuritySeal(testSecuritySeal));
        assertEquals(EntryOrderError.ORDEM_DE_ENTRADA_NAO_ENCONTRADA, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldCreateSecuritySealErrorSecuritySealCodeExist() {
        SecuritySealDTO testSecuritySeal = SecuritySealDTO.builder()
                .newSecuritySealCode("L151515151")
                .entryOrderCode("E5151515151")
                .build();

        when(entryOrderRepository.findByCodeOptional(testSecuritySeal.getEntryOrderCode())).thenReturn(Optional.of(new EntryOrder()));
        when(securitySealRepository.findBySecuritySealCodeOptional(testSecuritySeal.getNewSecuritySealCode())).thenReturn(Optional.of(new SecuritySeal()));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.createSecuritySeal(testSecuritySeal));
        assertEquals(EntryOrderError.CODIGO_LACRE_JA_EXISTE, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldCreateSecuritySealErrorEntryOrderCodeExist() {
        SecuritySealDTO testSecuritySeal = SecuritySealDTO.builder()
                .newSecuritySealCode("L151515151")
                .entryOrderCode("E5151515151")
                .build();

        EntryOrder entryOrder = mock(EntryOrder.class);
        SecuritySeal securitySeal = mock(SecuritySeal.class);

        when(entryOrderRepository.findByCodeOptional(testSecuritySeal.getEntryOrderCode())).thenReturn(Optional.of(entryOrder));
        when(securitySealRepository.findByEntryOrderOptional(entryOrder)).thenReturn(Optional.of(securitySeal));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.createSecuritySeal(testSecuritySeal));
        assertEquals(EntryOrderError.ORDEM_DE_ENTRADA_JA_POSSUI_LACRE, repassaException.getRepassaUtilError());
    }



    @Test
    void shouldUpdateSecuritySeal() throws RepassaException {
        String entryOrderCode = "entryOrderCode";
        String newSecuritySealCode = "E5151515151";
        String securitySealCode = "securitySealCode";

        SecuritySealUpdateDTO securitySealUpdateDTO = SecuritySealUpdateDTO.builder()
                .entryOrderCode(entryOrderCode)
                .newSecuritySealCode(newSecuritySealCode)
                .build();

        EntryOrder entryOrder = EntryOrder.builder().build();
        SecuritySeal securitySeal = SecuritySeal.builder().securitySealCode(securitySealCode).build();

        when(entryOrderRepository.findByCodeOptional(entryOrderCode)).thenReturn(Optional.of(entryOrder));
        when(securitySealRepository.findByEntryOrderOptional(entryOrder)).thenReturn(Optional.of(securitySeal));

        entryOrderService.updateSecuritySeal(securitySealUpdateDTO);
    }

    @Test
    void shouldThrowRepassaExceptionWhenSecuritySealAlreadyExists() {
        String entryOrderCode = "entryOrderCode";
        String newSecuritySealCode = "E5151515151";
        String securitySealCode = "securitySealCode";

        SecuritySealUpdateDTO securitySealUpdateDTO = SecuritySealUpdateDTO.builder()
                .entryOrderCode(entryOrderCode)
                .newSecuritySealCode(newSecuritySealCode)
                .build();

        EntryOrder entryOrder = EntryOrder.builder().build();
        SecuritySeal securitySeal = SecuritySeal.builder().securitySealCode(securitySealCode).build();

        when(entryOrderRepository.findByCodeOptional(entryOrderCode)).thenReturn(Optional.of(entryOrder));
        when(securitySealRepository.findByEntryOrderOptional(entryOrder)).thenReturn(Optional.of(securitySeal));

        when(securitySealRepository.findBySecuritySealCodeOptional(newSecuritySealCode))
                .thenReturn(Optional.of(securitySeal));

        RepassaException repassaException = assertThrows(RepassaException.class,
                () -> entryOrderService.updateSecuritySeal(securitySealUpdateDTO));

        assertEquals(EntryOrderError.CODIGO_LACRE_JA_EXISTE, repassaException.getRepassaUtilError());
    }

    @Test
    void shouldThrowRepassaExceptionWhenUpdateWithSameSecuritySealCode() {
        String entryOrderCode = "entryOrderCode";
        String newSecuritySealCode = "L151515151";

        SecuritySealUpdateDTO securitySealUpdateDTO = SecuritySealUpdateDTO.builder()
                .entryOrderCode(entryOrderCode)
                .newSecuritySealCode(newSecuritySealCode)
                .build();

        EntryOrder entryOrder = EntryOrder.builder().build();
        SecuritySeal securitySeal = SecuritySeal.builder().securitySealCode(newSecuritySealCode).build();

        when(entryOrderRepository.findByCodeOptional(entryOrderCode)).thenReturn(Optional.of(entryOrder));
        when(securitySealRepository.findByEntryOrderOptional(entryOrder)).thenReturn(Optional.of(securitySeal));

        RepassaException repassaException = assertThrows(RepassaException.class,
                () -> entryOrderService.updateSecuritySeal(securitySealUpdateDTO));

        assertEquals(EntryOrderError.ALTERAR_MESMO_CODIGO_LACRE, repassaException.getRepassaUtilError());
    }


    @Test
    void shouldUpdateEntryOrderStatusToClosed() throws RepassaException {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.CLOSED.getStatus())
                .entryOrderCode("entryOrderCode")
                .receivedProducts(List.of(1L))
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .dateCreatedAt(LocalDateTime.now())
                .entryOrderProductList(List.of(EntryOrderProduct.builder().build()))
                .build();

        when(entryOrderProductRepository.findByEntryOrderAndStatus(any(), any()))
                .thenReturn(List.of(EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));
        when(entryOrderProductRepository.findByEntryOrderAndProductId(any(), anyLong()))
                .thenReturn(Optional.of(EntryOrderProduct.builder().build()));

        entryOrderService.updateEntryOrderStatus(updateStatusDTO);

        verify(entryOrderRepository, times(1)).persist(any(EntryOrder.class));
        verify(entryOrderProductRepository, times(2)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldNotSendEmailNotStoredItems() throws RepassaException {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.CLOSED.getStatus())
                .entryOrderCode("entryOrderCode")
                .receivedProducts(List.of(1L))
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .dateCreatedAt(LocalDateTime.now())
                .entryOrderProductList(List.of(EntryOrderProduct.builder().build()))
                .build();

        when(entryOrderProductRepository.findByEntryOrderAndStatus(any(), eq(EntryOrderProductStatusEnum.SENT)))
                .thenReturn(List.of());

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));
        when(entryOrderProductRepository.findByEntryOrderAndProductId(any(), anyLong()))
                .thenReturn(Optional.of(EntryOrderProduct.builder().build()));

        entryOrderService.updateEntryOrderStatus(updateStatusDTO);

        verify(emailRestClient, times(0)).sendEmail(any(EmailDTO.class));
    }

    @Test
    void shouldUpdateEntryOrderStatusToSent() throws RepassaException {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.SENT.getStatus())
                .entryOrderCode("entryOrderCode")
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .build();

        when(entryOrderProductRepository.findByEntryOrderAndProductId(any(),anyLong()))
                .thenReturn(Optional.of(EntryOrderProduct.builder().build()));

        when(entryOrderProductRepository.findByEntryOrder(any()))
                .thenReturn(List.of(EntryOrderProduct.builder().build(), EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));

        entryOrderService.updateEntryOrderStatus(updateStatusDTO);

        verify(entryOrderRepository, times(1)).persist(any(EntryOrder.class));
        verify(entryOrderProductRepository, times(2)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldUpdateStatusThrowExceptionWhenEnumParseError() {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus("ABC")
                .entryOrderCode("entryOrderCode")
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .build();

        when(entryOrderProductRepository.findByEntryOrder(any()))
                .thenReturn(List.of(EntryOrderProduct.builder().build(), EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.updateEntryOrderStatus(updateStatusDTO));

        assertEquals(repassaException.getRepassaUtilError(), EntryOrderError.STATUS_ORDEM_ENTRADA_INVALIDO);
    }

    @Test
    void shouldUpdateStatusThrowExceptionWhenDatabaseError() throws RepassaException {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.CLOSED.getStatus())
                .entryOrderCode("entryOrderCode")
                .receivedProducts(List.of(1L))
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .dateCreatedAt(LocalDateTime.now())
                .entryOrderProductList(List.of(EntryOrderProduct.builder().build()))
                .build();

        when(entryOrderProductRepository.findByEntryOrderAndStatus(any(), any()))
                .thenReturn(List.of(EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));
        when(entryOrderProductRepository.findByProductId(anyLong()))
                .thenReturn(Optional.empty());

        entryOrderService.updateEntryOrderStatus(updateStatusDTO);

        doThrow(PersistenceException.class).when(entryOrderProductRepository).persist(any(EntryOrderProduct.class));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.updateEntryOrderStatus(updateStatusDTO));

        assertEquals(repassaException.getRepassaUtilError(), EntryOrderError.ERRO_AO_CONECTAR_NO_BANCO);
    }

    @Test
    void shouldUpdateStatusThrowGenericException() throws RepassaException {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.CLOSED.getStatus())
                .entryOrderCode("entryOrderCode")
                .receivedProducts(List.of(1L))
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .dateCreatedAt(LocalDateTime.now())
                .entryOrderProductList(List.of(EntryOrderProduct.builder().build()))
                .build();

        when(entryOrderProductRepository.findByEntryOrderAndStatus(any(), any()))
                .thenReturn(List.of(EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));
        when(entryOrderProductRepository.findByProductId(anyLong()))
                .thenReturn(Optional.empty());

        entryOrderService.updateEntryOrderStatus(updateStatusDTO);

        doThrow(RuntimeException.class).when(entryOrderProductRepository).persist(any(EntryOrderProduct.class));

        RepassaException repassaException = assertThrows(RepassaException.class, () -> entryOrderService.updateEntryOrderStatus(updateStatusDTO));

        assertEquals(repassaException.getRepassaUtilError(), EntryOrderError.ERRO_AO_ATUALIZAR_STATUS_OE);
    }

    @Test
    void shouldUpdateEntryOrderErrorProductNotSentToHighjump() {
        UpdateStatusDTO updateStatusDTO = UpdateStatusDTO.builder()
                .entryOrderStatus(EntryOrderStatusEnum.CLOSED.getStatus())
                .entryOrderCode("entryOrderCode")
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .build();

        EntryOrderProduct entryOrderProduct = EntryOrderProduct.builder()
                .entryOrderProductStatus(EntryOrderProductStatusEnum.SENDING.getStatus())
                .build();

        when(entryOrderProductRepository.findByEntryOrder(entryOrder))
                .thenReturn(List.of(entryOrderProduct));

        when(entryOrderProductRepository.findByEntryOrderAndProductId(any(),anyLong()))
                .thenReturn(Optional.of(EntryOrderProduct.builder().build()));

        when(entryOrderRepository.findByCodeOptional(anyString())).thenReturn(Optional.of(entryOrder));

        RepassaException repassaException = assertThrows(RepassaException.class,
                () -> entryOrderService.updateEntryOrderStatus(updateStatusDTO));

        assertEquals(repassaException.getRepassaUtilError(), EntryOrderError.PRODUTO_NAO_ENVIADO_AO_HJ);
    }

    @Test
    void shouldCreateReverseEntryOrder() throws RepassaException {
        Long productId = 123L;

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type(TypeEnum.REVERSE.getType())
                .classification(ClassificationEnum.NORMAL.getClassification())
                .productId(productId)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .code("R14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .type(TypeEnum.REVERSE.getType())
                .build();

        RenovaProductDTO renovaProductDTO = RenovaProductDTO.builder()
                .title(RegistrationStatusEnum.FINISHED.getStatus())
                .currentStatus(RenovaProductStatus.CANCELED.getValue())
                .productId(productId)
                .build();

        when(entryOrderMapper.toEntity(any(), any(), any())).thenReturn(entryOrder);

        when(productIntegrationRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(renovaProductDTO);

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        entryOrderService.registerProductOnEntryOrder(registerRequestDTO);

        verify(entryOrderProductRepository, times(1)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldRegisterReverseEntryOrder() throws RepassaException {
        Long productId = 123L;

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type(TypeEnum.REVERSE.getType())
                .classification(ClassificationEnum.NORMAL.getClassification())
                .productId(productId)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .code("R14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .type(TypeEnum.REVERSE.getType())
                .build();

        RenovaProductDTO renovaProductDTO = RenovaProductDTO.builder()
                .title(RegistrationStatusEnum.FINISHED.getStatus())
                .currentStatus(RenovaProductStatus.SHORTAGE.getValue())
                .productId(productId)
                .build();

        when(entryOrderMapper.toEntity(any(), any(), any())).thenReturn(entryOrder);

        when(productIntegrationRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(renovaProductDTO);

        when(entryOrderRepository.findByCodeOptional(any()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        entryOrderService.registerProductOnEntryOrder(registerRequestDTO);

        verify(entryOrderProductRepository, times(1)).persist(any(EntryOrderProduct.class));
    }

    @Test
    void shouldThrowExceptionRegisterReverseInvalidStatusRenovaProduct() {
        Long productId = 123L;

        String tokenAuth = headers.getHeaderString(AUTHORIZATION);

        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO.builder()
                .type(TypeEnum.REVERSE.getType())
                .classification(ClassificationEnum.NORMAL.getClassification())
                .productId(productId)
                .build();

        EntryOrder entryOrder = EntryOrder.builder()
                .code("R14112023151617")
                .dateCreatedAt(LocalDateTime.now())
                .userUpdatedEmail("teste@teste.com.br")
                .status(EntryOrderStatusEnum.PENDING.getStatus())
                .type(TypeEnum.REVERSE.getType())
                .build();

        RenovaProductDTO renovaProductDTO = RenovaProductDTO.builder()
                .title(RegistrationStatusEnum.FINISHED.getStatus())
                .currentStatus(0)
                .productId(productId)
                .build();

        when(entryOrderMapper.toEntity(any(), any(), any())).thenReturn(entryOrder);

        when(productIntegrationRestClient.findProductById(registerRequestDTO.getProductId(), tokenAuth))
                .thenReturn(renovaProductDTO);

        when(entryOrderRepository.findByCodeOptional(any()))
                .thenReturn(Optional.of(entryOrder));

        when(entryOrderProductRepository.findByIdProductAndStatus(productId, EntryOrderStatusEnum.PENDING.name()))
                .thenReturn(Optional.empty());

        RepassaException repassaException = assertThrows(RepassaException.class,
                () -> entryOrderService.registerProductOnEntryOrder(registerRequestDTO));

        assertEquals(repassaException.getRepassaUtilError(), EntryOrderError.PRODUTO_RENOVA_NOVO);
    }
}
