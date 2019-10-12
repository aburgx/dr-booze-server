package helper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerHelper {
    private static EntityManager em;

    public static EntityManager getInstance() {
        if (em == null) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
            em = emf.createEntityManager();
        }
        return em;
    }
}
