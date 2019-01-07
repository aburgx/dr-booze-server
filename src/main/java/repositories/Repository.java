package repositories;

import entities.User;
import entities.VerificationToken;
import org.json.JSONObject;
import services.MailService;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Burghuber
 */
public class Repository {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private EntityManager em = emf.createEntityManager();
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();
    private MailService mailService = new MailService();

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
                errorJson.accumulate("error_code", violation.getMessage());
                errorJson.accumulate("error_reason", violation.getPropertyPath());
            });
            String jsonString = errorJson.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }

        // check if the username is already taken
        TypedQuery<Long> queryUniqueName = em.createNamedQuery("User.checkUniqueName", Long.class).setParameter("username", username);
        long numberOfEntriesName = queryUniqueName.getSingleResult();

        // check if the email is already taken
        TypedQuery<Long> queryUniqueEmail = em.createNamedQuery("User.checkUniqueEmail", Long.class).setParameter("email", email);
        long numberOfEntriesEmail = queryUniqueEmail.getSingleResult();

        if (numberOfEntriesName != 0 || numberOfEntriesEmail != 0) {
            // the username or email already exists
            JSONObject errorJson = new JSONObject();
            if (numberOfEntriesName != 0) {
                errorJson.put("error_code", "602");
                errorJson.put("error_reason", "username");
            }
            if (numberOfEntriesEmail != 0) {
                errorJson.put("error_code", "602");
                errorJson.put("error_reason", "email");
            }

            // return the errors as a json
            String jsonString = errorJson.toString();
            System.out.println("Violations: " + jsonString);
            return errorJson.toString();
        } else {

            // setup the verification token of the user
            VerificationToken verificationToken = new VerificationToken(user);
            System.out.println("Setup token: " + verificationToken.getToken() + " Expire: " + verificationToken.getExpiryDate());

            // persist the new user
            em.getTransaction().begin();
            em.persist(user);
            em.persist(verificationToken);
            em.getTransaction().commit();

            // send the email confirmation
            try {
                mailService.send(user, verificationToken);
                // return user as json
                String jsonString = user.toJson();
                System.out.println(jsonString);
                return jsonString;
            } catch (MessagingException e) {
                // an exception occured while sending the email
                System.out.println(e.toString());
                JSONObject errorJson = new JSONObject();
                errorJson.put("error_code", "606");
                errorJson.put("error_reason", "email");
                String jsonString = errorJson.toString();
                System.out.println("Violations: " + jsonString);
                return jsonString;
            }

        }
    }

    // TODO: Delete Verificationtoken
    // TODO: optimize namedqueries
    public boolean verify(final String token) {
        // check if a user has the unique token
        TypedQuery<Long> queryToken = em.createNamedQuery("VerificationToken.verify", Long.class).setParameter("token", token);
        long numberOfTokens = queryToken.getSingleResult();

        if (numberOfTokens != 0) {
            // verify and enable the user
            TypedQuery<User> queryGetUser = em.createNamedQuery("VerificationToken.getUser", User.class).setParameter("token", token);
            User user = queryGetUser.getSingleResult();
            em.getTransaction().begin();

            user.setEnabled(true);
            em.getTransaction().commit();
            return true;
        } else {
            return false;
        }
    }

    public String login(final String username, final String password) {
        TypedQuery<User> queryGetUser = em.createNamedQuery("User.getUser", User.class).setParameter("username", username);
        List<User> resultsGetUser = queryGetUser.getResultList();

        if (resultsGetUser.size() == 0) {
            JSONObject json = new JSONObject();
            json.put("error_code", "605");
            json.put("error_reason", "login");
            return json.toString();
        }

        User user = resultsGetUser.get(0);
        if (!user.getPassword().equals(password)) {
            JSONObject json = new JSONObject();
            json.put("error_code", "606");
            return json.toString();
        }

        return user.toJson();
    }

}