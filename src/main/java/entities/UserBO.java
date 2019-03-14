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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.get-with-username", query = "SELECT u FROM UserBO u WHERE u.username = :username"),
        @NamedQuery(name = "User.get-with-email", query = "SELECT u FROM UserBO u WHERE u.email = :email"),
        @NamedQuery(name = "User.count-username", query = "SELECT COUNT(u) FROM UserBO u WHERE u.username = :username"),
        @NamedQuery(name = "User.count-email", query = "SELECT COUNT(u) FROM UserBO u WHERE u.email = :email")
})

public class UserBO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.MERGE)
    private VerificationToken verificationToken;

    @OneToOne(cascade = CascadeType.MERGE)
    private PersonBO person;

    @OneToMany(mappedBy = "user")
    private List<DrinkBO> drinks;

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

    private String password;
    private String salt;

    private boolean enabled = false;

    public UserBO() {
        this.drinks = new ArrayList<>();
    }

    public UserBO(String username, String email,
                  @NotNull(message = "601")
                  @Pattern(message = "604", regexp = "^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$")
                  @Size(min = 8, max = 25, message = "603")
                          String password) {
        this();
        this.username = username;
        this.email = email;
        hashPassword(password);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("email", email);
        return json;
    }

    private void hashPassword(String password) {
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
            this.password = encryptedPassword;
            String saltString = new String(Hex.encode(salt));
            this.salt = saltString;

            System.out.println("Encrypted Pwd: " + encryptedPassword + ", Salt: " + saltString);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public VerificationToken getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(VerificationToken verificationToken) {
        this.verificationToken = verificationToken;
    }

    public PersonBO getPerson() {
        return person;
    }

    public void setPerson(PersonBO person) {
        this.person = person;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            @NotNull(message = "601")
            @Pattern(message = "604", regexp = "^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$")
            @Size(min = 8, max = 25, message = "603")
                    String password) {
        hashPassword(password);

    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<DrinkBO> getDrinks() {
        return drinks;
    }

    public void setDrinks(List<DrinkBO> drinks) {
        this.drinks = drinks;
    }
}
