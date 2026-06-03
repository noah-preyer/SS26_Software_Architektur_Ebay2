package group5.ebay2.user;
import jakarta.persistence.*;

@Entity
@Table(name = "authors")
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String password;
    private String email;
}

