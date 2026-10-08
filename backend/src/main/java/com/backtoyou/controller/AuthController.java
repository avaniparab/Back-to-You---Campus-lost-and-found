package com.backtoyou.controller;

import com.backtoyou.dto.AuthResponse;
import com.backtoyou.dto.LoginRequest;
import com.backtoyou.dto.RegisterRequest;
import com.backtoyou.dto.UserDto;
import com.backtoyou.entity.User;
import com.backtoyou.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(AuthResponse.success("Registration successful! You can now log in.", UserDto.fromEntity(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(AuthResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AuthResponse.error("Registration failed due to a server error."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            User user = authService.login(request);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            session.setAttribute("user_id", user.getId());
            session.setAttribute("name", user.getName());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("role", user.getRole().name());

            return ResponseEntity.ok(AuthResponse.success("Login successful", UserDto.fromEntity(user)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.ok(AuthResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(AuthResponse.error("Invalid email or password."));
        }
    }

    @GetMapping("/session-check")
    public ResponseEntity<AuthResponse> sessionCheck(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Integer userId = (Integer) session.getAttribute("user_id");
            String name = (String) session.getAttribute("name");
            String email = (String) session.getAttribute("email");
            String role = (String) session.getAttribute("role");

            if (userId != null && name != null && email != null && role != null) {
                UserDto userDto = new UserDto(userId, name, email, role);
                return ResponseEntity.ok(AuthResponse.sessionCheck(true, userDto));
            }
        }

        return ResponseEntity.ok(AuthResponse.sessionCheck(false, null));
    }
}
