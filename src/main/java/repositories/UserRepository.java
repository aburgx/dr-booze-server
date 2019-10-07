package repositories;

import data.entities.User;
import data.entities.VerificationToken;
import helper.EntityManagerHelper;
import helper.JwtHelper;
import mail.Mail;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UserRepository {
    private EntityManager em = EntityManagerHelper.getInstance();
    private JwtHelper jwtHelper = new JwtHelper();
    private Mail mail = new Mail();
    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private static Logger LOG = Logger.getLogger(UserRepository.class.getName());

    /**
     * Registers a new user.
     *
     * @param username the username of the user
     * @param email    the email of the user
     * @param password the password of the user
     * @return a response containing OK, CONFLICT or FORBIDDEN
     */
    public Response register(String username, String email, String password) {
        if (validateUsername(username) && validateEmail(email) && validatePassword(password)) {
            // check if the username or email already exists
            long countUsername = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            long countEmail = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            if (countUsername == 0 && countEmail == 0) {
                User user = new User(username, email, password);

                // generate a token for the email verification
                VerificationToken verificationToken = new VerificationToken(user, false);

                em.getTransaction().begin();
                em.persist(user);
                em.persist(verificationToken);
                em.getTransaction().commit();

                // async email sending
                executor.execute(() -> mail.sendConfirmation(user, verificationToken));

                LOG.info("Registered new user: " + user.getId() + ", " + user.getUsername());
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }

        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    /**
     * Logs an user in.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a response containing OK (with the jwt) or UNAUTHORIZED
     */
    public Response login(String username, String password) {
        // check if an user with this username exists
        List<User> results = em.createNamedQuery("User.get-with-username", User.class)
                .setParameter("username", username)
                .getResultList();
        if (results.size() != 0) {
            User user = results.get(0);
            // check if the password is correct
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(Hex.decode(user.getSalt()));
                byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

                if (new String(Hex.encode(hash)).equals(user.getPassword())) {
                    String jwt = jwtHelper.create(user.getId());
                    JSONObject json = new JSONObject()
                            .put("token", jwt);
                    LOG.info("Logged in user: " + user.getId() + ", " + user.getUsername());
                    return Response.ok(json.toString()).build();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /**
     * Verifies the email of the user using the token that was send with the url inside the verification email.
     *
     * @param token the verification token
     * @return true if the verification was successful, false if not
     */
    public boolean verify(String token) {
        // check if the token exists
        List<VerificationToken> tokens
                = em.createQuery("SELECT v FROM VerificationToken v WHERE v.token = :token", VerificationToken.class)
                .setParameter("token", token)
                .getResultList();
        if (tokens.size() != 0) {
            // verify the user and delete the verification token
            VerificationToken verifyToken = tokens.get(0);
            User user = verifyToken.getUser();
            em.getTransaction().begin();
            user.setEnabled(true);
            em.remove(verifyToken);
            em.getTransaction().commit();

            LOG.info("Verified user: " + user.getId() + ", " + user.getUsername());
            return true;
        }
        return false;
    }

    /**
     * Returns the user.
     *
     * @param jwt the json web token
     * @return a response containing OK (with the user) or CONFLICT
     */
    public Response getUser(String jwt) {
        User user = getUserFromJwt(jwt);
        if (user.isDetailsSet()) {
            return Response.ok(user.toJson().toString()).build();
        }
        return Response.status(Response.Status.CONFLICT).build();
    }

    /**
     * Sets the details of an user.
     * The firstName and the lastName are allowed to be null.
     *
     * @param jwt       the json web token
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @param gender    the gender of the user
     * @param birthday  the birthday of the user
     * @param height    the height of the user
     * @param weight    the weight of the user
     * @return a response containing OK (with the user) or FORBIDDEN
     */
    public Response setDetails(String jwt, String firstName, String lastName,
                               String gender, long birthday, int height, int weight) {
        if ((firstName == null && lastName == null) || validateName(firstName, lastName)) {
            if (validateHeightWeight(height, weight)) {
                if (validateGender(gender)) {
                    User user = getUserFromJwt(jwt);
                    em.getTransaction().begin();
                    if (firstName != null) {
                        // if firstName is not null then lastName is also not null because of the previous if
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                    }
                    user.setGender(gender.toUpperCase());
                    user.setBirthday(new Date(birthday));
                    user.setHeight(height);
                    user.setWeight(weight);
                    user.setDetailsSet(true);
                    em.getTransaction().commit();

                    LOG.info("Set details of user: " + user.getId() + ", " + user.getUsername());
                    return Response.ok(user.toJson().toString()).build();
                }
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Request the change of the password.
     *
     * @param email the email of an user
     * @return a response containing OK, NOT_FOUND or FORBIDDEN
     */
    public Response requestPasswordChange(String email) {
        List<User> results = em.createNamedQuery("User.get-with-email", User.class)
                .setParameter("email", email)
                .getResultList();

        if (results.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User user = results.get(0);
        if (!user.isEnabled()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        VerificationToken verificationToken = new VerificationToken(user, true);

        em.getTransaction().begin();
        em.persist(verificationToken);
        em.getTransaction().commit();

        int pin = Integer.parseInt(verificationToken.getToken());

        // async email sending
        executor.execute(() -> mail.resetPasswordConfirmation(user, pin));

        LOG.info("Requested password change of user: " + user.getId() + ", " + user.getUsername());
        return Response.ok().build();
    }

    /**
     * Changes the password of an user.
     *
     * @param pin      the pin from the request-password-change email
     * @param password the new password of the user
     * @return a response containing OK, NOT_FOUND, GONE or FORBIDDEN
     */
    public Response changePassword(int pin, String password) {
        //check if the token exists
        List<VerificationToken> results = em.createNamedQuery("Token.get-by-token", VerificationToken.class)
                .setParameter("token", String.valueOf(pin))
                .getResultList();
        if (results.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        VerificationToken verificationToken = results.get(0);

        // check if the token has expired
        Date currentDate = new Date();
        Date tokenDate = verificationToken.getExpiryDate();
        if (tokenDate.compareTo(currentDate) <= 0) {
            em.getTransaction().begin();
            em.remove(verificationToken);
            em.getTransaction().commit();
            return Response.status(Response.Status.GONE).build();
        }

        // check if the new password is valid
        if (!validatePassword(password)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // set the new password
        User user = verificationToken.getUser();
        em.getTransaction().begin();
        user.setPassword(password);
        em.getTransaction().commit();

        LOG.info("Changed password of user: " + user.getId() + ", " + user.getUsername());
        return Response.ok().build();
    }

    private User getUserFromJwt(String jwt) {
        long id = jwtHelper.getUserId(jwt);
        User user = em.find(User.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }

    private boolean validateUsername(String username) {
        if (username != null) {
            return (username.length() >= 4 && username.length() <= 25);
        }
        return false;
    }

    private boolean validatePassword(String password) {
        if (password != null) {
            return (password.length() >= 8 && password.length() <= 25
                    && password.matches("^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$"));
        }
        return false;
    }

    private boolean validateEmail(String email) {
        if (email != null) {
            return (email.length() >= 6 && email.length() <= 100
                    && email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)" +
                    "|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)" +
                    "+[a-zA-Z]{2,}))$"));
        }
        return false;
    }

    private boolean validateName(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return firstName.length() <= 100 && lastName.length() <= 100;
        }
        return false;
    }

    private boolean validateGender(String gender) {
        if (gender != null) {
            gender = gender.toUpperCase();
            return gender.equals("M") || gender.equals("F");
        }
        return false;
    }

    private boolean validateHeightWeight(double height, double weight) {
        if (height >= 150.0 && height <= 230.0) {
            return (weight >= 30 && weight <= 200);
        }
        return false;
    }
}
