package objects;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JwtBuilder {

    private String key = null;

    public JwtBuilder() {
        // load the jwt key from the config file
        try (InputStream input = new FileInputStream("src/main/resources/properties/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            key = prop.getProperty("jwt_key");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String create(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public String checkSubject(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (SignatureException ex) {
            return null;
        }
    }
}
