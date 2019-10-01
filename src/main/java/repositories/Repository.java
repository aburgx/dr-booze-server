package repositories;

import entities.*;
import enums.AlcoholType;
import enums.ChallengeType;
import helper.EntityManagerFactoryHelper;
import helper.JwtHelper;
import helper.ValidatorHelper;
import mail.Mail;
import objects.ErrorGenerator;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import transferObjects.DrinkVO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@SuppressWarnings("Duplicates")
public class Repository {

    private EntityManager em;
    private ErrorGenerator errorgen;
    private ValidatorHelper validator;
    private JwtHelper jwtHelper;
    private Mail mail;
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    private static Repository instance = null;

    private Repository() {
        EntityManagerFactory emf = EntityManagerFactoryHelper.getFactory();
        this.em = emf.createEntityManager();
        this.validator = new ValidatorHelper();
        this.errorgen = new ErrorGenerator();
        this.jwtHelper = new JwtHelper();
        this.mail = new Mail();
    }

    public static Repository getInstance() {
        if (instance == null)
            instance = new Repository();
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
        UserBO user = new UserBO(username, email, password);

        // validate the user
        String resultUser = validator.validateUser(user);
        if (resultUser != null)
            return resultUser;

        TypedQuery<Long> queryUniqueName = em.createNamedQuery("User.count-username", Long.class)
                .setParameter("username", username);
        long numberOfEntriesName = queryUniqueName.getSingleResult();

        TypedQuery<Long> queryUniqueEmail = em.createNamedQuery("User.count-email", Long.class)
                .setParameter("email", email);
        long numberOfEntriesEmail = queryUniqueEmail.getSingleResult();

        // check if the username or the email is already taken
        if (numberOfEntriesName != 0) {
            return errorgen.generate(602, "username");
        }
        if (numberOfEntriesEmail != 0) {
            return errorgen.generate(602, "email");
        }

        // setup the verification token of the user
        VerificationToken verificationToken = new VerificationToken(user, false);
        System.out.println("Setup token: " + verificationToken.getToken()
                + " Expire: " + verificationToken.getExpiryDate());

        // persist the new user
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

        // return user as json
        JSONObject json = new JSONObject();
        json.put("user", user.toJson());
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
        TypedQuery<UserBO> queryGetUser = em.createNamedQuery("User.get-with-username", UserBO.class)
                .setParameter("username", username);
        List<UserBO> resultsGetUser = queryGetUser.getResultList();

        if (resultsGetUser.size() == 0) {
            return errorgen.generate(605, "login");
        }

        // check if the password is correct
        UserBO user = resultsGetUser.get(0);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Hex.decode(user.getSalt()));
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

