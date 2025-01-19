package searchengine.dao;


import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.utils.HibernateUtil;

import javax.persistence.Query;
import java.util.List;

@Component
public class SiteDao implements DaoInterface<Site, Integer>{
    @Override
    public void save(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(site);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void update(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(site);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public Site findById(Integer id) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Site result = session.get(Site.class, id);
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public void delete(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.delete(site);
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    @Override
    public List<Site> findAll() {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            Query query = session.createQuery("from Site");
            List<Site> result = query.getResultList();
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
            query = session.createSQLQuery("TRUNCATE TABLE site");
            query.executeUpdate();
            query = session.createSQLQuery("SET FOREIGN_KEY_CHECKS = 1");
            query.executeUpdate();
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

    public boolean isExistByUrl(String url){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery("from Site where url = " + "'" + url + "'");
        boolean result = !query.getResultList().isEmpty();
        tx.commit();
        session.close();
        return result;
    }

    public Object findByUrl(String url){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            session.setDefaultReadOnly(true);
            Query query = session.createQuery("from Site where url = " + "'" + url + "'");
            Object result = query.getSingleResult();
            return result;
        } catch (Exception e){
            return null;
        } finally {
            tx.commit();
            session.close();
        }
    }

    public Object findBySiteId(String id){
        Session session = HibernateUtil.getSession();
        session.setDefaultReadOnly(true);
        Query query = session.createQuery("from Site where site_id = " + "'" + id + "'");
        Object result = query.getSingleResult();
        session.close();
        return result;
    }

    public void saveOrUpdate(Site site){
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try {
            if (isExistByUrl(site.getUrl())) {
                session.update(site);
            } else {
                session.save(site);
            }
        } catch (Exception e){

        } finally {
            tx.commit();
            session.close();
        }
    }

}
