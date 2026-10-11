package com.caraoke.user;

import com.caraoke.user.UserDtos.AccountStanding;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StandingController {

    private final UserStandingService standing;

    public StandingController(UserStandingService standing) {
        this.standing = standing;
    }

    /** Private to you: GOOD / WARNED / SUSPENDED (until) / BANNED, and whether you moderate. */
    @GetMapping("/me/standing")
    public AccountStanding mine(Authentication auth) {
        return standing.mine(auth.getName());
    }
}
