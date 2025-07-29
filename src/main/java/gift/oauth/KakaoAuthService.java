package gift.oauth;

import gift.jwt.JwtResponse;
import gift.jwt.JwtUtil;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import gift.oauth.dto.KakaoLoginRequest;
import org.springframework.stereotype.Service;
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

    @Transactional
    public JwtResponse login(KakaoLoginRequest loginRequest) {
        String kakaoAccessToken = kakaoOauthClient.requestToken(loginRequest.code());
        String email = kakaoOauthClient.extractEmailFromResponse(kakaoAccessToken);

        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> register(email, kakaoAccessToken));

        String accessToken = jwtUtil.createAccessToken(member);

        return new JwtResponse(
            accessToken,
            member.getId()
        );
    }

    private Member register(String email, String kakaoAccessToken) {
        Member member = new Member(email, kakaoAccessToken);

        return memberRepository.save(member);
    }
}
