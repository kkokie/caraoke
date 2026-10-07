package com.caraoke.user;

import com.caraoke.user.UserDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService service;
    private final AvatarService avatars;

    public UserController(UserService service, AvatarService avatars) {
        this.service = service;
        this.avatars = avatars;
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

    /** Profile photo upload (multipart field "file"). The app resizes to ~512px first. */
    @PutMapping(path = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PublicProfile setAvatar(Authentication auth, @RequestParam("file") MultipartFile file) throws IOException {
        return avatars.set(auth.getName(), file.getBytes());
    }

    @DeleteMapping("/me/avatar")
    public PublicProfile removeAvatar(Authentication auth) {
        return avatars.remove(auth.getName());
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
