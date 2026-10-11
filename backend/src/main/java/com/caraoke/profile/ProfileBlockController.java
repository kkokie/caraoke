package com.caraoke.profile;

import com.caraoke.profile.ProfileDtos.BlockedAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Block and unblock are idempotent; both return 204. */
@RestController
@RequestMapping("/api")
public class ProfileBlockController {

    private final ProfileBlockService service;

    public ProfileBlockController(ProfileBlockService service) {
        this.service = service;
    }

    @PutMapping("/profiles/{handle}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(Authentication auth, @PathVariable String handle) {
        service.block(auth.getName(), handle);
    }

    @DeleteMapping("/profiles/{handle}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(Authentication auth, @PathVariable String handle) {
        service.unblock(auth.getName(), handle);
    }

    @GetMapping("/me/blocked")
    public List<BlockedAccount> blocked(Authentication auth) {
        return service.blockedAccounts(auth.getName());
    }
}
