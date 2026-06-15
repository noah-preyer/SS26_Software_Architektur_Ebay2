package group5.ebay2.user;

import group5.ebay2.user.dtos.AddressDto;
import group5.ebay2.user.dtos.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserProfileDto.Response> addUser(
            @Valid @RequestBody UserProfileDto.Request request) {
        UserProfileDto.Response response = userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserProfileDto.Response> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/user/by-auth/{authUserId}")
    public ResponseEntity<UserProfileDto.Response> getUserByAuthUserId(
            @PathVariable UUID authUserId) {
        return ResponseEntity.ok(userService.getUserByAuthUserId(authUserId));
    }

    @GetMapping("/user/by-email/{email}")
    public ResponseEntity<UserProfileDto.Response> getUserByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/user/by-username/{username}")
    public ResponseEntity<UserProfileDto.Response> getUserByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserProfileDto.Response> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileDto.UpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{userId}/address")
    public ResponseEntity<AddressDto.Response> addAddress(
            @PathVariable UUID userId,
            @Valid @RequestBody AddressDto.Request request) {
        AddressDto.Response response = userService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDto.Response> updateAddress(
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressDto.Request request) {
        return ResponseEntity.ok(userService.updateAddress(addressId, request));
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<Void> removeAddress(@PathVariable UUID addressId) {
        userService.removeAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/user/{userId}/address/{addressId}/default")
    public ResponseEntity<AddressDto.Response> setDefaultAddress(
            @PathVariable UUID userId,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(userService.setDefaultAddress(userId, addressId));
    }

    @GetMapping("/user/{userId}/addresses")
    public ResponseEntity<List<AddressDto.Response>> getAddresses(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getAddresses(userId));
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDto.Response> getAddress(
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(userService.getAddress(addressId));
    }
}