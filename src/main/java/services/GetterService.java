package services;

import repositories.Repository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("getter")
public class GetterService {

    @Path("getBeer")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getBeer() {
        return Repository.getInstance().getBeer();
    }

    @Path("getWine")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getWine() {
        return Repository.getInstance().getWine();
    }

}
