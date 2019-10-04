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
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
            JSONObject drinkJson = new JSONObject()
                    .put("alcohol", drink.getAlcohol().toJson())
                    .put("drankDate", drink.getDrankDate())
                    .put("longitude", drink.getLongitude())
                    .put("latitude", drink.getLatitude());
            jsonArray.put(drinkJson);
        }

        LOG.info("Returned drinks of user: " + user.getId() + ", " + user.getUsername());
        return Response.ok(jsonArray.toString()).build();
    }

    /**
     * Returns all alcohols of this type.
     *
     * @param typeStr the alcohol type
     * @return a response containing either an OK (with the alcohols) or NOT_FOUND
     */
    public Response getAlcohols(String typeStr) {
        if (typeStr.equals("BEER") || typeStr.equals("WINE") || typeStr.equals("LIQUOR") || typeStr.equals("COCKTAIL")) {
            AlcoholType type = AlcoholType.valueOf(typeStr);
            TypedQuery<Alcohol> query = em.createNamedQuery("Alcohol.get-with-type", Alcohol.class)
                    .setParameter("type", type);
            List<Alcohol> alcohols = query.getResultList();

            JSONArray jsonArray = new JSONArray();
            for (Alcohol alcohol : alcohols) {
                jsonArray.put(alcohol.toJson());
            }
            LOG.info("Returned alcohols of type: " + typeStr);
            return Response.ok(jsonArray.toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Loads all alcohol from the json files into the database.
     */
    public void loadAlcohol() {
        String folder = "src/main/resources/alcohol/";
        Map<AlcoholType, String> alcohols = new HashMap<>();
        alcohols.put(AlcoholType.BEER, "beers.json");
        alcohols.put(AlcoholType.WINE, "wine.json");
        alcohols.put(AlcoholType.LIQUOR, "liquor.json");
        alcohols.put(AlcoholType.COCKTAIL, "cocktails.json");

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
                    em.getTransaction().begin();
                    em.persist(alcohol);
                    em.getTransaction().commit();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}
