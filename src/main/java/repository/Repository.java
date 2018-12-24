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

    public String register(String username, String email, String password) {

        String response;

        em.getTransaction().begin();

        User user = new User(username, password, email);

        em.persist(user);
        em.getTransaction().commit();
        response = "Registered new user: " + user.getUsername();

        System.out.println(response);
        return response;
    }
}
