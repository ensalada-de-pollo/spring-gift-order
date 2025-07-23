package gift;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.member.domain.Member;
import gift.member.domain.enums.UserRole;
import gift.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버_저장_테스트() {
        Member member = new Member(
            "asdf@gmail.com",
            "passwd",
            UserRole.NORMAL
        );

        Member savedMember = memberRepository.save(member);

        assertAll(
            () -> assertThat(savedMember.getId()).isNotNull(),
            () -> assertThat(savedMember.getEmail()).isEqualTo(member.getEmail()),
            () -> assertThat(savedMember.getPassword()).isEqualTo(member.getPassword()),
            () -> assertThat(savedMember.getUserRole()).isEqualTo(member.getUserRole())
        );
    }

    @Test
    void email로_멤버를_조회() {
        String email = "asdf@gmail.com";
        memberRepository.save(new Member(email, "passwd", UserRole.NORMAL));

        String foundEmail = memberRepository.findByEmail(email).get().getEmail();

        assertThat(foundEmail).isEqualTo(email);
    }


}