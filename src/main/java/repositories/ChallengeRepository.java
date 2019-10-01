package repositories;

import data.entities.ChallengeBO;
import data.entities.DrinkBO;
import data.entities.Template;
import data.entities.UserBO;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class ChallengeRepository {
    private EntityManager em;
    private JwtHelper jwtHelper;

    public ChallengeRepository() {
        em = EntityManagerHelper.getInstance();
        this.jwtHelper = new JwtHelper();
    }

    /**
     * Loads every template from the json file into the database
     *
     * @throws IOException while file reading
     */
    public void loadTemplates() throws IOException {

        String jsonFolder = "src/main/resources/challenges/";

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

    private UserBO getUserFromJwt(final String jwt) {
        long id = jwtHelper.getUserId(jwt);
        UserBO user = em.find(UserBO.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }
}