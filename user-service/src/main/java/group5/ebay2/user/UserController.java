package group5.ebay2.user;

import group5.ebay2.user.dtos.AddUserDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public AddUserDto.Response addUser(@Valid @RequestBody AddUserDto.Request request) {
        log.info("Received AddUser request");
        return service.addUser(request);
    }
}
