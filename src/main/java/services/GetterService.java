package services;

import repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("getter")
public class GetterService {

    /**
     * @return all beer in the database as a json
     */
    @Path("getBeer")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getBeer() {
        return Repository.getInstance().getBeer();
    }

    /**
     * @return all wine in the database as a json
     */
    @Path("getWine")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getWine() {
        return Repository.getInstance().getWine();
    }

    /**
     * @return all cocktails in the database as a json
     */
    @Path("getCocktails")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getCocktails() {
        return Repository.getInstance().getCocktails();
    }

    /**
     * @return all liquor in the database as a json
     */
    @Path("getLiquor")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLiquor() {
        return Repository.getInstance().getLiquor();
    }
}
