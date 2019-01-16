package repositories;

import entities.User;
import entities.VerificationToken;
import objects.ErrorGenerator;
import org.bouncycastle.util.encoders.Hex;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Burghuber
 */
public class AuthenticationRepo {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private EntityManager em = emf.createEntityManager();
    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private Validator validator = factory.getValidator();
    private MailService mailService = new MailService();
    private ErrorGenerator errorgen = new ErrorGenerator();

    private static AuthenticationRepo instance = null;

    private AuthenticationRepo() {
    }

    public static AuthenticationRepo getInstance() {
        if (instance == null)
            instance = new AuthenticationRepo();
        return instance;
    }

    public String test() {
        return "Does nothing.";
    }

    /**
     * Registers a new user and validates his credentials
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

        TypedQuery<Long> queryUniqueName = em.createNamedQuery("User.checkUniqueName", Long.class).setParameter("username", username);
        long numberOfEntriesName = queryUniqueName.getSingleResult();

        TypedQuery<Long> queryUniqueEmail = em.createNamedQuery("User.checkUniqueEmail", Long.class).setParameter("email", email);
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

        try {
            // send the email confirmation
            mailService.send(user, verificationToken);

            // return user as json
            String jsonString = user.toJson();
            System.out.println(jsonString);
            return jsonString;
        } catch (MessagingException ex) {
            // an exception occured while sending the email
            System.out.println(ex.toString());
            return errorgen.generate(606, "email");
        }

    }

    /***
     * Verifies an user using the token that was send with the url inside the verification email.
     * @param token the verification token
     * @return a boolean that indicates if the verification was successful or not
     */
    public boolean verify(final String token) {
        // check if the token exists
        List<VerificationToken> tokenList
                = em.createQuery("SELECT v FROM Booze_VerificationToken v WHERE v.token = :token", VerificationToken.class)
                .setParameter("token", token)
                .getResultList();
        if (tokenList.size() != 0) {
            VerificationToken verificationToken = tokenList.get(0);
            User user = verificationToken.getUser();
            // set the user enabled and delete the token
            em.getTransaction().begin();
            user.setEnabled(true);
            em.remove(verificationToken);
            em.getTransaction().commit();
            return true;
        } else {
            return false;
        }
    }

    /***
     * Logs the user in if the username and password is correct
     * @param username the username of the user
     * @param password the password of the user
     * @return a Json containing either the user if the login was successful or an error code
     */
    public String login(final String username, final String password) {
        // check if the username exists in the database
        TypedQuery<User> queryGetUser = em.createNamedQuery("User.getUser", User.class).setParameter("username", username);
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

}