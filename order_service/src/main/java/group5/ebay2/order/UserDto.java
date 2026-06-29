package group5.ebay2.order;

public record UserDto(
        Long id,
        Long authUserId,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileImageObjectKey
) {
}
