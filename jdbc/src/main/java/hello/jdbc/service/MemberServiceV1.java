package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {
    private final MemberRepositoryV1 memberRepository;

    public void accountTransfar(String fromId, String toId, int money) throws SQLException{
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }
        private void validation(Member toMember) {
         if (toMember.getMemberId().equals("ex")) {
             throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
//  formId의회원을조회해서toId의회원에게money만큼의돈을계좌이체하는로직이다.
//  fromId 회원의돈을money만큼감소한다 -->  UPDATE SQL 실행
//  toId 회원의돈을money만큼증가한다 --> UPDATE SQL 실행
//  예외상황을테스트해보기위해toId가"ex"인경우예외를발생한다.

