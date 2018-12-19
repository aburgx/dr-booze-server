package service;

import repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author Alexander Burghuber
 */
@Path("booze")
public class Service {

    @Path("message")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String message() { return " Hello REST Service powered by Java SE "; }

    @Path("testJPA")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        Repository.getInstance().testJPA();
        return "testing completed";
    }

}
