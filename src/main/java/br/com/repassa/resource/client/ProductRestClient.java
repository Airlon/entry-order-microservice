package br.com.repassa.resource.client;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.repassa.dto.FindProductDTO;
import br.com.repassa.dto.EntryOrderStatusDTO;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path(value = "/v1/product")
@RegisterRestClient(configKey = "product-resource")
public interface ProductRestClient {
    @PUT
    @Path("/entryorderstatus")
    @Produces(MediaType.APPLICATION_JSON)
    Response updateProductOrderStatus(@RequestBody EntryOrderStatusDTO orderEntryStatusDTO,
                                      @HeaderParam("Authorization") String token);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    FindProductDTO findProductById(@QueryParam("productId") Long productId,
                                   @HeaderParam("Authorization") String token);
}
