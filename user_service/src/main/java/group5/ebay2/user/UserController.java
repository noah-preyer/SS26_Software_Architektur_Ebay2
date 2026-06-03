package group5.ebay2.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

@RestController
@SpringBootApplication
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    private final UserService service;

    public UserController(
            UserService userService
    ) {
        this.service = userService;
    }
    // dto::AddUserDto.java
    @PostMapping("/add_user")
    public AddUserDto.Response addUser(@Valid @RequestBody AddUserDto.Request request){
        log.info("recieved AddUser Request");
        return service.addUser(request);
    }



    public static void main(String[] args) {
        SpringApplication.run(UserController.class, args);
    }

}