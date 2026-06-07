package group5.ebay2.user;

import group5.ebay2.user.dtos.AddUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public AddUserDto.Response addUser(AddUserDto.Request request) {
        log.info("Creating user with email: {}", request.email());
        return new AddUserDto.Response(1L, request.username(), request.email());
    }
}
