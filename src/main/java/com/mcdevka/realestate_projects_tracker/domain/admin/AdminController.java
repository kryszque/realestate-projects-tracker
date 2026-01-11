package com.mcdevka.realestate_projects_tracker.domain.admin;

import com.mcdevka.realestate_projects_tracker.domain.admin.dto.AssignCompanyRequest;
import com.mcdevka.realestate_projects_tracker.domain.admin.dto.GrantPermissionsRequest;
import com.mcdevka.realestate_projects_tracker.domain.user.User;
import com.mcdevka.realestate_projects_tracker.domain.user.UserService;
import com.mcdevka.realestate_projects_tracker.domain.user.dto.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @PostMapping("/users/{userId}/company/add")
    public ResponseEntity<Void> addCompanyToUser(@PathVariable long userId,
                                                    @RequestBody AssignCompanyRequest request){
        try{
            adminService.addCompanyToUser(request, userId);
            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/users/{userId}/company/delete")
    public ResponseEntity<Void> deleteCompanyFromUser(@PathVariable long userId,
                                                 @RequestBody AssignCompanyRequest request){
        try{
            adminService.deleteCompanyFromUser(request, userId);
            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/users/{userId}/permissions")
    public ResponseEntity<Void> grantUserPermissions(@PathVariable long userId,
                                                     @RequestBody GrantPermissionsRequest request){
        try {
            adminService.grantUserPermissions(request, userId);
            return ResponseEntity.ok().build();
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        try {
            List<User> allUsers = adminService.getAllUsers();
            return  ResponseEntity.ok(allUsers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/users/{userId}/delete")
    public ResponseEntity<User> deleteUser(@PathVariable long userId){
        try {
            User deletedUser = adminService.deleteUser(userId);
            return ResponseEntity.ok(deletedUser);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDetail> getUserDetails(@PathVariable long userId){
        try{
            UserDetail userDetail = userService.getUserDetails(userId);
            return ResponseEntity.ok(userDetail);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
