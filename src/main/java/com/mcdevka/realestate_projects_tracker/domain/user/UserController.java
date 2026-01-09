package com.mcdevka.realestate_projects_tracker.domain.user;

import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}