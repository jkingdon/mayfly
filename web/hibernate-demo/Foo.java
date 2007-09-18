import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Foo {
    
    @Id
    int x;
    
    String name;

}
