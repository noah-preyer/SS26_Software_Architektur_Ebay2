package group5.ebay2.notification.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EmailDto {

    public record SendRequest(

            @NotBlank(message = "Recipient email is required")
            @Email(message = "Recipient email must be valid")
            String recipientEmail,

            @NotBlank(message = "Template code is required")
            String templateCode,

            Map<String, String> placeholders

    ) {
    }

    public record Response(
            UUID id,
            String recipientEmail,
            String subject,
            String body,
            String status,
            Instant sentAt,
            String templateCode
    ) {
    }
}
