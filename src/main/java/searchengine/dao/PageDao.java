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
import java.util.List;

@Component
public class PageDao implements DaoInterface<Page, Integer>{
    @Override
    public void save(Page page) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(page);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void update(Page page) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(page);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public Page findById(Integer id) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Page result = session.get(Page.class, id);
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void delete(Page page) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(page);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public List<Page> findAll() {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Page");
            List<Page> result = query.getResultList();
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
            query = session.createSQLQuery("TRUNCATE TABLE page");
            query.executeUpdate();
            query = session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1");
            query.executeUpdate();
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    public List<Page> findBySiteId(Integer id){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Page where site_id = " + "'" + id + "'");
            List<Page> result = query.getResultList();
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    public int count(){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try{
            NativeQuery query = session.createSQLQuery("select count(*) as count from page");
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
            NativeQuery query = session.createSQLQuery("select count(*) as count from page where site_id = :siteId")
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
