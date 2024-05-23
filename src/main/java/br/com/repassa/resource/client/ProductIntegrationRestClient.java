package br.com.repassa.resource.client;

import br.com.repassa.dto.RenovaProductDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path(value = "/v1/product/")
@RegisterRestClient(configKey = "product-integration-resource")
public interface ProductIntegrationRestClient {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    RenovaProductDTO findProductById(@QueryParam("productId") Long productId,
                                     @HeaderParam("Authorization") String token);
}
