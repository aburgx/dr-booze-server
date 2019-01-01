package entities;

import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.checkUniqueName", query = "SELECT COUNT(u) FROM Booze_User u WHERE u.username = :username"),
        @NamedQuery(name = "User.checkUniqueEmail", query = "SELECT COUNT(u) FROM Booze_User u WHERE u.email = :email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull(message = "notnull")
    @Size(min = 4, max = 25, message = "notbetween{min}-{max}")
    @Column(unique = true)
    private String username;

    @NotNull(message = "notnull")
    @Email(message = "notanemail", regexp =
            "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\" +
                    "[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
    @Column(unique = true)
    private String email;

    @NotNull(message = "notnull")
    @Size(min = 8, max = 25, message = "notbetween{min}-{max}")
    private String password;

    public User() {
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("email", email);
        json.put("password", "***");
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

    public void setPassword(String password) {
        this.password = password;
    }

}
