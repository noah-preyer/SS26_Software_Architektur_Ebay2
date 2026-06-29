package group5.ebay2.user;

import group5.ebay2.user.dtos.AddressDto;
import group5.ebay2.user.dtos.UserProfileDto;
import group5.ebay2.user.repositories.AddressRepository;
import group5.ebay2.user.repositories.AddressTypeRepository;
import group5.ebay2.user.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final AddressTypeRepository addressTypeRepository;
    private final AuthServiceClient authServiceClient;

    public UserService(UserProfileRepository userProfileRepository,
                       AddressRepository addressRepository,
                       AddressTypeRepository addressTypeRepository,
                       AuthServiceClient authServiceClient) {
        this.userProfileRepository = userProfileRepository;
        this.addressRepository = addressRepository;
        this.addressTypeRepository = addressTypeRepository;
        this.authServiceClient = authServiceClient;
    }

    @Transactional
    public UserProfileDto.Response addUser(UserProfileDto.Request request) {
        log.info("Creating user with email: {}", request.email());

        if (userProfileRepository.existsByEmail(request.email())) {
            throw new UserExceptions.UserAlreadyExistsException(
                    "Email already exists: " + request.email()
            );
        }

        if (userProfileRepository.existsByUsername(request.username())) {
            throw new UserExceptions.UserAlreadyExistsException(
                    "Username already exists: " + request.username()
            );
        }

        AuthServiceClient.AuthUser authUser;
        try {
            authUser = authServiceClient.createUser(
                    request.username(), request.email(), request.password());
        } catch (Exception e) {
            log.error("Failed to create auth user: {}", e.getMessage());
            throw new RuntimeException("Registration failed: could not create auth user");
        }

        UUID authUserId = authUser.id();

        try {
            UserProfile userProfile = new UserProfile(authUserId, request.username(), request.email());
            userProfile.updateProfile(
                    request.firstName(),
                    request.lastName(),
                    request.phoneNumber()
            );
            if (request.profileImageObjectKey() != null) {
                userProfile.updateProfileImage(request.profileImageObjectKey());
            }

            UserProfile saved = userProfileRepository.save(userProfile);

            if (request.addressStreet() != null && !request.addressStreet().isBlank()) {
                AddressType addressType = addressTypeRepository.findByCodeAndActiveTrue("SHIPPING")
                        .orElse(null);
                if (addressType != null) {
                    Address address = new Address(
                            request.addressStreet(),
                            request.addressHouseNumber(),
                            request.addressPostalCode(),
                            request.addressCity(),
                            request.addressCountry(),
                            addressType,
                            true
                    );
                    saved.addAddress(address);
                    addressRepository.save(address);
                }
            }

            log.info("Created user with id: {}, authUserId: {}", saved.getId(), saved.getAuthUserId());

            return toResponse(saved);
        } catch (Exception e) {
            log.error("Failed to save user profile, rolling back auth user: {}", e.getMessage());
            try {
                authServiceClient.deleteUser(authUserId);
            } catch (Exception ex) {
                log.error("Failed to delete auth user {} during rollback: {}", authUserId, ex.getMessage());
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserProfileDto.Response getUser(UUID id) {
        return toResponse(findUserById(id));
    }

    @Transactional(readOnly = true)
    public UserProfileDto.Response getUserByAuthUserId(UUID authUserId) {
        return toResponse(userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException(
                        "User not found with authUserId: " + authUserId)));
    }

    @Transactional(readOnly = true)
    public UserProfileDto.Response getUserByEmail(String email) {
        return toResponse(userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException(
                        "User not found with email: " + email)));
    }

    @Transactional(readOnly = true)
    public UserProfileDto.Response getUserByUsername(String username) {
        return toResponse(userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException(
                        "User not found with username: " + username)));
    }

    @Transactional
    public UserProfileDto.Response updateUser(UUID id, UserProfileDto.UpdateRequest request) {
        UserProfile user = findUserById(id);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userProfileRepository.existsByUsername(request.username())) {
                throw new UserExceptions.UserAlreadyExistsException(
                        "Username already exists: " + request.username()
                );
            }
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userProfileRepository.existsByEmail(request.email())) {
                throw new UserExceptions.UserAlreadyExistsException(
                        "Email already exists: " + request.email()
                );
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null || request.lastName() != null || request.phoneNumber() != null) {
            user.updateProfile(
                    request.firstName() != null ? request.firstName() : user.getFirstName(),
                    request.lastName() != null ? request.lastName() : user.getLastName(),
                    request.phoneNumber() != null ? request.phoneNumber() : user.getPhoneNumber()
            );
        }

        if (request.profileImageObjectKey() != null) {
            user.updateProfileImage(request.profileImageObjectKey());
        }

        UserProfile saved = userProfileRepository.save(user);
        log.info("Updated user with id: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteUser(UUID id) {
        UserProfile user = findUserById(id);
        userProfileRepository.delete(user);
        log.info("Deleted user with id: {}", id);
    }

    @Transactional
    public AddressDto.Response addAddress(UUID userId, AddressDto.Request request) {
        UserProfile user = findUserById(userId);
        AddressType addressType = findAddressType(request.addressTypeCode());

        Address address = new Address(
                request.street(),
                request.houseNumber(),
                request.postalCode(),
                request.city(),
                request.country(),
                addressType,
                request.defaultAddress()
        );

        if (request.defaultAddress()) {
            clearDefaultAddresses(user);
        }

        user.addAddress(address);
        Address saved = addressRepository.save(address);
        log.info("Added address id: {} to user id: {}", saved.getId(), userId);

        return toResponse(saved);
    }

    @Transactional
    public AddressDto.Response updateAddress(UUID addressId, AddressDto.Request request) {
        Address address = findAddressById(addressId);
        AddressType addressType = findAddressType(request.addressTypeCode());

        address.update(
                request.street(),
                request.houseNumber(),
                request.postalCode(),
                request.city(),
                request.country(),
                addressType,
                request.defaultAddress()
        );

        if (request.defaultAddress()) {
            clearDefaultAddresses(address.getUserProfile());
        }

        Address saved = addressRepository.save(address);
        log.info("Updated address id: {}", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public void removeAddress(UUID addressId) {
        Address address = findAddressById(addressId);
        address.getUserProfile().removeAddress(address);
        addressRepository.delete(address);
        log.info("Removed address id: {}", addressId);
    }

    @Transactional
    public AddressDto.Response setDefaultAddress(UUID userId, UUID addressId) {
        UserProfile user = findUserById(userId);
        Address address = findAddressById(addressId);

        if (!address.getUserProfile().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        clearDefaultAddresses(user);
        address.update(
                address.getStreet(),
                address.getHouseNumber(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry(),
                address.getAddressType(),
                true
        );

        Address saved = addressRepository.save(address);
        log.info("Set default address id: {} for user id: {}", addressId, userId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressDto.Response> getAddresses(UUID userId) {
        findUserById(userId);
        return addressRepository.findByUserProfileId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AddressDto.Response getAddress(UUID addressId) {
        return toResponse(findAddressById(addressId));
    }

    private UserProfile findUserById(UUID id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new UserExceptions.UserNotFoundException("User not found: " + id));
    }

    private Address findAddressById(UUID id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new UserExceptions.AddressNotFoundException("Address not found: " + id));
    }

    private AddressType findAddressType(String code) {
        return addressTypeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new UserExceptions.AddressTypeNotFoundException(
                        "Address type not found or inactive: " + code));
    }

    private void clearDefaultAddresses(UserProfile user) {
        user.getAddresses().stream()
                .filter(Address::isDefaultAddress)
                .forEach(a -> a.update(
                        a.getStreet(), a.getHouseNumber(), a.getPostalCode(),
                        a.getCity(), a.getCountry(), a.getAddressType(), false
                ));
    }

    private UserProfileDto.Response toResponse(UserProfile user) {
        AuthServiceClient.AuthUser authUser = null;
        try {
            authUser = authServiceClient.getUser(user.getAuthUserId());
        } catch (Exception e) {
            log.warn("Failed to fetch auth data for authUserId={}: {}", user.getAuthUserId(), e.getMessage());
        }

        return new UserProfileDto.Response(
                user.getId(),
                user.getAuthUserId(),
                authUser != null ? authUser.username() : user.getUsername(),
                authUser != null ? authUser.email() : user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getProfileImageObjectKey()
        );
    }

    private AddressDto.Response toResponse(Address address) {
        return new AddressDto.Response(
                address.getId(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry(),
                address.getAddressType().getCode(),
                address.getAddressType().getDisplayName(),
                address.isDefaultAddress()
        );
    }
}
