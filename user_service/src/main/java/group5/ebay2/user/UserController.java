package group5.ebay2.user;

import group5.ebay2.user.dtos.AddressDto;
import group5.ebay2.user.dtos.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserProfileDto.Response> addUser(
            @Valid @RequestBody UserProfileDto.Request request) {
        UserProfileDto.Response response = userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto.Response> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/by-auth/{authUserId}")
    public ResponseEntity<UserProfileDto.Response> getUserByAuthUserId(
            @PathVariable String authUserId) {
        try {
            Long id = Long.valueOf(authUserId);
            return ResponseEntity.ok(userService.getUserByAuthUserId(id));
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserProfileDto.Response> getUserByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserProfileDto.Response> getUserByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto.Response> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileDto.UpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/address")
    public ResponseEntity<AddressDto.Response> addAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressDto.Request request) {
        AddressDto.Response response = userService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<AddressDto.Response>> getAddresses(
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getAddresses(userId));
    }

    @PutMapping("/{userId}/address/{addressId}/default")
    public ResponseEntity<AddressDto.Response> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        return ResponseEntity.ok(userService.setDefaultAddress(userId, addressId));
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<AddressDto.Response> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDto.Request request) {
        return ResponseEntity.ok(userService.updateAddress(addressId, request));
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<Void> removeAddress(@PathVariable Long addressId) {
        userService.removeAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<AddressDto.Response> getAddress(
            @PathVariable Long addressId) {
        return ResponseEntity.ok(userService.getAddress(addressId));
    }
}