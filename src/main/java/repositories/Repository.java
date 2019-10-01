package repositories;

import data.entities.Alcohol;
import data.entities.DrinkBO;
import data.entities.UserBO;
import data.entities.VerificationToken;
import data.enums.AlcoholType;
import data.transferobjects.DrinkVO;
import helper.EntityManagerHelper;
import helper.JwtHelper;
import mail.Mail;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Repository {
    private EntityManager em;
    private JwtHelper jwtHelper;
    private Mail mail;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public Repository() {
        em = EntityManagerHelper.getInstance();
        this.jwtHelper = new JwtHelper();
        this.mail = new Mail();
    }

    /**
     * Registers a new user
     *
     * @param username the username of the user
     * @param email    the email of the user
     * @param password the password of the user
     * @return a response containing either a OK, CONFLICT or FORBIDDEN
     */
    public Response register(String username, String email, String password) {
        if (validateUser(username, email, password)) {
            long countUsername = em.createQuery("SELECT COUNT(u) FROM UserBO u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();

            long countEmail = em.createQuery("SELECT COUNT(u) FROM UserBO u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            if (countUsername == 0 && countEmail == 0) {
                UserBO user = new UserBO(username, email, password);

                VerificationToken verificationToken = new VerificationToken(user, false);
                System.out.println("Setup token: " + verificationToken.getToken()
                        + " Expire: " + verificationToken.getExpiryDate());

                em.getTransaction().begin();
                em.persist(user);
                em.persist(verificationToken);
                em.getTransaction().commit();

                /*
                // multithreaded email sending
                executor.execute(() -> {
                    System.out.println("Sending email confirmation...");
                    mail.sendConfirmation(user, verificationToken);
                    System.out.println("Email confirmation sent.");
                });
                */
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }

        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    /**
     * Logs an user in
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return a response containing either a OK (with the user) or UNAUTHORIZED
     */
    public Response login(String username, String password) {
        // check if the username exists in the database
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
                    JSONObject json = new JSONObject();
                    json.put("token", jwt);
                    return Response.ok(json.toString()).build();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /**
     * Verifies an user using the token that was send with the url inside the verification email.
     *
     * @param emailToken the verification token
     * @return a boolean that indicates if the verification was successful or not
     */
    public boolean verify(final String emailToken) {
        // check if the token exists
        List<VerificationToken> tokenList
                = em.createQuery("SELECT v FROM VerificationToken v WHERE v.token = :token", VerificationToken.class)
                .setParameter("token", emailToken)
                .getResultList();
        if (tokenList.size() != 0) {
            VerificationToken verifyToken = tokenList.get(0);

            Date currentDate = new Date();
            Date tokenDate = verifyToken.getExpiryDate();

            if (tokenDate.compareTo(currentDate) >= 0) {
                UserBO user = verifyToken.getUser();
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

   /* public String getDetails(String jwt) {
        UserBO user = getUserFromJwt(jwt);

        PersonBO person = user.getPerson();

        JSONObject json = new JSONObject();
        if (person == null) {
            json.put("person", JSONObject.NULL);
        } else {
            json.put("person", person.toJson());
        }
        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
    }*/

    /**
     * Inserts the details of an user
     *
     * @param jwt       the json web token
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @param gender    the gender of the user
     * @param birthday  the birthday of the user
     * @param height    the height of the user
     * @param weight    the weight of the user
     * @return a response containing either a OK (with the user) or FORBIDDEN
     */
    public Response insertDetails(String jwt, String firstName, String lastName,
                                  String gender, Date birthday, double height, double weight) {
        gender = gender.toUpperCase();
        if (validateUserDetails(firstName, lastName, gender, height, weight)) {
            UserBO user = getUserFromJwt(jwt);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGender(gender);
            user.setBirthday(birthday);
            user.setHeight(height);
            user.setWeight(weight);

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return Response.ok(user.toJson().toString()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Updates the details of an user
     *
     * @param jwt       the json web token
     * @param password  the new password of the user
     * @param firstName the new firstName of the user
     * @param lastName  the new lastName of the user
     * @param gender    the new gender of the user
     * @param birthday  the new birthday of the user
     * @param height    the new height of the user
     * @param weight    the new weight of the user
     * @return a response containing either a OK (with the updated user) or FORBIDDEN
     */
    public Response updateDetails(String jwt, String username, String password, String firstName,
                                  String lastName, String gender, Date birthday,
                                  double height, double weight) {
        if (validateUserDetails(firstName, lastName, gender, height, weight)) {
            UserBO user = getUserFromJwt(jwt);
            if (username != null) {
                if (!validateUsername(username)) return Response.status(Response.Status.FORBIDDEN).build();
                user.setUsername(username);
            }
            if (password != null) {
                if (!validatePassword(password)) return Response.status(Response.Status.FORBIDDEN).build();
                user.setPassword(password);
            }
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGender(gender);
            user.setBirthday(birthday);
            user.setHeight(height);
            user.setWeight(weight);

            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return Response.ok(user.toJson().toString()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * @param typeStr the alcohol type
     * @return a response containing either a OK and the alcohols or a NOT_FOUND if the type doesn't exists
     */
    public Response getAlcohols(String typeStr) {
        if (typeStr.equals("BEER") || typeStr.equals("WINE") || typeStr.equals("LIQUOR") || typeStr.equals("COCKTAIL")) {
            AlcoholType type = AlcoholType.valueOf(typeStr);
            TypedQuery<Alcohol> query = em.createNamedQuery("Alcohol.get-with-type", Alcohol.class)
                    .setParameter("type", type);
            List<Alcohol> alcohols = query.getResultList();

            JSONArray jsonArray = new JSONArray();
            for (Alcohol alcohol : alcohols) {
                JSONObject alcoholJson = new JSONObject()
                        .put("id", alcohol.getId())
                        .put("name", alcohol.getName())
                        .put("percentage", alcohol.getPercentage())
                        .put("amount", alcohol.getAmount());
                jsonArray.put(alcoholJson);
            }
            return Response.ok(jsonArray.toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * adds a drink to a user
     *
     * @param jwt     the json web token
     * @param drinkVO the drink value object
     * @return a response containing either a OK, NOT_FOUND or UNAUTHORIZED
     */
    public Response addDrink(String jwt, DrinkVO drinkVO) {
        UserBO user = getUserFromJwt(jwt);
        Alcohol alcohol = em.find(Alcohol.class, drinkVO.getAlcoholId());
        if (alcohol != null) {
            DrinkBO drink = new DrinkBO(
                    user, alcohol, drinkVO.getDrankDate(),
                    drinkVO.getLongitude(), drinkVO.getLatitude()
            );
            em.getTransaction().begin();
            em.persist(drink);
            em.getTransaction().commit();
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Loads all alcohol from the json files into the database
     */
    public void loadAlcohol() {
        String folder = "src/main/resources/alcohol/";
        Map<AlcoholType, String> alcohols = new HashMap<>();
        alcohols.put(AlcoholType.BEER, "beers.json");
        alcohols.put(AlcoholType.WINE, "wine.json");
        alcohols.put(AlcoholType.LIQUOR, "liquor.json");
        alcohols.put(AlcoholType.COCKTAIL, "cocktails.json");

        em.getTransaction().begin();
        alcohols.forEach((type, file) -> {
            try {
                InputStream inputStream = Files.newInputStream(Paths.get(folder + file));
                JSONArray jsonArray = new JSONArray(new JSONTokener(inputStream));
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    Alcohol alcohol = new Alcohol(
                            type,
                            json.getString("name"),
                            json.getFloat("percentage"),
                            json.getInt("amount")
                    );
                    em.persist(alcohol);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        em.getTransaction().commit();
    }


    /**
     * Request a change on the password that is linked to the email
     *
     * @param email the verified email of an user
     * @return a status code (OK, Conflict)
     */
    public Response requestPasswordChange(String email) {
        TypedQuery<UserBO> queryGetUser = em.createNamedQuery("User.get-with-email", UserBO.class)
                .setParameter("email", email);
        List<UserBO> resultsGetUser = queryGetUser.getResultList();

        if (resultsGetUser.size() == 0) {
            System.out.println("Request Password Change: No User found");
            return Response.status(Response.Status.CONFLICT).build();
        }
        UserBO user = resultsGetUser.get(0);
        if (!user.isEnabled()) return Response.status(Response.Status.UNAUTHORIZED).build();

        VerificationToken verificationToken = new VerificationToken(user, true);

        em.getTransaction().begin();
        em.persist(verificationToken);
        em.getTransaction().commit();

        int pin = Integer.parseInt(verificationToken.getToken());

        executor.execute(() -> {
            System.out.println("Sending email for password change...");
            mail.resetPasswordConfirmation(resultsGetUser.get(0), pin);
            System.out.println("Email sent.");
        });

        System.out.println("Request Password Change: Success");
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Updates the password of an user
     *
     * @param pin      the pin from the password change email
     * @param password the new password of the user
     * @return a status code (OK, Conflict, Not_found)
     */
    public Response updatePassword(int pin, String password) {
        String token = (new Integer(pin)).toString();
        TypedQuery<VerificationToken> queryGetToken = em.createNamedQuery("Token.get-by-token", VerificationToken.class)
                .setParameter("token", token);
        List<VerificationToken> resultsGetToken = queryGetToken.getResultList();
        System.out.println(resultsGetToken);
        if (resultsGetToken.size() == 0) return Response.status(Response.Status.NOT_FOUND).build();

        VerificationToken verificationToken = resultsGetToken.get(0);

        Date currentDate = new Date();
        Date tokenDate = verificationToken.getExpiryDate();

        if (tokenDate.compareTo(currentDate) <= 0) {
            em.getTransaction().begin();
            em.remove(verificationToken);
            em.getTransaction().commit();
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (!validatePassword(password)) return Response.status(Response.Status.FORBIDDEN).build();

        UserBO user = verificationToken.getUser();
        user.setPassword(password);
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        return Response.status(Response.Status.OK).build();
    }

    /**
     * @param jwt the json web token
     * @return a response containing either a OK and the drinks or a UNAUTHORIZED
     */
    public Response getDrinks(String jwt) {
        UserBO user = getUserFromJwt(jwt);
        em.refresh(user);
        JSONArray jsonArray = new JSONArray();
        for (DrinkBO drink : user.getDrinks()) {
            JSONObject drinkJson = new JSONObject()
                    .put("alcoholId", drink.getAlcohol().getId())
                    .put("drankDate", drink.getDrankDate())
                    .put("longitude", drink.getLongitude())
                    .put("latitude", drink.getLatitude());
            jsonArray.put(drinkJson);
        }
        return Response.ok(jsonArray.toString()).build();
    }

    private UserBO getUserFromJwt(final String jwt) {
        long id = jwtHelper.getUserId(jwt);
        UserBO user = em.find(UserBO.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }

    private boolean validateUser(String username, String email, String password) {
        if (validateUsername(username)) {
            if (email.length() >= 6 && email.length() <= 100
                    && email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)" +
                    "|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)" +
                    "+[a-zA-Z]{2,}))$")) {
                return validatePassword(password);
            }
        }
        return false;
    }

    private boolean validateUserDetails(String firstName, String lastName,
                                        String gender, double height, double weight) {
        if (firstName.length() <= 100 && lastName.length() <= 100) {
            if (gender.equals("M") || gender.equals("F")) {
                if (height >= 150.0 && height <= 230.0) {
                    return (weight >= 30 && weight <= 200);
                }
            }
        }
        return false;
    }

    private boolean validateUsername(String username) {
        return (username.length() >= 4 && username.length() <= 25);
    }

    private boolean validatePassword(String password) {
        return (password.length() >= 8 && password.length() <= 25
                && password.matches("^.*(?=.{8,})(?=.*\\d)((?=.*[a-z]))((?=.*[A-Z])).*$"));
    }
}