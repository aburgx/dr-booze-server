package data.entities;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.get-with-username", query = "SELECT u FROM UserBO u WHERE u.username = :username"),
        @NamedQuery(name = "User.get-with-email", query = "SELECT u FROM UserBO u WHERE u.email = :email")
})
public class UserBO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.MERGE)
    private VerificationToken verificationToken;

    @OneToMany(mappedBy = "user")
    private List<DrinkBO> drinks;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;
    private String salt;
    private boolean enabled = false;

    private String firstName;
    private String lastName;
    private String gender;

    @Temporal(TemporalType.DATE)
    private Date birthday;
    private int height;
    private int weight;

    @OneToMany
    private List<ChallengeBO> challenges;
    private int points;

    public UserBO() {
        this.drinks = new ArrayList<>();
        this.challenges = new ArrayList<>();
    }

    public UserBO(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        hashPassword(password);
    }

    public JSONObject toJson() {
        return new JSONObject()
                .put("username", username)
                .put("email", email)
                .put("firstName", firstName)
                .put("lastName", lastName)
                .put("gender", gender)
                .put("birthDay", birthday)
                .put("height", height)
                .put("weight", weight)
                .put("points", points);
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

    public void setId(long id) {
        this.id = id;
    }

    public VerificationToken getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(VerificationToken verificationToken) {
        this.verificationToken = verificationToken;
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
        hashPassword(password);
    }

    public List<DrinkBO> getDrinks() {
        return drinks;
    }

    public void setDrinks(List<DrinkBO> drinks) {
        this.drinks = drinks;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public List<ChallengeBO> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<ChallengeBO> challenges) {
        this.challenges = challenges;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}