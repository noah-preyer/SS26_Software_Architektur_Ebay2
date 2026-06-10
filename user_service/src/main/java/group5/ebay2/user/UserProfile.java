package group5.ebay2.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID id;

    // ID from auth-service
    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 30)
    private String phoneNumber;

    // Example: "users/5/profile.png" in MinIO
    private String profileImageObjectKey;

    @OneToMany(
            mappedBy = "userProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Address> addresses = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(UUID authUserId, String username, String email) {
        this.authUserId = authUserId;
        this.username = username;
        this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageObjectKey() {
        return profileImageObjectKey;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void updateProfile(
            String firstName,
            String lastName,
            String phoneNumber
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public void updateProfileImage(String profileImageObjectKey) {
        this.profileImageObjectKey = profileImageObjectKey;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUserProfile(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUserProfile(null);
    }
}