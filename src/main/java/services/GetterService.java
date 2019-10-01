package services;

import repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("getter")
public class GetterService {
    private Repository repo = new Repository();

    /**
     * @param type the alcohol type
     * @return all alcohols as jsonarray
     */
    @Path("getAlcohols/{type}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlcohols(@PathParam("type") String type) {
        return repo.getAlcohols(type.toUpperCase());
    }
}