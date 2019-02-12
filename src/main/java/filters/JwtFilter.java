package filters;

import objects.JwtBuilder;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext rc) {
        // Checks if the JWT token is valid when the url path includes the ManageService
        if (!rc.getUriInfo().getPath().contains("manage") || rc.getMethod().equals("OPTIONS"))
            return;
        JwtBuilder jwtBuilder = new JwtBuilder();
        try {
            String[] auth = rc.getHeaderString("Authorization")
                    .split("\\s");
            jwtBuilder.checkSubject(auth[1]);
        } catch (Exception ex) {
            System.out.println("Unauthorized activity has been detected");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

}

