package data.entities;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Entity
@Table(name = "Booze_User")
@NamedQueries({
        @NamedQuery(name = "User.get-with-username", query = "SELECT u FROM User u WHERE u.username = :username"),
        @NamedQuery(name = "User.get-with-email", query = "SELECT u FROM User u WHERE u.email = :email")
})
public class User {

    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;
    private String salt;
    private boolean enabled = false;
    private boolean detailsSet = false;

    private String firstName;
    private String lastName;
    private String gender;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    private int height;
    private int weight;

    private int points;

    @OneToOne(cascade = CascadeType.MERGE)
    private VerificationToken verificationToken;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Drink> drinks;

    @ManyToMany
    @JoinTable(name = "Booze_FavouriteAlcohol")
    private Set<Alcohol> favouriteAlcohols;

    @OneToMany
    private List<Challenge> challenges;

    public User() {
        this.drinks = new ArrayList<>();
        this.favouriteAlcohols = new HashSet<>();
        this.challenges = new ArrayList<>();
    }

    public User(String username, String email, String password) {
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
                .put("birthday", birthday.getTime())
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
            // encrypted password
            this.password = new String(Hex.encode(hash));
            // salt
            this.salt = new String(Hex.encode(salt));
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

    public boolean isDetailsSet() {
        return detailsSet;
    }

    public void setDetailsSet(boolean detailsSet) {
        this.detailsSet = detailsSet;
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

    public List<Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(List<Drink> drinks) {
        this.drinks = drinks;
    }

    public Set<Alcohol> getFavouriteAlcohols() {
        return favouriteAlcohols;
    }

    public void setFavouriteAlcohols(Set<Alcohol> favouriteAlcohols) {
        this.favouriteAlcohols = favouriteAlcohols;
    }

    public List<Challenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<Challenge> challenges) {
        this.challenges = challenges;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
