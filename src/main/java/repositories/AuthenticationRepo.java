package repositories;

import entities.User;
import entities.VerificationToken;
import objects.ErrorGenerator;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;
import mail.MailService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Response;
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
            List<JSONObject> jsonList = new ArrayList<>();

            constraintViolations.forEach(violation -> {
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
            System.out.println("Sending email confirmation.");
            mail.sendConfirmation(user, verificationToken);
            System.out.println("Email confirmation sent.");
        });

        // return user as json
        String jsonString = user.toJson();
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Logs the user in if the username and password is correct
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a Json containing either the user if the login was successful or an error code
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
        // return the user if the login was successful
        System.out.println("Logged in: " + user.getUsername());
        return user.toJson();
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
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}