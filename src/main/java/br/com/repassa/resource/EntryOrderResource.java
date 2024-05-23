package br.com.repassa.resource;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.EntryOrderDTO;
import br.com.repassa.dto.EntryOrderFinishResponseDTO;
import br.com.repassa.dto.FilterDTO;
import br.com.repassa.dto.RegisterRequestDTO;
import br.com.repassa.dto.RegisterResponseDTO;
import br.com.repassa.dto.SecuritySealDTO;
import br.com.repassa.dto.SecuritySealResponseDTO;
import br.com.repassa.dto.SecuritySealUpdateDTO;
import br.com.repassa.dto.UpdateStatusDTO;
import br.com.repassa.service.EntryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
@Path("/v1/entryorder")
public class EntryOrderResource {

    @Inject
    EntryOrderService entryOrderService;

    @POST
    @Operation(summary = "Registra produtos em uma ordem de entrada", description = "Registro dos produtos na ordem de entrada")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public Response registerProductOnEntryOrder(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) throws RepassaException {
        log.info("Iniciando o registro de produto na ordem de entrada");
        RegisterResponseDTO registerResponseDTO = entryOrderService.registerProductOnEntryOrder(registerRequestDTO);
        log.info("Registro de produto na ordem de entrada criado com sucesso");
        return Response.ok(registerResponseDTO).build();
    }

    @DELETE
    @Operation(summary = "Remover Item/Produto de uma Ordem de Entrada", description = "Esta operação permite a exclusão de um ou múltiplos produtos específicos de uma ordem de entrada.")
    @Path("/remove")
    public Response deleteProductsEntryOrder(List<Long> productIds) throws RepassaException {
        log.info("Iniciando a remocao de produtos da ordem de entrada.");
        entryOrderService.deleteProductsEntryOrder(productIds);
        if (productIds.size() == 1) {
            log.info("Produto removido com sucesso da ordem de entrada.");
            return Response.ok("Sucesso! Produto removido com sucesso da ordem de entrada.").build();
        } else {
            log.info("Produtos removidos com sucesso da ordem de entrada.");
            return Response.ok("Sucesso! Produtos removidos com sucesso da ordem de entrada.").build();
        }
    }

    @GET
    @Operation(summary = "Lista os dados de uma ordem de entrada", description = "Lista os dados de uma ordem de entrada e os produtos vinculados")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEntryOrderByCode(@QueryParam("entryOrderCode") String entryOrderCode) throws RepassaException {
        log.info("Iniciando listagem de dados da ordem de entrada");
        EntryOrderDTO productEntryOrderDTO = entryOrderService.getEntryOrderWithProducts(entryOrderCode);
        log.info("Listagem de dados da ordem de entrada concluida com sucesso");
        return Response.ok(productEntryOrderDTO).build();
    }

    @POST
    @Operation(summary = "Buscar a listagem das ordens de entrada", description = "Endpoint usado para buscar sa listagem das ordens de entrada.")
    @Path("/findorders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findBagsForProduct(@DefaultValue("0") @RestQuery("page") int page,
                                       @DefaultValue("40") @RestQuery("size") int size,
                                       @RequestBody @Valid FilterDTO filterDTO)
    throws RepassaException {
        log.info("Iniciando busca por orders");
        filterDTO.setPage(page);
        filterDTO.setSize(size);
        Response response = Response.ok(entryOrderService.getOrdersList(filterDTO)).build();
        log.info("Busca por orders realizada com sucesso");
        return response;
    }

    @POST
    @Operation(summary = "Salvar código do lacre", description = "Endpoint para salvar no banco o código do lacre da ordem de entrada.")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/securityseal")
    public Response createdSecuritySeal(@RequestBody @Valid SecuritySealDTO securitySealDTO) throws RepassaException {
        SecuritySealResponseDTO response = SecuritySealResponseDTO.builder().securitySealOk("Sucesso! Código do lacre salvo!").build();
        log.info("Salvando informacoes do lacre no banco de dados");
        entryOrderService.createSecuritySeal(securitySealDTO);
        log.info("Informacoes do lacre salvas com sucesso");
        return Response.ok(response).build();
    }

    @PUT
    @Operation(summary = "Alterar código do lacre", description = "Endpoint para alterar código do lacre da ordem de entrada.")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/securityseal")
    public Response updateSecuritySeal(@RequestBody @Valid SecuritySealUpdateDTO securitySealUpdateDTO) throws RepassaException {
        SecuritySealResponseDTO response = SecuritySealResponseDTO.builder().securitySealOk("Sucesso! Novo código de lacre salvo com sucesso.").build();
        log.info("Alterando informacoes do lacre no banco de dados");
        entryOrderService.updateSecuritySeal(securitySealUpdateDTO);
        log.info("Informacoes do lacre foram alteradas com sucesso");
        return Response.ok(response).build();
    }

    @GET
    @Operation(summary = "Lista os detalhes de uma ordem de entrada", description = "Lista os detalhes de uma ordem de entrada e os produtos vinculados")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/entryorderdetail")
    public Response getDetailEntryOrder(@QueryParam("entryOrderCode") String entryOrderCode) throws RepassaException {
        log.info("Iniciando detalhes da ordem de entrada");
        EntryOrderDTO productEntryOrderDTO = entryOrderService.getEntryOrderWithProducts(entryOrderCode);
        log.info("Detalhes da ordem de entrada concluida com sucesso");
        return Response.ok(productEntryOrderDTO).build();
    }

    @PUT
    @Operation(summary = "Altera o status da ordem de entrada", description = "Altera o status da ordem de entrada")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/status")
    public Response updateEntryOrderStatus(@RequestBody UpdateStatusDTO updateStatusDTO) throws RepassaException {
        log.info("Iniciando alteracao do status da ordem de entrada");
        entryOrderService.updateEntryOrderStatus(updateStatusDTO);
        log.info("Alteracao do status da ordem de entrada concluida sucesso");
        return Response.noContent().build();
    }

    @PUT
    @Operation(summary = "Concluir ordem de entrada", description = "Endpoint utilizado para concluir uma ordem de entrada")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/finish")
    public Response finishOrder(@QueryParam("entryOrderCode") String entryOrderCode) throws RepassaException {
        log.info("Iniciando finalizacao da ordem de entrada");
        entryOrderService.finishEntryOrder(entryOrderCode);
        log.info("Ordem de entrada foi finalizada com sucesso");
        EntryOrderFinishResponseDTO entryOrderFinishResponseDTO = EntryOrderFinishResponseDTO.builder()
                .responseMessage("Sucesso! Ordem de entrada enviada!")
                .build();
        return Response.ok(entryOrderFinishResponseDTO).build();
    }
}
