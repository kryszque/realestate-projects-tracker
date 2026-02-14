package com.mcdevka.realestate_projects_tracker.domain.user;

import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDetail> getMyProfile(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal automatycznie wstrzykuje obiekt zalogowanego użytkownika
        return ResponseEntity.ok(userService.getUserDetails(user.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDetail> updateMyProfile(
            @AuthenticationPrincipal User currentUser, // To jest user z sesji/tokena (ID)
            @RequestBody User requestData          // To są dane z formularza (JSON)
    ) {
        try {
            // Przekazujemy ID zalogowanego usera oraz dane z formularza
            UserDetail updatedUser = userService.updateCurrentUser(currentUser.getId(), requestData);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}