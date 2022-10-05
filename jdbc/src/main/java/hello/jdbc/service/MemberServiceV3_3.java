package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;


/**
 *  트랜잭션 - @Transactional AOP
**/

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {


    private final MemberRepositoryV3 memberRepository;

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        bizLogic(fromId, toId, money);
    }
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
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


// * 순수한 비즈니스 로직만 남기고, 트랜잭션 관련 코드는 모두 제거함.
//  스프링이제공하는트랜잭션 AOP를적용하기위해@Transactional 애노테이션을추가했다.
//  @Transactional 애노테이션은메서드에붙여도되고, 클래스에붙여도된다.
//  클래스에붙이면외부에서 호출가능한public 메서드가 AOP 적용대상이된다