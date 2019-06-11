package helper;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryHelper {

    private static EntityManagerFactory factory;

    static {
        try {
            factory = Persistence.createEntityManagerFactory("DrBoozePU");
        } catch(ExceptionInInitializerError ex) {
            ex.printStackTrace();
        }
    }

    public static EntityManagerFactory getFactory() {
        return factory;
    }
}
