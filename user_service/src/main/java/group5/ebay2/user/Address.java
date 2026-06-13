package group5.ebay2.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(nullable = false, length = 20)
    private String houseNumber;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_type_code", nullable = false)
    private AddressType addressType;

    @Column(nullable = false)
    private boolean defaultAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Address() {
    }

    public Address(
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String country,
            AddressType addressType,
            boolean defaultAddress
    ) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.addressType = addressType;
        this.defaultAddress = defaultAddress;
    }

    @PrePersist
    protected void onCreate() {
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

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void update(
            String street,
            String houseNumber,
            String postalCode,
            String city,
            String country,
            AddressType addressType,
            boolean defaultAddress
    ) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.addressType = addressType;
        this.defaultAddress = defaultAddress;
    }
}