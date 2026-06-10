package group5.ebay2.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "address_types")
public class AddressType {

    @Id
    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private boolean active = true;

    protected AddressType() {
    }

    public AddressType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void rename(String displayName) {
        this.displayName = displayName;
    }
}