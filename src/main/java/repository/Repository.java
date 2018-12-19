package repository;

import entities.User;
import service.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Alexander Burghuber
 */
public class Repository {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("DrBoozePU");
    private static EntityManager em = emf.createEntityManager();

    public void testJPA() {
        em.getTransaction().begin();

        User user1 = new User("burgus","passme","burgi@burgmail.com");

        em.persist(user1);
        em.getTransaction().commit();
    }

    public void login(String username, String password) {
        // TODO: login
    }

    public void register(String username, String password, String email) {
        em.getTransaction().begin();

        // TODO: Registrier Überprüfung
        User user = new User(username, password, email);

        em.persist(user);
        em.getTransaction().commit();
    }
}
