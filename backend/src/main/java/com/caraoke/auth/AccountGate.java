package com.caraoke.auth;

import java.util.Optional;

/**
 * Whether a signed-in caller may make this request, given where their account stands.
 * Implemented by the user feature; enforced by config.StandingFilter. Empty = allowed.
 */
public interface AccountGate {

    Optional<String> refusal(String authUid, boolean write);
}
