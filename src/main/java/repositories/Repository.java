package repositories;

import entities.User;
import org.json.JSONObject;
import services.Mail;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;

/**
 * @author Alexander Burghuber
 */
public class Repository {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private EntityManager em = emf.createEntityManager();
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    public String test() {
        return "Does currently nothing.";
    }

    /**
     * Registers a new user and validates his credentials
     *
     * @param username the username of the user
     * @param email    the email of the user
     * @param password the password of the user
     * @return a Json String that includes either the newly registered user or all validation errors
     */
    public String register(final String username, final String email, final String password) {
        User user = new User(username, email, password);
        // validate the credentials
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        if (constraintViolations.size() > 0) {
            // collect all violations and put them into a json object
            JSONObject errorJson = new JSONObject();
            constraintViolations.forEach(violation -> {
                /* getPropertyPath() is the credential where the error occured and getMessage() the errorcode.
                if a single credential has multiple errors then the value of the name/value pair will be
                a list of all errors. this is why accumulate() was used instead of put() */
                errorJson.accumulate(violation.getPropertyPath().toString(), violation.getMessage());
            });
            String jsonString = errorJson.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }

        // Check if the username is already taken
        TypedQuery<Long> queryUniqueName = em.createNamedQuery("User.checkUniqueName", Long.class).setParameter("username", username);
        long numberOfEntriesName = queryUniqueName.getSingleResult();

        // Check if the email is already taken
        TypedQuery<Long> queryUniqueEmail = em.createNamedQuery("User.checkUniqueEmail", Long.class).setParameter("email", email);
        long numberOfEntriesEmail = queryUniqueEmail.getSingleResult();

        if (numberOfEntriesName != 0 || numberOfEntriesEmail != 0) {
            // the username or email already exists
            JSONObject errorJson = new JSONObject();
            if (numberOfEntriesName != 0) {
                errorJson.put("username", "602");
            }
            if (numberOfEntriesEmail != 0) {
                errorJson.put("email", "602");
            }

            // return the errors as a json
            String jsonString = errorJson.toString();
            System.out.println("Violations: " + jsonString);
            return errorJson.toString();
        } else {
            // send the email confirmation
            Mail confirmationMail = new Mail(email);
            try {
                confirmationMail.sendConfirmationMail();
                // persist the new user
                em.getTransaction().begin();
                em.persist(user);
                em.getTransaction().commit();
                // return user as json
                String jsonString = user.toJson();
                System.out.println(jsonString);
                return jsonString;
            } catch (MessagingException e) {
                // an exception occured while sending the email
                System.out.println(e.toString());

                JSONObject errorJson = new JSONObject();
                errorJson.put("email", "606");
                String jsonString = errorJson.toString();
                System.out.println("Violations: " + jsonString);
                return jsonString;
            }

        }
    }

    public String login(final String username, final String password) {
        TypedQuery<User> queryGetUser = em.createNamedQuery("User.getUser", User.class).setParameter("username", username);
        List<User> resultsGetUser = queryGetUser.getResultList();

        if(resultsGetUser.size() == 0) {
            JSONObject json = new JSONObject();
            json.put("login", "605");
            return json.toString();
        }

        User user = resultsGetUser.get(0);
        if (!user.getPassword().equals(password)) {
            JSONObject json = new JSONObject();
            json.put("login", "wrong");
            return json.toString();
        }

        return user.toJson();
    }

}