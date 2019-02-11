package filters;

import objects.JwtBuilder;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

/*
@Provider
public class JwtFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext rc) {
        if (rc.getUriInfo().getPath().contains("jwt") || rc.getMethod().equals("OPTIONS"))
            return;

        JwtBuilder jwtBuilder = new JwtBuilder();
        String[] auth = rc.getHeaderString("Authorization")
                .split("\\s");
        String subject = jwtBuilder.checkSubject(auth[1]);
    }
}
*/
