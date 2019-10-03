package helper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JwtHelper {
    private static String key = null;

    public JwtHelper() {
        if (key == null) {
            // load the jwt key from the config file
            try (InputStream input = new FileInputStream("src/main/resources/properties/config.properties")) {
                Properties prop = new Properties();
                prop.load(input);
                key = prop.getProperty("jwt_key");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public String create(long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public long getUserId(String token) {
        String subject = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(subject);
    }
}
