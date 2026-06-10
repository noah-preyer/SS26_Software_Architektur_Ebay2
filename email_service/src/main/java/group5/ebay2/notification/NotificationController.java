package group5.ebay2.notification;

import group5.ebay2.notification.dtos.EmailDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notification/send")
    public ResponseEntity<EmailDto.Response> sendEmail(
            @Valid @RequestBody EmailDto.SendRequest request) {
        EmailDto.Response response = notificationService.sendEmail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/notification/{id}")
    public ResponseEntity<EmailDto.Response> getNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getNotification(id));
    }
}
