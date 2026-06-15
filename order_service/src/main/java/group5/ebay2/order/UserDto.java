package group5.ebay2.order;

import java.util.UUID;

public record UserDto(
        UUID id,
        UUID authUserId,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileImageObjectKey
) {
}
