package group5.ebay2.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_users")
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    public AuthUser() {}

    public AuthUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
