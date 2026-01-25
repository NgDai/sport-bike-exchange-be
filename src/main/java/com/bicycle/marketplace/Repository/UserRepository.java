package com.bicycle.marketplace.Repository;

import com.bicycle.marketplace.Repository.Impl.IUserRepository;
import com.bicycle.marketplace.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;
import java.util.Optional;

public class UserRepository implements IUserRepository {
    private static EntityManagerFactory emf;
    private static EntityManager em = null;

    public UserRepository(){
        emf = Persistence.createEntityManagerFactory("bicycle-marketplace");
        em = emf.createEntityManager();
    }

    public UserRepository(String persistenceUnitName){
        emf = Persistence.createEntityManagerFactory(persistenceUnitName);
    }


    @Override
    public void createUser(User user) {
        try{
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }catch (Exception e){
            if(em.getTransaction().isActive() && em != null){
                em.getTransaction().rollback();
            }
            throw new RuntimeException();
        }finally {
            if(em != null){
                em.close();
            }
        }
    }

    @Override
    public List<User> listAllUsers() {
        try {
            em = emf.createEntityManager();
            em.getTransaction().begin();
            List<User> items = em.createQuery("select u from User u", User.class).getResultList();
            em.getTransaction().commit();
            return items;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public User findUserById(int userId) {
        try{
            em = emf.createEntityManager();
            em.getTransaction().begin();
            User items = em.find(User.class, userId);
            em.getTransaction().commit();
            return items;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateUser(User user) {
        try{
            em = emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public void deleteUser(int userId) {
        try{
            em = emf.createEntityManager();
            User item = this.findUserById(userId);
            if(item != null){
                em.getTransaction().begin();
                em.remove(item);
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}


