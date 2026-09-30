package com.caraoke.user;

import com.caraoke.user.UserDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ---- The signed-in user ------------------------------------------------
    // auth.getName() is the Firebase uid (JWT "sub"), or the X-Dev-User value in dev mode.

    @GetMapping("/me")
    public PublicProfile me(Authentication auth) {
        return service.getMe(auth.getName());
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicProfile createProfile(Authentication auth, @Valid @RequestBody CreateProfileRequest req) {
        return service.createProfile(auth.getName(), req);
    }

    @PatchMapping("/me")
    public PublicProfile updateProfile(Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        return service.updateProfile(auth.getName(), req);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Authentication auth) {
        service.deleteAccount(auth.getName());
    }

    // ---- Public ------------------------------------------------------------

    @GetMapping("/users/{handle}")
    public PublicProfile profile(@PathVariable String handle) {
        return service.getByHandle(handle);
    }

    @GetMapping("/handles/{handle}/available")
    public HandleAvailability handleAvailable(@PathVariable String handle) {
        return service.checkHandle(handle);
    }
}
