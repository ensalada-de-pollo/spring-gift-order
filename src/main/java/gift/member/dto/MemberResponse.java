package gift.member.dto;

import gift.member.domain.enums.UserRole;

public record MemberResponse(Long id, String email, UserRole userRole) {

}
