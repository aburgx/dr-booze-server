package data.entities;

import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/*
    TODO: Regex for firstName and lastName
 */
@Entity
@Table(name = "Booze_Person")
@NamedQueries({
        @NamedQuery(name = "Person.get-with-user", query = "SELECT p FROM PersonBO p WHERE p.user = :user")
})
public class PersonBO {

    /**
     * the id of the person
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    /**
     * the associated user of the person
     */
    @OneToOne(mappedBy = "person", cascade = CascadeType.MERGE)
    @NotNull(message = "601")
    private UserBO user;
    /**
     * the first name of the person
     */
    @Size(max = 100, message = "603")
    private String firstName;
    /**
     * the last name of the person
     */
    @Size(max = 100, message = "603")
    private String lastName;
    /**
     * the gender of the person
     */
    @NotNull(message = "601")
    @Size(min = 1, max = 1, message = "603")
    private String gender;
    /**
     * the birthday of the person
     */
    @Temporal(value = TemporalType.DATE)
    @NotNull(message = "601")
    private Date birthday;
    /**
     * the height in cm
     */
    @NotNull(message = "601")
    private double height;
    /**
     * the weight in kg
     */
    @NotNull(message = "601")
    private double weight;

    public PersonBO() {
    }

    public PersonBO(UserBO user, String firstName, String lastName, String gender, Date birthday, double height, double weight) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("gender", gender);
        json.put("birthday", birthday.getTime());
        json.put("weight", weight);
        json.put("height", height);
        json.put("user", user.toJson());
        return json;
    }

    public long getId() {
        return id;
    }

    public UserBO getUser() {
        return user;
    }

    public void setUser(UserBO user) {
        this.user = user;
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

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}
