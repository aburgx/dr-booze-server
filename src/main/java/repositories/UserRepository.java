package repositories;

import data.entities.UserBO;
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

public class UserRepository {
    private EntityManager em = EntityManagerHelper.getInstance();
    private JwtHelper jwtHelper = new JwtHelper();
    private Mail mail = new Mail();
    private ExecutorService executor = Executors.newFixedThreadPool(10);

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
            long countUsername = em.createQuery("SELECT COUNT(u) FROM UserBO u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            long countEmail = em.createQuery("SELECT COUNT(u) FROM UserBO u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            if (countUsername == 0 && countEmail == 0) {
                UserBO user = new UserBO(username, email, password);

                // generate a token for the email verification
                VerificationToken verificationToken = new VerificationToken(user, false);

                em.getTransaction().begin();
                em.persist(user);
                em.persist(verificationToken);
                em.getTransaction().commit();

                // async email sending
                executor.execute(() -> {
                    System.out.println("Sending email confirmation...");
                    mail.sendConfirmation(user, verificationToken);
                    System.out.println("Email confirmation sent.");
                });

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
     * @return a response containing OK (with the user and token) or UNAUTHORIZED
     */
    public Response login(String username, String password) {
        // check if an user with this username exists
        List<UserBO> results = em.createNamedQuery("User.get-with-username", UserBO.class)
                .setParameter("username", username)
                .getResultList();
        if (results.size() != 0) {
            UserBO user = results.get(0);
            // check if the password is correct
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(Hex.decode(user.getSalt()));
                byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

                if (new String(Hex.encode(hash)).equals(user.getPassword())) {
                    String jwt = jwtHelper.create(user.getId());
                    JSONObject json = new JSONObject()
                            .put("user", user.toJson())
                            .put("token", jwt);
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
            em.getTransaction().begin();
            verifyToken.getUser().setEnabled(true);
            em.remove(verifyToken);
            em.getTransaction().commit();
            return true;
        }
        return false;
    }

    /**
     * Inserts the details of an user.
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
    public Response insertDetails(String jwt, String firstName, String lastName,
                                  String gender, Date birthday, int height, int weight) {
        gender = gender.toUpperCase();
        if (validateUserDetails(firstName, lastName, gender, height, weight)) {
            UserBO user = getUserFromJwt(jwt);

            em.getTransaction().begin();

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGender(gender);
            user.setBirthday(birthday);
            user.setHeight(height);
            user.setWeight(weight);

            em.getTransaction().commit();

            return Response.ok(user.toJson().toString()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Returns the user.
     *
     * @param jwt the json web token
     * @return a response containing OK (with the user)
     */
    public Response getUser(String jwt) {
        UserBO user = getUserFromJwt(jwt);
        return Response.ok(user.toJson().toString()).build();
    }

    /**
     * Updates the details of an user.
     *
     * @param jwt       the json web token
     * @param password  the new password of the user
     * @param firstName the new firstName of the user
     * @param lastName  the new lastName of the user
     * @param gender    the new gender of the user
     * @param birthday  the new birthday of the user
     * @param height    the new height of the user
     * @param weight    the new weight of the user
     * @return a response containing OK (with the updated user) or FORBIDDEN
     */
    public Response updateDetails(String jwt, String username, String password, String firstName,
                                  String lastName, String gender, Date birthday,
                                  int height, int weight) {
        if (validateUserDetails(firstName, lastName, gender, height, weight)) {
            if (password == null || validatePassword(password)) {
                if (validateUsername(username)) {
                    UserBO user = getUserFromJwt(jwt);

                    em.getTransaction().begin();
                    user.setUsername(username);
                    if (password != null) {
                        user.setPassword(password);
                    }
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setGender(gender);
                    user.setBirthday(birthday);
                    user.setHeight(height);
                    user.setWeight(weight);

                    em.getTransaction().commit();

                    return Response.ok(user.toJson().toString()).build();
                }
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    // TODO: Update Responses
    /**
     * Request the change of the password
     *
     * @param email the email of an user
     * @return a response containing OK, NOT_FOUND or FORBIDDEN
     */
    public Response requestPasswordChange(String email) {
        List<UserBO> results = em.createNamedQuery("User.get-with-email", UserBO.class)
                .setParameter("email", email)
                .getResultList();

        if (results.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        UserBO user = results.get(0);
        if (!user.isEnabled()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        VerificationToken verificationToken = new VerificationToken(user, true);

        em.getTransaction().begin();
        em.persist(verificationToken);
        em.getTransaction().commit();

        int pin = Integer.parseInt(verificationToken.getToken());

        // async email sending
        executor.execute(() -> {
            System.out.println("Sending email for password change...");
            mail.resetPasswordConfirmation(user, pin);
            System.out.println("Email sent.");
        });

        return Response.ok().build();
    }

    /**
     * Updates the password of an user.
     *
     * @param pin      the pin from the request-password-change email
     * @param password the new password of the user
     * @return a response containing OK, NOT_FOUND, GONE or FORBIDDEN
     */
    public Response updatePassword(int pin, String password) {
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
        UserBO user = verificationToken.getUser();
        em.getTransaction().begin();
        user.setPassword(password);
        em.getTransaction().commit();

        return Response.ok().build();
    }

    private UserBO getUserFromJwt(String jwt) {
        long id = jwtHelper.getUserId(jwt);
        UserBO user = em.find(UserBO.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }

    private boolean validateUsername(String username) {
        return (username.length() >= 4 && username.length() <= 25);
    }

    private boolean validatePassword(String password) {
        return (password.length() >= 8 && password.length() <= 25
                && password.matches("^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$"));
    }

    private boolean validateEmail(String email) {
        return (email.length() >= 6 && email.length() <= 100
                && email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)" +
                "|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)" +
                "+[a-zA-Z]{2,}))$"));
    }

    private boolean validateUserDetails(String firstName, String lastName, String gender, double height, double weight) {
        if (firstName.length() <= 100 && lastName.length() <= 100) {
            if (gender.equals("M") || gender.equals("F")) {
                if (height >= 150.0 && height <= 230.0) {
                    return (weight >= 30 && weight <= 200);
                }
            }
        }
        return false;
    }
}