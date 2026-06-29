package group5.ebay2.user;

import group5.ebay2.user.dtos.AddressDto;
import group5.ebay2.user.dtos.UserProfileDto;
import group5.ebay2.user.repositories.AddressRepository;
import group5.ebay2.user.repositories.AddressTypeRepository;
import group5.ebay2.user.repositories.UserProfileRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressTypeRepository addressTypeRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    private UserProfileDto.Request validRequest;
    private UUID authUserId;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        AuthServiceClient mockAuthClient() {
            AuthServiceClient client = mock(AuthServiceClient.class);
            when(client.createUser(any(), any(), any()))
                    .thenReturn(new AuthServiceClient.AuthUser(UUID.randomUUID(), "testuser", "test@example.com"));
            when(client.getUser(any()))
                    .thenReturn(new AuthServiceClient.AuthUser(UUID.randomUUID(), "testuser", "test@example.com"));
            return client;
        }
    }

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        userProfileRepository.deleteAll();

        authUserId = UUID.randomUUID();
        when(authServiceClient.createUser(any(), any(), any()))
                .thenReturn(new AuthServiceClient.AuthUser(authUserId, "testuser", "test@example.com"));
        when(authServiceClient.getUser(any()))
                .thenReturn(new AuthServiceClient.AuthUser(authUserId, "testuser", "test@example.com"));

        validRequest = new UserProfileDto.Request(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User",
                "+491234567890",
                null,
                null, null, null, null, null
        );
    }

    @Test
    void addUser_shouldCreateUser() {
        UserProfileDto.Response response = userService.addUser(validRequest);

        assertThat(response.id()).isNotNull();
        assertThat(response.authUserId()).isEqualTo(authUserId);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("Test");
        assertThat(response.lastName()).isEqualTo("User");
        assertThat(response.phoneNumber()).isEqualTo("+491234567890");
        assertThat(response.profileImageObjectKey()).isNull();
    }

    @Test
    void addUser_shouldThrowOnDuplicateEmail() {
        userService.addUser(validRequest);

        UserProfileDto.Request duplicate = new UserProfileDto.Request(
                "otheruser",
                "test@example.com",
                "password123",
                "Other",
                "User",
                "+499999999999",
                null,
                null, null, null, null, null
        );

        assertThatThrownBy(() -> userService.addUser(duplicate))
                .isInstanceOf(UserExceptions.UserAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void addUser_shouldThrowOnDuplicateUsername() {
        userService.addUser(validRequest);

        UserProfileDto.Request duplicate = new UserProfileDto.Request(
                "testuser",
                "other@example.com",
                "password123",
                "Other",
                "User",
                "+499999999999",
                null,
                null, null, null, null, null
        );

        assertThatThrownBy(() -> userService.addUser(duplicate))
                .isInstanceOf(UserExceptions.UserAlreadyExistsException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void getUser_shouldReturnUser() {
        UserProfileDto.Response created = userService.addUser(validRequest);
        UserProfileDto.Response found = userService.getUser(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.email()).isEqualTo("test@example.com");
    }

    @Test
    void getUser_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> userService.getUser(UUID.randomUUID()))
                .isInstanceOf(UserExceptions.UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserByAuthUserId_shouldReturnUser() {
        userService.addUser(validRequest);
        UserProfileDto.Response found = userService.getUserByAuthUserId(authUserId);

        assertThat(found.email()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByAuthUserId_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> userService.getUserByAuthUserId(UUID.randomUUID()))
                .isInstanceOf(UserExceptions.UserNotFoundException.class);
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        userService.addUser(validRequest);
        UserProfileDto.Response found = userService.getUserByEmail("test@example.com");

        assertThat(found.username()).isEqualTo("testuser");
    }

    @Test
    void getUserByUsername_shouldReturnUser() {
        userService.addUser(validRequest);
        UserProfileDto.Response found = userService.getUserByUsername("testuser");

        assertThat(found.email()).isEqualTo("test@example.com");
    }

    @Test
    void updateUser_shouldUpdateFields() {
        UserProfileDto.Response created = userService.addUser(validRequest);

        UserProfileDto.UpdateRequest update = new UserProfileDto.UpdateRequest(
                "newusername",
                "new@example.com",
                "NewFirst",
                "NewLast",
                "+499999999999",
                "users/1/profile.png"
        );

        UserProfileDto.Response updated = userService.updateUser(created.id(), update);

        assertThat(updated.username()).isEqualTo("newusername");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.firstName()).isEqualTo("NewFirst");
        assertThat(updated.lastName()).isEqualTo("NewLast");
        assertThat(updated.phoneNumber()).isEqualTo("+499999999999");
        assertThat(updated.profileImageObjectKey()).isEqualTo("users/1/profile.png");
    }

    @Test
    void updateUser_shouldOnlyUpdateNonNullFields() {
        UserProfileDto.Response created = userService.addUser(validRequest);

        UserProfileDto.UpdateRequest update = new UserProfileDto.UpdateRequest(
                null,
                null,
                "OnlyFirst",
                null,
                null,
                null
        );

        UserProfileDto.Response updated = userService.updateUser(created.id(), update);

        assertThat(updated.username()).isEqualTo("testuser");
        assertThat(updated.email()).isEqualTo("test@example.com");
        assertThat(updated.firstName()).isEqualTo("OnlyFirst");
        assertThat(updated.lastName()).isEqualTo("User");
        assertThat(updated.phoneNumber()).isEqualTo("+491234567890");
    }

    @Test
    void updateUser_shouldThrowOnDuplicateUsername() {
        UserProfileDto.Response user1 = userService.addUser(validRequest);

        userService.addUser(new UserProfileDto.Request(
                "otheruser",
                "other@example.com",
                "password123",
                "Other",
                "User",
                "+499999999999",
                null,
                null, null, null, null, null
        ));

        UserProfileDto.UpdateRequest update = new UserProfileDto.UpdateRequest(
                "otheruser",
                null, null, null, null, null
        );

        assertThatThrownBy(() -> userService.updateUser(user1.id(), update))
                .isInstanceOf(UserExceptions.UserAlreadyExistsException.class);
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        UserProfileDto.Response created = userService.addUser(validRequest);
        userService.deleteUser(created.id());

        assertThat(userProfileRepository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteUser_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> userService.deleteUser(UUID.randomUUID()))
                .isInstanceOf(UserExceptions.UserNotFoundException.class);
    }

    @Test
    void addAddress_shouldAddAddressToUser() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        AddressDto.Request addressReq = new AddressDto.Request(
                "Main Street", "10", "10115", "Berlin", "Germany",
                "SHIPPING", true
        );

        AddressDto.Response address = userService.addAddress(user.id(), addressReq);

        assertThat(address.id()).isNotNull();
        assertThat(address.street()).isEqualTo("Main Street");
        assertThat(address.city()).isEqualTo("Berlin");
        assertThat(address.addressTypeCode()).isEqualTo("SHIPPING");
        assertThat(address.defaultAddress()).isTrue();
    }

    @Test
    void addAddress_shouldThrowOnInvalidAddressType() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        AddressDto.Request addressReq = new AddressDto.Request(
                "Main Street", "10", "10115", "Berlin", "Germany",
                "INVALID_TYPE", false
        );

        assertThatThrownBy(() -> userService.addAddress(user.id(), addressReq))
                .isInstanceOf(UserExceptions.AddressTypeNotFoundException.class);
    }

    @Test
    void getAddresses_shouldReturnUserAddresses() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        userService.addAddress(user.id(), new AddressDto.Request(
                "Street A", "1", "10115", "Berlin", "Germany", "SHIPPING", true
        ));
        userService.addAddress(user.id(), new AddressDto.Request(
                "Street B", "2", "80331", "Munich", "Germany", "BILLING", false
        ));

        List<AddressDto.Response> addresses = userService.getAddresses(user.id());

        assertThat(addresses).hasSize(2);
    }

    @Test
    void setDefaultAddress_shouldUnsetPreviousDefault() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        AddressDto.Response first = userService.addAddress(user.id(), new AddressDto.Request(
                "First St", "1", "10115", "Berlin", "Germany", "SHIPPING", true
        ));
        AddressDto.Response second = userService.addAddress(user.id(), new AddressDto.Request(
                "Second St", "2", "80331", "Munich", "Germany", "BILLING", false
        ));

        AddressDto.Response defaultAddr = userService.setDefaultAddress(user.id(), second.id());

        assertThat(defaultAddr.defaultAddress()).isTrue();
        assertThat(defaultAddr.id()).isEqualTo(second.id());

        AddressDto.Response firstAfter = userService.getAddress(first.id());
        assertThat(firstAfter.defaultAddress()).isFalse();
    }

    @Test
    void removeAddress_shouldRemoveAddressFromUser() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        AddressDto.Response address = userService.addAddress(user.id(), new AddressDto.Request(
                "Main St", "10", "10115", "Berlin", "Germany", "SHIPPING", true
        ));

        userService.removeAddress(address.id());

        assertThat(addressRepository.findById(address.id())).isEmpty();
        assertThat(userService.getAddresses(user.id())).isEmpty();
    }

    @Test
    void addAddress_shouldClearPreviousDefaultWhenAddingNewDefault() {
        seedAddressTypes();
        UserProfileDto.Response user = userService.addUser(validRequest);

        AddressDto.Response first = userService.addAddress(user.id(), new AddressDto.Request(
                "First St", "1", "10115", "Berlin", "Germany", "SHIPPING", true
        ));
        AddressDto.Response second = userService.addAddress(user.id(), new AddressDto.Request(
                "Second St", "2", "80331", "Munich", "Germany", "BILLING", true
        ));

        assertThat(userService.getAddress(first.id()).defaultAddress()).isFalse();
        assertThat(userService.getAddress(second.id()).defaultAddress()).isTrue();
    }

    private void seedAddressTypes() {
        if (addressTypeRepository.count() == 0) {
            addressTypeRepository.save(new AddressType("SHIPPING", "Shipping"));
            addressTypeRepository.save(new AddressType("BILLING", "Billing"));
            addressTypeRepository.save(new AddressType("PRIMARY", "Primary"));
        }
    }
}
