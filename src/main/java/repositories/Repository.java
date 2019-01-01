package repositories;

import entities.User;
import org.json.JSONObject;

import javax.validation.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.Set;

/**
 * @author Alexander Burghuber
 */
public class Repository {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private EntityManager em = emf.createEntityManager();
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();

    public String testJPA() {
        return register("test", "email@gmail.com", "jklasdfjlka");
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
        em.getTransaction().begin();
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
            System.out.println("Constraint-Violations: " + jsonString);
            return jsonString;
        }

        // Check if the username is already taken
        TypedQuery<Long> checkUniqueName = em.createNamedQuery("User.checkUniqueName", Long.class).setParameter("username", username);
        long numberOfEntriesName = checkUniqueName.getSingleResult();

        // Check if the email is already taken
        TypedQuery<Long> checkUniqueEmail = em.createNamedQuery("User.checkUniqueEmail", Long.class).setParameter("email", email);
        long numberOfEntriesEmail = checkUniqueEmail.getSingleResult();

        JSONObject errorJson = new JSONObject();
        if (numberOfEntriesName != 0) {
            errorJson.put("username", "taken");
        }
        if (numberOfEntriesEmail != 0) {
            errorJson.put("email", "taken");
        }

        if (numberOfEntriesName != 0 || numberOfEntriesEmail != 0) {
            // the username or email already exists and the errors are returned as a json
            String jsonString = errorJson.toString();
            System.out.println("Unique-Violations: " + jsonString);
            return errorJson.toString();
        } else {
            // everything is correct and the user is returned as a json
            em.persist(user);
            em.getTransaction().commit();
            String jsonString = user.toJson();
            System.out.println(jsonString);
            return jsonString;
        }
    }

}