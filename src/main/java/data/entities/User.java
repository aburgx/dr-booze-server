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

    /**
     * The unique id
     */
    @Id
    @GeneratedValue
    private long id;

    /**
     * The username
     */
    @Column(unique = true)
    private String username;

    /**
     * The email
     */
    @Column(unique = true)
    private String email;

    /**
     * The encrypted password
     */
    private String password;

    /**
     * The salt used for the encryption of the password
     */
    private String salt;

    /**
     * If the email was confirmed
     */
    private boolean enabled = false;

    /**
     * If the details (height, weight, ...) have been set
     */
    private boolean detailsSet = false;

    /**
     * The first name
     */
    private String firstName;

    /**
     * The last name
     */
    private String lastName;

    /**
     * The gender
     */
    private String gender;

    /**
     * The birthday
     */
    @Temporal(TemporalType.DATE)
    private Date birthday;

    /**
     * The height
     */
    private int height;

    /**
     * The weight
     */
    private int weight;

    /**
     * The amount of booze points the user has collected with challenges
     */
    private int points;

    /**
     * The token used for verification
     */
    @OneToOne(cascade = CascadeType.MERGE)
    private VerificationToken verificationToken;

    /**
     * The drinks that the user has drunk
     */
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private List<Drink> drinks;

    /**
     * The favourite alcohols of the user
     */
    @ManyToMany
    @JoinTable(name = "Booze_FavouriteAlcohol")
    private Set<Alcohol> favouriteAlcohols;

    /** The current challenges for the user */
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

    /**
     * Creates a {@link JSONObject} of specific user properties
     *
     * @return the JSONObject
     */
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
