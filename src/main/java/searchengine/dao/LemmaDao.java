package searchengine.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Component;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.utils.HibernateUtil;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Component
public class LemmaDao implements DaoInterface<Lemma, Integer> {

    @Override
    public void save(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(lemma);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void update(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(lemma);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public Lemma findById(Integer id) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Lemma result = session.get(Lemma.class, id);
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void delete(Lemma lemma) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(lemma);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public List<Lemma> findAll() {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Lemma");
            List<Lemma> result = query.getResultList();
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void deleteAll() {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            Query query = session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 0");
            query.executeUpdate();
            query = session.createSQLQuery("TRUNCATE TABLE lemmas");
            query.executeUpdate();
            query = session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1");
            query.executeUpdate();
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }


    public Lemma findByNameAndSite(String name, int siteId){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Lemma where lemma = :lemma and site_id = :siteId")
                    .setParameter("lemma", name)
                    .setParameter("siteId", siteId);
            Lemma lemma = (Lemma) query.getSingleResult();
            return lemma;
        } catch (NoResultException e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    public List<Lemma> findByName(String name){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Lemma where lemma = " + "'" + name + "'");
            List<Lemma> result = query.getResultList();
            return result;
        } catch (NoResultException e){
            return null;
        } finally {
            tx.commit();
            session.close();
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

    public void addFrequency(String lemma, int siteId){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("update Lemma set frequency = frequency + 1 where lemma = :lemma and site_id = :siteId")
                    .setParameter("lemma", lemma)
                    .setParameter("siteId", siteId);
            query.executeUpdate();
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    public  List<Page> getPages(Lemma lemma){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        List<Page> result = new ArrayList<>();
        try {
            Query query = session.createQuery("select p from Page p join p.indexes i where i.lemma.id = :lemmaId").setParameter("lemmaId", lemma.getId());
            result = query.getResultList();
        } catch (Exception e){

        } finally {
        tx.commit();
        session.close();
        }
        return result;
    }

    public int count(){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            NativeQuery query = session.createSQLQuery("select count(*) as count from lemmas");
            return ((Number) query.uniqueResult()).intValue();
        } catch (Exception e){
            return 0;
        } finally {
            tx.commit();
            session.close();
        }
    }


    public int count(int siteId){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            NativeQuery query = session.createSQLQuery("select count(*) as count from lemmas where site_id = :siteId")
                    .setParameter("siteId", siteId);
            return ((Number) query.uniqueResult()).intValue();
        } catch (Exception e){
            return 0;
        } finally {
            tx.commit();
            session.close();
        }
    }


}
