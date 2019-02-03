package repositories;

import entities.Person;
import entities.User;
import entities.VerificationToken;
import mail.MailService;
import objects.ErrorGenerator;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import javax.persistence.*;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexander Burghuber
 */
@SuppressWarnings("Duplicates")
public class AuthenticationRepo {

    private EntityManager em;
    private Validator validator;
    private ErrorGenerator errorgen;
    private MailService mail;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private static AuthenticationRepo instance = null;

    private AuthenticationRepo() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
        this.em = emf.createEntityManager();
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        this.validator = vf.getValidator();
        this.errorgen = new ErrorGenerator();
        this.mail = new MailService();
    }

    public static AuthenticationRepo getInstance() {
        if (instance == null)
            instance = new AuthenticationRepo();
        return instance;
    }

    /**
     * Registers a new user and validates his input
     *
     * @param username the username of the user
     * @param email    the email of the user
     * @param password the password of the user
     * @return a json String that includes either the newly registered user or all validation errors
     */
    public String register(final String username, final String email, final String password) {
        User user = new User(username, email, password);

        // validate the user
        String resultUser = validateUser(user);
        if (resultUser != null)
            return resultUser;

        TypedQuery<Long> queryUniqueName = em.createNamedQuery("User.count-username", Long.class).setParameter("username", username);
        long numberOfEntriesName = queryUniqueName.getSingleResult();

        TypedQuery<Long> queryUniqueEmail = em.createNamedQuery("User.count-email", Long.class).setParameter("email", email);
        long numberOfEntriesEmail = queryUniqueEmail.getSingleResult();

        // check if the username or the email is already taken
        if (numberOfEntriesName != 0) {
            return errorgen.generate(602, "username");
        }
        if (numberOfEntriesEmail != 0) {
            return errorgen.generate(602, "email");
        }

        // setup the verification token of the user
        VerificationToken verificationToken = new VerificationToken(user);
        System.out.println("Setup token: " + verificationToken.getToken() + " Expire: " + verificationToken.getExpiryDate());

        // persist the new user
        em.getTransaction().begin();
        em.persist(user);
        em.persist(verificationToken);
        em.getTransaction().commit();

        // multithreaded email sending
        executor.execute(() -> {
            System.out.println("Sending email confirmation...");
            mail.sendConfirmation(user, verificationToken);
            System.out.println("Email confirmation sent.");
        });

        // return user as json
        JSONObject json = new JSONObject();
        json.put("user", user.toJson());
        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Inserts the details of an user as a person object
     *
     * @param email     the email of the already existing user
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @param gender    the gender of the user
     * @param birthday  the birthday of the user
     * @param height    the height of the user
     * @param weight    the weight of the user
     * @return a json String that includes either the user or all validation errors
     */
    public String insertDetails(final String email, final String firstName, final String lastName, final String gender,
                                final Date birthday, final double height, final double weight) {

        // check if the gender, height and weight is incorrect
        if (!gender.equals("m") && !gender.equals("f")) {
            return errorgen.generate(604, "gender");
        } else if (height < 150.0 || height > 230.0) {
            return errorgen.generate(604, "height");
        } else if (weight < 30 || weight > 200) {
            return errorgen.generate(604, "weight");
        }

        TypedQuery<User> queryGetUser = em.createNamedQuery("User.get-with-email", User.class).setParameter("email", email);
        List<User> resultsGetUser = queryGetUser.getResultList();

        // check if an user exists with this email
        if (resultsGetUser.size() == 0) {
            return errorgen.generate(607, "user");
        }

        User user = resultsGetUser.get(0);
        Person person = new Person(user, firstName, lastName, gender, birthday, height, weight);

        // validate the person
        String resultPerson = validatePerson(person);
        if (resultPerson != null)
            return resultPerson;

        // persist the person
        em.getTransaction().begin();
        em.persist(person);
        em.getTransaction().commit();

        // if the update was successful return the user and if the person has been set return also the person
        JSONObject json = new JSONObject();
        json.put("user", user.toJson());
        json.put("person", person.toJson());

        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Updates the details of an user
     *
     * @param username  the username of the user
     * @param email     the new email of the user
     * @param password  the new password of the user
     * @param firstName the new firstName of the user
     * @param lastName  the new lastName of the user
     * @param gender    the new gender of the user
     * @param birthday  the new birthday of the user
     * @param height    the new height of the user
     * @param weight    the new weight of the user
     * @return a json String that includes either the user or all validation errors
     */
    public String updateDetails(final String username, final String email, final String password, final String firstName,
                                final String lastName, final String gender, final Date birthday, final double height, final double weight) {
        System.out.println("username: " + username + " email: " + email + " password: " + password +
                " firstName: " + firstName + " lastName: " + lastName + " gender: " + gender + " birthday: " + birthday +
                " height: " + height + " weight: " + weight);

        TypedQuery<User> queryGetUser = em.createNamedQuery("User.get-with-username", User.class).setParameter("username", username);
        List<User> resultsGetUser = queryGetUser.getResultList();

        // check if an user exists with this username
        if (resultsGetUser.size() == 0) {
            return errorgen.generate(607, "user");
        }

        User user = resultsGetUser.get(0);

        TypedQuery<Person> queryGetPerson = em.createNamedQuery("Person.get-with-user", Person.class).setParameter("user", user);
        Person person = queryGetPerson.getSingleResult();

        // set the new value if the value is not null
        if (email != null)
            user.setEmail(email);
        if (password != null)
            user.setPassword(password);
        if (firstName != null)
            person.setFirstName(firstName);
        if (lastName != null)
            person.setLastName(lastName);
        if (birthday != null)
            person.setBirthday(birthday);
        if (gender != null) {
            if (!gender.equals("m") && !gender.equals("f"))
                return errorgen.generate(604, "gender");
            person.setGender(gender);
        }
        if (height != 0) {
            if (height < 150.0 || height > 230.0)
                return errorgen.generate(604, "height");
            person.setHeight(height);
        }
        if (weight != 0) {
            if (weight < 30 || weight > 200)
                return errorgen.generate(604, "weight");
            person.setWeight(weight);
        }

        // validate the user
        String resultUser = validateUser(user);
        if (resultUser != null)
            return resultUser;

        // validate the person
        String resultPerson = validatePerson(person);
        if (resultPerson != null)
            return resultPerson;

        // persist the updated user & person
        em.getTransaction().begin();
        em.persist(user);
        em.persist(person);
        em.getTransaction().commit();

        // if the update was successful return the user and if the person has been set return also the person
        JSONObject json = new JSONObject();
        json.put("user", user.toJson());
        json.put("person", person.toJson());

        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Logs the user in if the username and password is correct
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a json containing either the user if the login was successful or an error code
     */
    public String login(final String username, final String password) {
        // check if the username exists in the database
        TypedQuery<User> queryGetUser = em.createNamedQuery("User.get-with-username", User.class).setParameter("username", username);
        List<User> resultsGetUser = queryGetUser.getResultList();

        if (resultsGetUser.size() == 0) {
            return errorgen.generate(605, "login");
        }

        // check if the password is correct
        User user = resultsGetUser.get(0);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Hex.decode(user.getSalt()));
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

            if (!new String(Hex.encode(hash)).equals(user.getPasswordHash())) {
                return errorgen.generate(605, "login");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        System.out.println("Logged in: " + user.getUsername());

        // if the login was successful return the user and if the person has been set return also the person
        JSONObject json = new JSONObject();
        json.put("user", user.toJson());

        TypedQuery<Person> queryGetPerson = em.createNamedQuery("Person.get-with-user", Person.class).setParameter("user", user);
        List<Person> resultsGetPerson = queryGetPerson.getResultList();
        if (resultsGetPerson.size() != 0) {
            Person person = resultsGetPerson.get(0);
            json.put("person", person.toJson());
        }
        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Verifies an user using the token that was send with the url inside the verification email.
     *
     * @param token the verification token
     * @return a boolean that indicates if the verification was successful or not
     */
    public boolean verify(final String token) {
        // check if the token exists
        List<VerificationToken> tokenList
                = em.createQuery("SELECT v FROM VerificationToken v WHERE v.token = :token", VerificationToken.class)
                .setParameter("token", token)
                .getResultList();
        if (tokenList.size() != 0) {
            VerificationToken verifyToken = tokenList.get(0);

            Date currentDate = new Date();
            Date tokenDate = verifyToken.getExpiryDate();

            if (tokenDate.compareTo(currentDate) >= 0) {
                User user = verifyToken.getUser();
                // set the user enabled and delete the token
                em.getTransaction().begin();
                user.setEnabled(true);
                em.remove(verifyToken);
                em.getTransaction().commit();
                return true;
            }
        }
        return false;
    }

    private String validateUser(User user) {
        Set<ConstraintViolation<User>> userViolations = validator.validate(user);
        if (userViolations.size() > 0) {
            List<JSONObject> jsonList = new ArrayList<>();

            userViolations.forEach(violation -> {
                JSONObject errorJson = new JSONObject();
                errorJson.accumulate("error_code", violation.getMessage());
                errorJson.accumulate("error_reason", violation.getPropertyPath());
                jsonList.add(errorJson);
            });

            // return the violations
            JSONObject json = new JSONObject();
            json.put("error", jsonList);
            String jsonString = json.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }
        return null;
    }

    private String validatePerson(Person person) {
        Set<ConstraintViolation<Person>> personViolations = validator.validate(person);
        if (personViolations.size() > 0) {
            List<JSONObject> jsonList = new ArrayList<>();

            personViolations.forEach(violation -> {
                JSONObject errorJson = new JSONObject();
                errorJson.accumulate("error_code", violation.getMessage());
                errorJson.accumulate("error_reason", violation.getPropertyPath());
                jsonList.add(errorJson);
            });

            // return the violations
            JSONObject json = new JSONObject();
            json.put("error", jsonList);
            String jsonString = json.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }
        return null;
    }

}