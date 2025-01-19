package searchengine.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.Site;
import searchengine.utils.HibernateUtil;

import javax.persistence.Query;
import java.util.List;

@Component
public class IndexDao implements DaoInterface<Index, Integer> {
    @Override
    public void save(Index index) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(index);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void update(Index index) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(index);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public Index findById(Integer id) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Index result = session.get(Index.class, id);
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void delete(Index index) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(index);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public List<Index> findAll() {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Index");
            List<Index> result = query.getResultList();
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
            query = session.createSQLQuery("TRUNCATE TABLE indexes");
            query.executeUpdate();
            query = session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1");
            query.executeUpdate();
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }


}
