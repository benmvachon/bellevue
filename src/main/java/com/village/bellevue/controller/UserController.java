package com.village.bellevue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.config.security.UserDetailsServiceImpl;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.error.AuthorizationException;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserDetailsServiceImpl userService;

    @PostMapping("/signup")
    public ResponseEntity<ScrubbedUserEntity> create(@RequestBody UserEntity user) {
        ScrubbedUserEntity scrubbedUser = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(scrubbedUser);
    }

    @DeleteMapping()
    public ResponseEntity<Void> delete() {
        try {
            userService.delete();
        } catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.noContent().build();
    }
}
