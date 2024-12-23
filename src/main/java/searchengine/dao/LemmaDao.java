package searchengine.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.utils.HibernateUtil;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;

@Component
public class LemmaDao implements DaoInterface<Lemma, Integer> {
    @Override
    public void save(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.save(lemma);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.update(lemma);
        tx.commit();
        session.close();
    }

    @Override
    public Lemma findById(Integer integer) {
        return null;
    }

    @Override
    public void delete(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.delete(lemma);
        tx.commit();
        session.close();
    }

    @Override
    public List<Lemma> findAll() {
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Lemma");
        List<Lemma> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void deleteAll() {
        List<Lemma> lemmas = findAll();
        try {
            lemmas.forEach(this::delete);
        } catch (Exception e){

        }
    }

    @Override
    public void dropAndCreateTable() {
        String dropPage = "DROP TABLE IF EXISTS lemma";
        String createPage = "CREATE TABLE lemma(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "lemma VARCHAR(255) NOT NULL, " +
                "frequency INT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "PRIMARY KEY(id))";

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(dropPage).executeUpdate();
        session.createSQLQuery(createPage).executeUpdate();
        tx.commit();
        session.close();
    }

    public Lemma findByNameAndSite(String name, int site_id){
        Session session = HibernateUtil.getSession();
        try {
            Query query = session.createQuery("from Lemma where lemma = " + "'" + name + "'" + " and site_id = " + site_id);
            Lemma lemma = (Lemma) query.getSingleResult();
            session.close();
            return lemma;
        } catch (NoResultException e){
            session.close();
            return null;
        }
    }

    public List<Lemma> findByName(String name){
        Session session = HibernateUtil.getSession();
        try {
            Query query = session.createQuery("from Lemma where lemma = " + "'" + name + "'");
            List<Lemma> result = query.getResultList();
            session.close();
            return result;
        } catch (NoResultException e){
            session.close();
            return null;
        }
    }

    public List<Lemma> findBySite(int id){
        Session session = HibernateUtil.getSession();
        try {
            Query query = session.createQuery("from Lemma where site_id = " + id);
            List<Lemma> result = query.getResultList();
            session.close();
            return result;
        } catch (NoResultException e){
            session.close();
            return null;
        }
    }


}