            if (!new String(Hex.encode(hash)).equals(user.getPassword())) {
                return errorgen.generate(605, "login");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String jwtToken = jwtHelper.create(user.getUsername());
        JSONObject json = new JSONObject();
        json.put("token", jwtToken);

        System.out.println("Logged in: " + user.getUsername() + " with token: " + jwtToken);
        return json.toString();
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

    /**
     * @param jwt the json web token
     * @return a json string that includes the person
     */
    public String getPerson(final String jwt) {
        UserBO user = getUserFromJwt(jwt);
        if (user == null)
            return errorgen.generate(607, "user");

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
    }

    /**
     * Inserts the details of an user as a person object
     *
     * @param jwt       the json web token
     * @param firstName the first name of the user
     * @param lastName  the last name of the user
     * @param gender    the gender of the user
     * @param birthday  the birthday of the user
     * @param height    the height of the user
     * @param weight    the weight of the user
     * @return a json String that includes either the user or all validation errors
     */
    public String insertDetails(final String jwt, final String firstName, final String lastName, final String gender,
                                final Date birthday, final double height, final double weight) {
        // check if the gender, height and weight is incorrect
        if (!gender.equals("m") && !gender.equals("f")) {
            return errorgen.generate(604, "gender");
        } else if (height < 150.0 || height > 230.0) {
            return errorgen.generate(604, "height");
        } else if (weight < 30 || weight > 200) {
            return errorgen.generate(604, "weight");
        }

        UserBO user = getUserFromJwt(jwt);
        if (user == null)
            return errorgen.generate(607, "user");

        PersonBO person = new PersonBO(user, firstName, lastName, gender, birthday, height, weight);
        user.setPerson(person);

        // validate the person
        String resultPerson = validator.validatePerson(person);
        if (resultPerson != null)
            return resultPerson;

        System.out.println(person.toString());
        System.out.println(person.getUser().toString());

        // persist the updated user & person
        em.getTransaction().begin();
        em.persist(person);
        em.getTransaction().commit();

        // return the user and person
        JSONObject json = new JSONObject();
        json.put("person", person.toJson());
        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
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
     * @return a json String that includes either the user or all validation errors
     */
    public String updateDetails(final String jwt, final String password, final String firstName,
                                final String lastName, final String gender, final Date birthday,
                                final double height, final double weight) {

        System.out.println("jwt: " + jwt + " password: " + password + " firstName: " + firstName
                + " lastName: " + lastName + " gender: " + gender + " birthday: " + birthday
                + " height: " + height + " weight: " + weight);

        UserBO user = getUserFromJwt(jwt);
        if (user == null)
            return errorgen.generate(607, "user");

        PersonBO person = user.getPerson();
        if (person == null)
            return errorgen.generate(607, "person");

        // set the new value if the value is not null
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
        String resultUser = validator.validateUser(user);
        if (resultUser != null)
            return resultUser;

        // validate the person
        String resultPerson = validator.validatePerson(person);
        if (resultPerson != null)
            return resultPerson;

        System.out.println(person.toJson().toString());

        // persist the updated user & person
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        // return the user and person
        JSONObject json = new JSONObject();
        json.put("person", person.toJson());
        String jsonString = json.toString();
        System.out.println(jsonString);
        return jsonString;
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
        if (user != null) {
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
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
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

    private UserBO getUserFromJwt(final String jwt) {
        String username = jwtHelper.checkSubject(jwt);

        TypedQuery<UserBO> queryGetUser = em.createNamedQuery("User.get-with-username", UserBO.class)
                .setParameter("username", username);
        List<UserBO> resultsGetUser = queryGetUser.getResultList();

        // check if user exists
        if (resultsGetUser.size() == 0) {
            return null;
        }
        return resultsGetUser.get(0);
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

        UserBO user = verificationToken.getUser();
        user.setPassword(password);

        if (validator.validateUser(user) != null) return Response.status(Response.Status.CONFLICT).build();

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
        if (user != null) {
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
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Loads every template from the json file into the database
     *
     * @throws IOException while file reading
     */
    public void loadTemplates() throws IOException {

        String jsonFolder = "src/main/resources/challenges/";

        // Read beer.json
        InputStream inputStream = Files.newInputStream(Paths.get(jsonFolder + "challenges.json"));
        JSONArray jsonArray = new JSONArray(new JSONTokener(inputStream));

        jsonArray.forEach(item -> {
            //Parse Object to JSONObject
            JSONObject jsonObject = (JSONObject) item;

            //Generate Template from JSONObject
            Template template = new Template(
                    jsonObject.getLong("id"),
                    jsonObject.getString("content"),
                    jsonObject.getInt("amount"),
                    jsonObject.getEnum(ChallengeType.class, "type")
            );

            em.getTransaction().begin();
            em.persist(template);
            em.getTransaction().commit();
        });
    }

    /**
     * <b>Manage all user challenges</b>
     * when the challenges are fulfilled then generate new challenges
     * else send the saved challenges
     *
     * @param jwt the json web token
     * @return challenges
     */
    public String challengeManager(final String jwt) {
        System.out.println("Manager reached");
        UserBO user = getUserFromJwt(jwt);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        // checks if user is null and breaks
        assert user != null;
        //Generate Challenges
        if (user.getChallenges().size() == 0) {
            generateChallenges(user);
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        }
        // checks if challenges are due and if so check if they are fulfilled
        System.out.println(user.getChallenges().get(0).getDate());
        System.out.println(cal.getTime());
        if (user.getChallenges().get(0).getDate().before(cal.getTime())) {
            System.out.println("Check challenges");
            checkChallenges(user, cal.getTime());
        }
        JSONArray challenges = new JSONArray();
        for (ChallengeBO challengeBO : user.getChallenges()) {
            challenges.put(challengeBO.toJson());
        }
        return challenges.toString();
    }

    /**
     * generate 3 challenges based on the users drinking history
     *
     * @param user user object of current user
     */
    private void generateChallenges(UserBO user) {
        // get 1 challenge at a time
        for (int i = 0; i < 3; i++) {
            Template template;
            boolean repeat;
            do {
                repeat = false;
                int rand = new Random().nextInt(5) + 1; //Start with 1 ends with 5
                if (rand == 4) rand = 5;// Challenge 4 not implemented yet TODO: remove
                template = em.createNamedQuery("Template.getRandomTemplate", Template.class)
                        .setParameter("id", rand).getSingleResult();
                for (ChallengeBO challengeBO : user.getChallenges()) {
                    if (challengeBO.getTemplate().getId() == rand) {
                        repeat = true;
                    }
                }
            } while (repeat);
            user.getChallenges().add(setParameter(template, user));
        }
    }

    /**
     * receive custom parameters for a challenge
     *
     * @param t template of the challenge to set parameters
     * @param u user object to generate personalised challenges
     * @return a new Challenge
     */
    private ChallengeBO setParameter(Template t, UserBO u) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        List<DrinkBO> drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", DrinkBO.class)
                .setParameter("id", u.getId())
                .setParameter("start", cal.getTime())
                .getResultList(); // drinks that the user drank in a week
        ChallengeBO challenge = new ChallengeBO();
        challenge.setTemplate(t);
        switch (t.getType()) {
            case MAXWEEK:// check if user drank no more than x drinks in a week
                challenge.getParameter().add(drinks.size() - 2 <= 0 ? 1 : drinks.size() - 2);
                break;
            case MAXDAY:// check if user drank more than x drinks per day
                challenge.getParameter().add(drinks.size() / 7 - 2 <= 0 ? 1 : drinks.size() / 7 - 2);
                break;
            case MAXDAYS:// check if a user drank on y days x drinks
                challenge.getParameter().add(drinks.size() / 7 - 2 <= 0 ? 1 : drinks.size() / 7 - 2);
                challenge.getParameter().add(new Random().nextInt(5) + 1);// random amount of days between 1 and 5 days
                break;
            case MAXPERCENTAGE:// check if a user haven't had more than x ‰ (per mille)
                break;
            case MAXGAG:// always true
                // Doesn't require any Parameter
                break;
        }
        return challenge;
    }

    /**
     * check the challenges on complete if the user fulfilled them
     *
     * @param user user object to check his challenges
     * @param d    current date
     */
    private void checkChallenges(UserBO user, Date d) {
        user.getChallenges().forEach(challenge -> {
            Template template = challenge.getTemplate();
            Integer maxAllowed;
            List<DrinkBO> drinks;
            boolean failed = false;
            Calendar cal = Calendar.getInstance();
            switch (template.getType()) {
                case MAXWEEK: // check if user drank no more than x drinks in a week
                    maxAllowed = challenge.getParameter().get(0);
                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", DrinkBO.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();
                    if (drinks.size() < maxAllowed) {
                        //TODO: implement first check
                        user.setToken(user.getToken() + challenge.getTemplate().getAmount());
                        challenge.setSuccess(true);
                    } else {
                        challenge.setSuccess(false);
                    }
                    break;
                case MAXDAY: // check if user drank more than x drinks per day
                    maxAllowed = challenge.getParameter().get(0);
                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", DrinkBO.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();
                    cal.setTime(d);

                    for (int i = 0; i < 7; i++) {
                        Stream<DrinkBO> drinksOfOneDay = drinks.stream().filter(
                                drinkBO -> drinkBO.getDrankDate().equals(cal.getTime())
                        );
                        if (drinksOfOneDay.count() > maxAllowed) {
                            failed = true;
                        }
                        cal.add(Calendar.DAY_OF_MONTH, i);
                    }
                    if (!failed) {
                        user.setToken(user.getToken() + challenge.getTemplate().getAmount());
                        challenge.setSuccess(true);
                    } else {
                        challenge.setSuccess(false);
                    }
                    break;
                case MAXDAYS:// check if a user drank on y days x drinks
                    int days = challenge.getParameter().get(0);
                    maxAllowed = challenge.getParameter().get(1);
                    int failedCount = 0;

                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", DrinkBO.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d);

                    for (int i = 0; i < 7; i++) {
                        Stream<DrinkBO> drinksOfOneDay = drinks.stream().filter(
                                drinkBO -> drinkBO.getDrankDate().equals(cal1.getTime())
                        );
                        if (drinksOfOneDay.count() < maxAllowed) {
                            failedCount++;
                        }
                        cal1.add(Calendar.DAY_OF_MONTH, i);
                    }
                    if (failedCount < days) {
                        user.setToken(user.getToken() + challenge.getTemplate().getAmount());
                        challenge.setSuccess(true);
                    } else {
                        challenge.setSuccess(false);
                    }
                    break;
                case MAXPERCENTAGE:// check if a user haven't had more than x ‰ (per mille)
                    break;
                case MAXGAG:// always true
                    challenge.setSuccess(true);
                    break;
            }
        });
        user.getChallenges().clear();
        generateChallenges(user);
    }
}