package entities;

import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
/*
    TODO: Regex for firstName and lastName
 */
@Entity
@Table(name = "Booze_Person")
@NamedQueries({
        @NamedQuery(name = "Person.get-with-user", query = "SELECT p FROM Person p WHERE p.user = :user")
})
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @NotNull(message = "601")
    private User user;

    @Size(min = 1, max = 100, message = "603")
    private String firstName;

    @Size(min = 1, max = 100, message = "603")
    private String lastName;

    @NotNull(message = "601")
    @Size(min = 1, max = 1, message = "603")
    private String gender;

    @Temporal(value = TemporalType.DATE)
    @NotNull(message = "601")
    private Date birthday;

    @NotNull(message = "601")
    private double weight;

    @NotNull(message = "601")
    private double height;

    public Person() {
    }

    public Person(User user, String firstName, String lastName, String gender, Date birthday, double weight, double height) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.weight = weight;
        this.height = height;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("firstName", firstName);
        json.put("lastName", lastName);
        json.put("gender", gender);
        json.put("birthday", birthday);
        json.put("weight", weight);
        json.put("height", height);
        return json;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

}
