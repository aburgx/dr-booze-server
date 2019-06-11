package filters;

import helper.JwtHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext rc) {
        // Checks if the JWT token is valid when the url path includes the ManageService
        if (!rc.getUriInfo().getPath().contains("manage") || rc.getMethod().equals("OPTIONS"))
            return;
        JwtHelper jwtHelper = new JwtHelper();
        try {
            String[] auth = rc.getHeaderString(HttpHeaders.AUTHORIZATION)
                    .split("\\s");
            jwtHelper.checkSubject(auth[1]);
        } catch (Exception ex) {
            System.out.println("Unauthorized activity has been detected");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

    }

}

