package helper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;

public class EntityManagerHelper {

    private static EntityManager em;
    private static Map<String, String> properties;

    public static EntityManager getInstance() {
        if (em == null) {
            EntityManagerFactory emf;
            if (properties != null) {
                emf = Persistence.createEntityManagerFactory("DrBoozePU", properties);
            } else {
                emf = Persistence.createEntityManagerFactory("DrBoozePU");
            }
            em = emf.createEntityManager();
        }
        return em;
    }

    public static void setProperties(Map<String, String> props) {
        properties = props;
    }
}
