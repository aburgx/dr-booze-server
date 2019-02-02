package objects;

import java.util.Date;

public class DataTransferObject {

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private char gender;
    private Date birthday;
    private double height;
    private double weight;

    public DataTransferObject() {
    }

    public DataTransferObject(String username, String password) {
        this.username = username.toLowerCase();
        this.password = password;
    }

    public DataTransferObject(String username, String email, String password) {
        this.username = username.toLowerCase();
        this.email = email;
        this.password = password;
    }

    public DataTransferObject(String email, String firstName, String lastName, char gender,
                              Date birthday, double height, double weight) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
    }

    public DataTransferObject(String username, String email, String password, String firstName, String lastName,
                              char gender, Date birthday, double height, double weight) {
        this.username = username.toLowerCase();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthday = birthday;
        this.height = height;
        this.weight = weight;
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

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
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
