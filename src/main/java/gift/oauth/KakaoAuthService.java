package gift.oauth;

import gift.jwt.JwtResponse;
import gift.jwt.JwtUtil;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.oauth.dto.KakaoLoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KakaoAuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final KakaoOauthClient kakaoOauthClient;

    public KakaoAuthService(
        KakaoOauthClient kakaoOauthClient,
        MemberRepository memberRepository,
        JwtUtil jwtUtil) {
        this.kakaoOauthClient = kakaoOauthClient;
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(readOnly = true)
    public JwtResponse login(KakaoLoginRequest loginRequest) {
        String kakaoAccessToken = kakaoOauthClient.requestToken(loginRequest.code());
        String email = kakaoOauthClient.getUserEmail(kakaoAccessToken);

        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> register(email));

        String accessToken = jwtUtil.createAccessToken(member);

        return new JwtResponse(
            accessToken,
            member.getId()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Member register(String email) {
        Member member = new Member(email);

        return memberRepository.save(member);
    }
}
