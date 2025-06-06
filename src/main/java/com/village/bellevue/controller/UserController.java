package com.village.bellevue.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.village.bellevue.assembler.ProfileModelAssembler;
import com.village.bellevue.config.security.UserDetailsImpl;
import com.village.bellevue.config.security.UserDetailsServiceImpl;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.ProfileEntity.LocationType;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api/user")
public class UserController {

  private final UserDetailsServiceImpl userService;
  private final UserProfileService profileService;
  private final ProfileModelAssembler profileModelAssembler;
  private final PagedResourcesAssembler<ProfileModel> pagedAssembler;
  private final AuthenticationManager authManager;

  public UserController(
    UserDetailsServiceImpl userService,
    UserProfileService profileService,
    ProfileModelAssembler profileModelAssembler,
    PagedResourcesAssembler<ProfileModel> pagedAssembler,
    AuthenticationManager authManager
  ) {
    this.userService = userService;
    this.profileService = profileService;
    this.profileModelAssembler = profileModelAssembler;
    this.pagedAssembler = pagedAssembler;
    this.authManager = authManager;
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(
    @RequestBody UserEntity loginRequest,
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    try {
      // Invalidate old session if present
      HttpSession oldSession = request.getSession(false);
      if (oldSession != null) {
        oldSession.invalidate(); // Required to force a new session ID
      }

      // Manually authenticate using the AuthenticationManager
      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
      Authentication authResult = authManager.authenticate(token);

      // Set the authenticated user in the security context
      SecurityContextHolder.getContext().setAuthentication(authResult);

      // Create a new session and store the context there
      HttpSession newSession = request.getSession(true); // Force creation of a new session
      newSession.setAttribute(
          HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
          SecurityContextHolder.getContext()
      );

      // Return user ID or any other response
      Long userId = ((UserDetailsImpl) authResult.getPrincipal()).getId();
      return ResponseEntity.ok(userId.toString());

    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
    }
  }

  @PostMapping("/signup")
  public ResponseEntity<EntityModel<ProfileModel>> create(@RequestBody UserEntity user) {
    try {
      ProfileModel profile = userService.create(user);
      EntityModel<ProfileModel> entityModel = profileModelAssembler.toModel(profile);
      return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PutMapping("/blackboard")
  public ResponseEntity<Void> create(@RequestBody String blackboard) {
    profileService.setBlackboard(blackboard);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{user}")
  public ResponseEntity<EntityModel<ProfileModel>> read(@PathVariable Long user) {
    try {
      Optional<ProfileModel> profile = profileService.read(user);
      return profile
        .map(model -> ResponseEntity.ok(profileModelAssembler.toModel(model)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    } catch (FriendshipException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @GetMapping("/search/{prefix}")
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> search(
    @PathVariable String prefix,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size
  ) {
    Page<ProfileModel> users = profileService.readByNamePrefix(prefix, page, size);
    PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(users, profileModelAssembler);
    return ResponseEntity.ok(pagedModel);
  }

  @GetMapping("/location/friends")
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> friends(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size
  ) {
    try {
      Page<ProfileModel> users = profileService.readFriendsByLocation(page, size);
      PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(users, profileModelAssembler);
      return ResponseEntity.ok(pagedModel);
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @GetMapping("/location/nonfriends")
  public ResponseEntity<PagedModel<EntityModel<ProfileModel>>> nonFriends(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size
  ) {
    Page<ProfileModel> users = profileService.readNonFriendsByLocation(page, size);
    PagedModel<EntityModel<ProfileModel>> pagedModel = pagedAssembler.toModel(users, profileModelAssembler);
    return ResponseEntity.ok(pagedModel);
  }

  @PutMapping(value = {"/location", "/location/{locationType}/{location}"})
  public ResponseEntity<Void> setLocation(
    @PathVariable(required = false) String locationType,
    @PathVariable(required = false) Long location
  ) {
    try {
      profileService.setLocation(location, LocationType.fromString(locationType));
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  @DeleteMapping
  public ResponseEntity<Void> delete() {
    try {
      userService.delete();
    } catch (AuthorizationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    return ResponseEntity.noContent().build();
  }
}
