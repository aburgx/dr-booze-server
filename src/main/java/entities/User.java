package entities;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Entity
@Table(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.getUser", query = "SELECT u FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.checkUniqueName", query = "SELECT COUNT(u) FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.checkUniqueEmail", query = "SELECT COUNT(u) FROM User u WHERE u.email = :email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull(message = "601")
    @Size(min = 4, max = 25, message = "603")
    @Column(unique = true)
    private String username;

    @NotNull(message = "601")
    @Email(message = "604", regexp =
            "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\" +
                    "[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
    @Size(min = 6, max = 100, message = "603")
    @Column(unique = true)
    private String email;

    @Transient
    @NotNull(message = "601")
    @Pattern(message = "605", regexp = "^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$")
    @Size(min = 8, max = 25, message = "603")
    private String password;

    private String passwordHash;
    private String salt;

    private boolean enabled = false;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("username", username);
        return json.toString();
    }

    @PrePersist
    public void hashPassword() {
        if (this.passwordHash == null) {
            // generate the salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            try {
                // setup the encryption
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);

                // encrypt
                byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
                String encryptedPassword = new String(Hex.encode(hash));
                this.passwordHash = encryptedPassword;
                String saltString = new String(Hex.encode(salt));
                this.salt = saltString;

                System.out.println("Encrypted Pwd: " + encryptedPassword + ", Salt: " + saltString);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

}
