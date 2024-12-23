package searchengine.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import searchengine.model.Page;
import searchengine.utils.HibernateUtil;

import javax.persistence.Query;
import java.util.List;

@Component
public class PageDao implements DaoInterface<Page, Integer>{
    @Override
    public void save(Page page) {
        Session session = HibernateUtil.getSession();
        session.save(page);
        session.close();
    }

    @Override
    public void update(Page page) {
        Session session = HibernateUtil.getSession();
        session.update(page);
        session.close();
    }

    @Override
    public Page findById(Integer id) {
        Session session = HibernateUtil.getSession();
        session.setDefaultReadOnly(true);
        Page result = session.get(Page.class, id);
        session.close();
        return result;
    }

    @Override
    public void delete(Page page) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.delete(page);
        tx.commit();
        session.close();
    }

    @Override
    public List<Page> findAll() {
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Page");
        List<Page> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void deleteAll() {
        List<Page> pages = findAll();
        pages.forEach(this::delete);
    }

    @Override
    public void dropAndCreateTable() {
        String dropPage = "DROP TABLE IF EXISTS page";
        String createPage = "CREATE TABLE page (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "PRIMARY KEY(id), KEY(path(200)))";
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(dropPage).executeUpdate();
        session.createSQLQuery(createPage).executeUpdate();
        tx.commit();
        session.close();
    }

    public List<Page> findBySiteId(Integer id){
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Page where site_id = " + "'" + id + "'");
        List<Page> result = query.getResultList();
        session.close();
        return result;
    }

    public List<Page> findByPath(String path){
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Page where path = "+"'"+path+"'");
        List<Page> result = query.getResultList();
        session.close();
        return result;
    }

    public boolean ifExistByPath(String path){
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Page where path = "+"'"+path+"'");
        boolean result = !query.getResultList().isEmpty();
        session.close();
        return result;
    }



}
