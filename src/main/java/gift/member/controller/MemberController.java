package gift.member.controller;

import gift.jwt.JwtResponse;
import gift.member.dto.LogInRequest;
import gift.member.dto.MemberResponse;
import gift.member.dto.RegisterRequest;
import gift.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(
        @Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(memberService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> logIn(
        @Valid @RequestBody LogInRequest loginRequest) {
        return ResponseEntity.ok(memberService.logIn(loginRequest));
    }

    @GetMapping
    public ResponseEntity<MemberResponse> getCurrentUser(HttpServletRequest request) {
        String token = (String) request.getAttribute("resolvedToken");

        return ResponseEntity.ok(memberService.getCurrentUser(token));
    }
}
