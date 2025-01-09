package searchengine.dao;


import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Proxy;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.utils.HibernateUtil;

import javax.persistence.Query;
import java.util.List;
import java.util.Objects;

@Component
public class SiteDao implements DaoInterface<Site, Integer>{
    @Override
    public void save(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.save(site);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.update(site);
        tx.commit();
        session.close();
    }

    @Override
    public Site findById(Integer id) {
        Session session = HibernateUtil.getSession();
        session.setDefaultReadOnly(true);
        Site result = session.get(Site.class, id);
        session.close();
        return result;
    }

    @Override
    public void delete(Site site) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.delete(site);
        tx.commit();
        session.close();
    }

    @Override
    public List<Site> findAll() {
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Site");
        List<Site> result = query.getResultList();
        result.forEach(site -> {
            Hibernate.initialize(site.getLemmas());
            Hibernate.initialize(site.getPages());
        });
        session.close();
        return result;
    }

    @Override
    public void deleteAll() {
        List<Site> sites = findAll();
        sites.forEach(this::delete);
    }

    @Override
    public void dropAndCreateTable() {
        String dropPage = "DROP TABLE IF EXISTS site";
        String createPage = "CREATE TABLE site(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL, " +
                "status_time DATETIME NOT NULL, " +
                "last_error TEXT, " +
                "url VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY(id))";
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(dropPage).executeUpdate();
        session.createSQLQuery(createPage).executeUpdate();
        tx.commit();
        session.close();
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
        session.setDefaultReadOnly(true);
        Query query = session.createQuery("from Site where url = " + "'" + url + "'");
        Object result = query.getSingleResult();
        session.close();
        return result;
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
        if (isExistByUrl(site.getUrl())){
            session.update(site);
        } else {
            session.save(site);
        }
        tx.commit();
        session.close();
    }

}
