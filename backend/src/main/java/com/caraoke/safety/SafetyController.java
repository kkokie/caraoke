package com.caraoke.safety;

import com.caraoke.safety.SafetyDtos.ReportRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SafetyController {

    private final SafetyService service;

    public SafetyController(SafetyService service) {
        this.service = service;
    }

    /** Report a story or a person. Reporting the same thing twice is fine (counted once). */
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void report(Authentication auth, @Valid @RequestBody ReportRequest req) {
        service.report(auth.getName(), req);
    }
}
