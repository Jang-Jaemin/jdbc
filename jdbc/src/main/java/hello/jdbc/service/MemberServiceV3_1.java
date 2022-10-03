package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 *  트랜잭션 - 트랜잭션 매니져
**/

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;
    
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        //비즈니스 로직
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); //성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); //실패시 롤백
            throw new IllegalStateException(e);
        }
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
            throw new IllegalStateException("이체중 예외 발생"); }
    }
}

//  private final PlatformTransactionManager transactionManager :
//      트랜잭션매니저를주입받는다. 지금은 JDBC 기술을사용하기때문에 DataSourceTransactionManager 구현체를주입받아야한다.
//      물론 JPA 같은기술로변경되면JpaTransactionManager를주입받으면된다.
//  transactionManager.getTransaction() : 트랜잭션을시작한다.
//      TransactionStatus status를반환한다. 현재트랜잭션의상태정보가포함되어있다. 이후 트랜잭션을커밋, 롤백할때필요하다.
//  new DefaultTransactionDefinition()
//      트랜잭션과관련된옵션을지정할수있다. 자세한내용은뒤에서설명한다.
//  transactionManager.commit(status)
//      트랜잭션이성공하면이로직을호출해서커밋하면된다.
//  transactionManager.rollback(status)
//      문제가발생하면이로직을호출해서트랜잭션을롤백하면된다.

