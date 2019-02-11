package objects;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtBuilder {

    private String key = "secret-booze";

    public String create(String subject) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS256, key)
                .setSubject(subject)
                .compact();
    }

    public String checkSubject(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
