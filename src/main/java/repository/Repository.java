package repository;

import entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/*
 * TODO: .idea folder .gitignore
 */

/**
 * @author Alexander Burghuber
 */
public class Repository {

    private static Repository instance = null;

    private Repository() {}

    public static Repository getInstance() {
        if (instance == null)
            instance = new Repository();
        return instance;
    }

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private static EntityManager em = emf.createEntityManager();

    public void testJPA() {
        em.getTransaction().begin();

        User user1 = new User("burgus");

        em.persist(user1);
        em.getTransaction().commit();
    }

}
