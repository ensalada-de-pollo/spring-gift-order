package gift.member.service;

import gift.common.PasswordEncoder;
import gift.common.exceptions.AlreadyExistsException;
import gift.common.exceptions.FailedToFindException;
import gift.common.exceptions.LogInFailedException;
import gift.jwt.JwtResponse;
import gift.jwt.JwtUtil;
import gift.member.domain.Member;
import gift.member.domain.enums.UserRole;
import gift.member.dto.LogInRequest;
import gift.member.dto.MemberResponse;
import gift.member.dto.RegisterRequest;
import gift.member.repository.MemberRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public MemberService(
        PasswordEncoder passwordEncoder,
        JwtUtil jwtUtil,
        MemberRepository memberRepository
    ) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.email();

        Optional<Member> checkExists = memberRepository.findByEmail(email);

        if (checkExists.isPresent()) {
            throw new AlreadyExistsException("이미 존재하는 사용자입니다.");
        }

        String encryptedPassword = passwordEncoder.encrypt(email, registerRequest.password());
        UserRole userRole = UserRole.NORMAL;

        if (registerRequest.userRole() != null) {
            userRole = registerRequest.userRole();
        }

        Member member = memberRepository.save(
            new Member(
                email,
                encryptedPassword,
                userRole
            )
        );

        String accessToken = jwtUtil.createAccessToken(member);

        return new JwtResponse(
            accessToken,
            member.getId()
        );
    }

    @Transactional
    public JwtResponse logIn(LogInRequest loginRequest) {
        String email = loginRequest.email();
        String encryptedPassword = passwordEncoder.encrypt(email, loginRequest.password());

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(LogInFailedException::new);

        if (!member.getPassword().equals(encryptedPassword)) {
            throw new LogInFailedException();
        }

        String accessToken = jwtUtil.createAccessToken(member);

        return new JwtResponse(
            accessToken,
            member.getId()
        );
    }

    @Transactional(readOnly = true)
    public MemberResponse getCurrentUser(String token) {
        Long id = jwtUtil.getIdFromToken(token);

        return convertToDTO(
            memberRepository.findById(id)
                .orElseThrow(() -> new FailedToFindException("존재하지 않는 회원입니다."))
        );
    }

    public Long getIdFromToken(String token) {
        return jwtUtil.getIdFromToken(token);
    }

    private MemberResponse convertToDTO(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getEmail(),
            member.getUserRole()
        );
    }
}