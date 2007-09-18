import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;


public class FooPersistence {
    
    Session session;

    public FooPersistence(Connection connection) {
        AnnotationConfiguration configuration = 
            new AnnotationConfiguration();
        configuration.setProperty("hibernate.dialect", "MayflyDialect");
        configuration.addAnnotatedClass(Foo.class);
        SessionFactory factory = configuration.buildSessionFactory();
        session = factory.openSession(connection);
    }

    public Foo getFoo(int id) {
        return (Foo) session.get(Foo.class, id);
    }

}
