package repositories;

import data.entities.Challenge;
import data.entities.ChallengeTemplate;
import data.entities.Drink;
import data.entities.User;
import data.enums.ChallengeType;
import helper.EntityManagerHelper;
import helper.JwtHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ChallengeRepository {

    private EntityManager em = EntityManagerHelper.getInstance();
    private JwtHelper jwtHelper = new JwtHelper();

    /**
     * Loads every template from the json file into the database
     */
    public void loadTemplates() {
        URL url = Thread.currentThread()
                .getContextClassLoader()
                .getResource("challenges/challenges.json");
        if (url != null) {
            try (InputStream inputStream = url.openStream()) {
                JSONArray jsonArray = new JSONArray(new JSONTokener(inputStream));

                jsonArray.forEach(item -> {
                    //Parse Object to JSONObject
                    JSONObject jsonObject = (JSONObject) item;

                    //Generate ChallengeTemplate from JSONObject
                    ChallengeTemplate template = new ChallengeTemplate(
                            jsonObject.getLong("id"),
                            jsonObject.getString("content"),
                            jsonObject.getInt("amount"),
                            jsonObject.getEnum(ChallengeType.class, "type")
                    );

                    em.getTransaction().begin();
                    em.persist(template);
                    em.getTransaction().commit();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Challenge templates not found");
        }
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
        User user = getUserFromJwt(jwt);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        // checks if user is null and breaks
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
        for (Challenge challenge : user.getChallenges()) {
            challenges.put(challenge.toJson());
        }
        return challenges.toString();
    }

    /**
     * generate 3 challenges based on the users drinking history
     *
     * @param user user object of current user
     */
    private void generateChallenges(User user) {
        // get 1 challenge at a time
        for (int i = 0; i < 3; i++) {
            ChallengeTemplate template;
            boolean repeat;
            do {
                repeat = false;
                int rand = new Random().nextInt(5) + 1; //Start with 1 ends with 5
                if (rand == 4) rand = 5;// Challenge 4 not implemented yet TODO: remove
                template = em.createNamedQuery("Template.getRandomTemplate", ChallengeTemplate.class)
                        .setParameter("id", rand).getSingleResult();
                for (Challenge challenge : user.getChallenges()) {
                    if (challenge.getTemplate().getId() == rand) {
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
    private Challenge setParameter(ChallengeTemplate t, User u) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        List<Drink> drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", Drink.class)
                .setParameter("id", u.getId())
                .setParameter("start", cal.getTime())
                .getResultList(); // drinks that the user drank in a week
        Challenge challenge = new Challenge();
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
    private void checkChallenges(User user, Date d) {
        user.getChallenges().forEach(challenge -> {
            ChallengeTemplate template = challenge.getTemplate();
            Integer maxAllowed;
            List<Drink> drinks;
            boolean failed = false;
            Calendar cal = Calendar.getInstance();
            switch (template.getType()) {
                case MAXWEEK: // check if user drank no more than x drinks in a week
                    maxAllowed = challenge.getParameter().get(0);
                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", Drink.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();
                    if (drinks.size() < maxAllowed) {
                        //TODO: implement first check
                        user.setPoints(user.getPoints() + challenge.getTemplate().getAmount());
                        challenge.setSuccess(true);
                    } else {
                        challenge.setSuccess(false);
                    }
                    break;
                case MAXDAY: // check if user drank more than x drinks per day
                    maxAllowed = challenge.getParameter().get(0);
                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", Drink.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();
                    cal.setTime(d);

                    for (int i = 0; i < 7; i++) {
                        Stream<Drink> drinksOfOneDay = drinks.stream().filter(
                                drink -> drink.getDrankDate().equals(cal.getTime())
                        );
                        if (drinksOfOneDay.count() > maxAllowed) {
                            failed = true;
                        }
                        cal.add(Calendar.DAY_OF_MONTH, i);
                    }
                    if (!failed) {
                        user.setPoints(user.getPoints() + challenge.getTemplate().getAmount());
                        challenge.setSuccess(true);
                    } else {
                        challenge.setSuccess(false);
                    }
                    break;
                case MAXDAYS:// check if a user drank on y days x drinks
                    int days = challenge.getParameter().get(0);
                    maxAllowed = challenge.getParameter().get(1);
                    int failedCount = 0;

                    drinks = em.createNamedQuery("Drink.get-drinks-in-between-time", Drink.class)
                            .setParameter("start", d)
                            .setParameter("id", user.getId())
                            .getResultList();

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d);

                    for (int i = 0; i < 7; i++) {
                        Stream<Drink> drinksOfOneDay = drinks.stream().filter(
                                drink -> drink.getDrankDate().equals(cal1.getTime())
                        );
                        if (drinksOfOneDay.count() < maxAllowed) {
                            failedCount++;
                        }
                        cal1.add(Calendar.DAY_OF_MONTH, i);
                    }
                    if (failedCount < days) {
                        user.setPoints(user.getPoints() + challenge.getTemplate().getAmount());
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

    private User getUserFromJwt(final String jwt) {
        long id = jwtHelper.getUserId(jwt);
        User user = em.find(User.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }
}
