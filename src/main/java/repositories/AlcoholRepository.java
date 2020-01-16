package repositories;

import data.entities.Alcohol;
import data.entities.Drink;
import data.entities.User;
import data.enums.AlcoholType;
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
import java.math.BigDecimal;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The repository for everything related to alcohols and drinks.
 * Note that every function that receives a jwt may throw a 401 UNAUTHORIZED Http Response
 * if the jwt is not longer valid.
 */
public class AlcoholRepository {

    private EntityManager em = EntityManagerHelper.getInstance();
    private JwtHelper jwtHelper = new JwtHelper();
    private static Logger LOG = Logger.getLogger(AlcoholRepository.class.getName());

    /**
     * Adds a drink to an user.
     *
     * @param jwt       the json web token
     * @param alcoholId the id of the {@link Alcohol}
     * @param drankDate the date the drink was drank
     * @param longitude the longitude of the position
     * @param latitude  the latitude of the position
     * @return a response containing OK or NOT_FOUND
     */
    public Response addDrink(String jwt, long alcoholId, Date drankDate, BigDecimal longitude, BigDecimal latitude) {
        User user = getUserFromJwt(jwt);
        Alcohol alcohol = em.find(Alcohol.class, alcoholId);
        if (alcohol != null) {
            Drink drink = new Drink(user, alcohol, drankDate, longitude, latitude);
            em.getTransaction().begin();
            em.persist(drink);
            em.getTransaction().commit();

            LOG.info("Added drink to user: " + user.getId() + ", " + user.getUsername());
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Returns all drinks the user drank.
     *
     * @param jwt the json web token
     * @return a response containing either OK (with the drinks) or UNAUTHORIZED
     */
    public Response getDrinks(String jwt) {
        User user = getUserFromJwt(jwt);
        em.refresh(user);
        JSONArray jsonArray = new JSONArray();
        for (Drink drink : user.getDrinks()) {
            jsonArray.put(drink.toJson());
        }
        LOG.info("Returned drinks of user: " + user.getId() + ", " + user.getUsername());
        return Response.ok(jsonArray.toString()).build();
    }


    /**
     * Removes a drink of an user.
     *
     * @param jwt     the json web token
     * @param drinkId the drink id
     * @return a response containing either an OK or NOT_FOUND
     */
    public Response removeDrink(String jwt, long drinkId) {
        User user = getUserFromJwt(jwt);
        Drink drink = em.find(Drink.class, drinkId);
        if (drink != null) {
            em.getTransaction().begin();
            user.getDrinks().remove(drink);
            em.getTransaction().commit();
            LOG.info("Removed drink from user: " + user.getId() + ", " + user.getUsername());
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Returns all alcohols of this type.
     *
     * @param typeStr the alcohol type
     * @return a response containing either an OK (with the alcohols) or NOT_FOUND
     */
    public Response getAlcohols(String typeStr) {
        typeStr = typeStr.toUpperCase();
        if (validateAlcoholType(typeStr)) {
            AlcoholType type = AlcoholType.valueOf(typeStr);

            List<Alcohol> alcohols
                    = em.createQuery(
                    "SELECT a FROM Alcohol a " +
                            "WHERE a.type = :type " +
                            "AND a.user IS NULL", Alcohol.class)
                    .setParameter("type", type)
                    .getResultList();

            JSONArray jsonArray = new JSONArray();
            for (Alcohol alcohol : alcohols) {
                jsonArray.put(alcohol.toJson());
            }

            LOG.info("Returned alcohols of type: " + typeStr);
            return Response.ok(jsonArray.toString()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Returns the favourite alcohols of an user of this type.
     *
     * @param jwt     the json web token
     * @param typeStr the alcohol type
     * @return a response containing either an OK (with the favorite alcohols) or NOT_FOUND
     */
    public Response getFavouritesOfType(String jwt, String typeStr) {
        if (validateAlcoholType(typeStr)) {
            User user = getUserFromJwt(jwt);
            AlcoholType type = AlcoholType.valueOf(typeStr);
            List<Alcohol> favouritesOfType = user.getFavouriteAlcohols()
                    .stream()
                    .filter(alcohol -> alcohol.getType() == type)
                    .collect(Collectors.toList());
            JSONArray jsonArray = new JSONArray();
            for (Alcohol alcohol : favouritesOfType) {
                jsonArray.put(alcohol.toJson());
            }
            LOG.info("Returned favourite alcohols of type " + typeStr
                    + " for user: " + user.getId() + ", " + user.getUsername());
            return Response.ok(jsonArray.toString()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Adds a new favourite alcohol of an user.
     *
     * @param jwt       the json web token
     * @param alcoholId the alcohol id
     * @return a response containing either an OK or NOT_FOUND
     */
    public Response addFavourite(String jwt, long alcoholId) {
        User user = getUserFromJwt(jwt);
        Alcohol alcohol = em.find(Alcohol.class, alcoholId);
        if (alcohol != null) {
            em.getTransaction().begin();
            user.getFavouriteAlcohols().add(alcohol);
            em.getTransaction().commit();
            LOG.info("Added favourite alcohol to user: " + user.getId() + ", " + user.getUsername());
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Removes a favourite alcohol of an user.
     *
     * @param jwt       the json web token
     * @param alcoholId the alcohol id
     * @return a response containing either an OK or NOT_FOUND
     */
    public Response removeFavourite(String jwt, long alcoholId) {
        User user = getUserFromJwt(jwt);
        Alcohol alcohol = em.find(Alcohol.class, alcoholId);
        if (alcohol != null) {
            em.getTransaction().begin();
            user.getFavouriteAlcohols().remove(alcohol);
            em.getTransaction().commit();
            LOG.info("Removed favourite alcohol of user: " + user.getId() + ", " + user.getUsername());
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Returns the personal alcohols of an user of this type.
     *
     * @param jwt     the json web token
     * @param typeStr the alcohol type
     * @return a response containing either an OK (with the personal alcohols) or NOT_FOUND
     */
    public Response getPersonalAlcoholsOfType(String jwt, String typeStr) {
        typeStr = typeStr.toUpperCase();
        if (validateAlcoholType(typeStr)) {
            AlcoholType type = AlcoholType.valueOf(typeStr);
            User user = getUserFromJwt(jwt);

            List<Alcohol> personalAlcohols
                    = em.createQuery(
                    "SELECT a FROM Alcohol a WHERE a.user = :user" +
                            " AND a.type = :type" +
                            " AND a.isArchived = false", Alcohol.class)
                    .setParameter("user", user)
                    .setParameter("type", type)
                    .getResultList();

            JSONArray jsonArray = new JSONArray();
            for (Alcohol alcohol : personalAlcohols) {
                jsonArray.put(alcohol.toJson());
            }

            LOG.info("Returned personal alcohols of type " + typeStr
                    + " for user: " + user.getId() + ", " + user.getUsername());
            return Response.ok(jsonArray.toString()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Adds a new personal alcohol.
     *
     * @param jwt        the json web token
     * @param type       the alcohol type
     * @param name       the alcohol name
     * @param category   the alcohol category
     * @param percentage the percentage of the alcohol
     * @param amount     the amount of the alcohol
     * @return a response containing either an OK (with the new personal alcohols) or FORBIDDEN
     */
    public Response addPersonalAlcohol(String jwt, AlcoholType type, String name, String category, float percentage, int amount) {
        if (name != null && percentage >= 0 && percentage <= 100 && amount >= 1 && amount <= 2000) {
            User user = getUserFromJwt(jwt);

            Alcohol personalAlcohol = new Alcohol(type, name, percentage, amount);
            personalAlcohol.setUser(user);
            if (category != null) {
                personalAlcohol.setCategory(category.toLowerCase());
            }

            em.getTransaction().begin();
            em.persist(personalAlcohol);
            em.getTransaction().commit();

            LOG.info("Add personal alcohol to user: " + user.getId() + ", " + user.getUsername());
            return Response.ok(personalAlcohol.toJson().toString()).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    /**
     * Archives a personal alcohol.
     * The alcohol will not be completely removed because a drink might already been drank with it.
     * The alcohol will be also be removed from favourites if it is one.
     *
     * @param jwt       the json web token
     * @param alcoholId the alcohol id
     * @return a response containing either an OK, FORBIDDEN or NOT_FOUND
     */
    public Response removePersonalAlcohol(String jwt, long alcoholId) {
        User user = getUserFromJwt(jwt);
        Alcohol alcohol = em.find(Alcohol.class, alcoholId);
        if (alcohol != null) {
            if (alcohol.getUser() == user) {
                em.getTransaction().begin();
                alcohol.setArchived(true);
                em.getTransaction().commit();
                LOG.info("Removed personal alcohol of user: " + user.getId() + ", " + user.getUsername());
                removeFavourite(jwt, alcoholId);
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Loads all alcohol from the json files into the database.
     */
    public void loadAlcohol() {
        Map<AlcoholType, String> alcohols = new HashMap<>();
        alcohols.put(AlcoholType.BEER, "beers.json");
        alcohols.put(AlcoholType.WINE, "wine.json");
        alcohols.put(AlcoholType.LIQUOR, "liquor.json");
        alcohols.put(AlcoholType.COCKTAIL, "cocktails.json");

        alcohols.forEach((type, file) -> {
            URL url = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource("alcohol/" + file);

            if (url != null) {
                try (InputStream inputStream = url.openStream()) {
                    // InputStream inputStream = Files.newInputStream(Paths.get(folder + file));
                    JSONArray jsonArray = new JSONArray(new JSONTokener(inputStream));
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        Alcohol alcohol = new Alcohol(
                                type,
                                json.getString("name"),
                                json.getFloat("percentage"),
                                json.getInt("amount")
                        );
                        if (json.has("category")) {
                            alcohol.setCategory(json.getString("category"));
                        }
                        em.getTransaction().begin();
                        em.persist(alcohol);
                        em.getTransaction().commit();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Template file " + file + " not found");
            }
        });
    }

    private User getUserFromJwt(String jwt) {
        long id = jwtHelper.getUserId(jwt);
        User user = em.find(User.class, id);
        if (user == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return user;
    }

    private boolean validateAlcoholType(String typeStr) {
        return typeStr.equals("BEER") || typeStr.equals("WINE") || typeStr.equals("LIQUOR") || typeStr.equals("COCKTAIL");
    }
}
