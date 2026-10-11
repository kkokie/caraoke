package com.caraoke.user;

import com.caraoke.media.MediaStorage;
import com.caraoke.user.UserDtos.CreateProfileRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SignupConsentTest {

    private final UserRepository users = mock(UserRepository.class);
    private final UserService service = new UserService(users, mock(MediaStorage.class));

    @Test
    void under13OrNoConsentCantSignUp() {
        when(users.findByAuthUid("uid")).thenReturn(Optional.empty());

        assertBadRequest(new CreateProfileRequest("ian", "Ian", null, true, false));
        assertBadRequest(new CreateProfileRequest("ian", "Ian", null, false, true));
        assertBadRequest(new CreateProfileRequest("ian", "Ian", null));          // old clients
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void consentIsRecordedWithTheTermsVersion() {
        when(users.findByAuthUid("uid")).thenReturn(Optional.empty());
        when(users.existsByHandle("ian")).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createProfile("uid", new CreateProfileRequest("ian", "Ian", null, true, true));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getTermsVersion()).isEqualTo(LegalTerms.CURRENT_VERSION);
        assertThat(saved.getValue().getTermsAcceptedAt()).isNotNull();
    }

    private void assertBadRequest(CreateProfileRequest req) {
        assertThatThrownBy(() -> service.createProfile("uid", req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }
}
