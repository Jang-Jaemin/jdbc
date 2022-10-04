package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;


/**
 *  트랜잭션 - 트랜잭션 템플릿
**/

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_2 {

   // private final PlatformTransactionManager transactionManager;
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }
    
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        txTemplate.executeWithoutResult((status) -> {
            try {
                //비즈니스 로직
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e); }
        });
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

//  TransactionTemplate을사용하려면transactionManager가필요하다.
//  생성자에서 transactionManager를 주입 받으면서 TransactionTemplate을 생성했다.

// 템플릿 사용 로직 :
//  트랜잭션템플릿덕분에트랜잭션을시작하고, 커밋하거나롤백하는코드가모두제거되었다.
//  트랜잭션템플릿의기본동작은다음과같다.
//  비즈니스로직이정상수행되면커밋한다.
//  언체크예외가발생하면롤백한다. 그외의경우커밋한다. (체크예외의경우에는커밋하는데, 이 부분은뒤에서설명한다.
//  코드에서예외를처리하기위해try~catch가들어갔는데, bizLogic() 메서드를호출하면 SQLException 체크예외를넘겨준다.
//  해당람다에서체크예외를밖으로던질수없기때문에언체크 예외로바꾸어던지도록예외를전환했다.


