package searchengine.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.utils.HibernateUtil;

import javax.persistence.Query;
import java.util.List;

@Component
public class IndexDao implements DaoInterface<Index, Integer> {
    @Override
    public void save(Index index) {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.save(index);
        tx.commit();
        session.close();
    }

    @Override
    public void update(Index index) {

    }

    @Override
    public Index findById(Integer integer) {
        return null;
    }

    @Override
    public void delete(Index index) {

    }

    @Override
    public List<Index> findAll() {
        Session session = HibernateUtil.getSession();
        Query query = session.createQuery("from Index");
        List<Index> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void deleteAll() {
        List<Index> indexes = findAll();
        indexes.forEach(this::delete);
    }

    @Override
    public void dropAndCreateTable() {
        String dropPage = "DROP TABLE IF EXISTS index";
//        String createPage = "CREATE TABLE page (" +
//                "id INT NOT NULL AUTO_INCREMENT, " +
//                "path TEXT NOT NULL, " +
//                "code INT NOT NULL, " +
//                "content MEDIUMTEXT NOT NULL, " +
//                "site_id INT NOT NULL, " +
//                "PRIMARY KEY(id), KEY(path(200)))";
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        session.createSQLQuery(dropPage).executeUpdate();
//        session.createSQLQuery(createPage).executeUpdate();
        tx.commit();
        session.close();

    }
}
