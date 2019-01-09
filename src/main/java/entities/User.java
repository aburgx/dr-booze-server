package entities;

import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.security.MessageDigest;

@Entity(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.getUser", query = "SELECT u FROM Booze_User u WHERE u.username = :username"),
        @NamedQuery(name = "User.checkUniqueName", query = "SELECT COUNT(u) FROM Booze_User u WHERE u.username = :username"),
        @NamedQuery(name = "User.checkUniqueEmail", query = "SELECT COUNT(u) FROM Booze_User u WHERE u.email = :email")
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

    //TODO: add new error code for password pattern on register
    @NotNull(message = "601")
    @Pattern(message = "605", regexp = "^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$")
    @Size(min = 8, max = 25, message = "603")
    private String password;

    private boolean enabled = false;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username.toLowerCase();
        this.email = email;
        this.password = password;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("password", "***");
        json.put("email", email);
        json.put("username", username);
        return json.toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
