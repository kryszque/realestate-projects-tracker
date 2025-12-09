package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users/{userId}/company")
    public ResponseEntity<Void> assignUserToCompany(@PathVariable long userId,
                                                    @RequestBody AssignCompanyRequest request){
        adminService.assignUserToCompany(request, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<Void> grantUserPermissions(@PathVariable long userId,
                                                     @RequestBody GrantPermissionsRequest request){
        adminService.grantUserPermissions(request, userId);
        return ResponseEntity.ok().build();
    }
}
