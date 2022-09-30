//  formId의회원을조회해서toId의회원에게money만큼의돈을계좌이체하는로직이다.
//  fromId 회원의돈을money만큼감소한다 -->  UPDATE SQL 실행
//  toId 회원의돈을money만큼증가한다 --> UPDATE SQL 실행
//  예외상황을테스트해보기위해toId가"ex"인경우예외를발생한다.

package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

// 트랜젝션 적용 한 코드(간단하고 깔끔하게 보인다)
@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);
        memberRepository.update(fromId, fromMember.getMoney() - money);
        memberRepository.update(toId, toMember.getMoney() + money);
    }
    //  설명
    //  MemberServiceV1은특정기술에종속적이지않고, 순수한비즈니스로직만존재한다.
    //  특정기술과관련된코드가거의없어서코드가깔끔하고, 유지보수하기쉽다.
    //  향후비즈니스로직의변경이필요하면이부분을변경하면된다.

    //  SQLException이라는 JDBC 기술에의존한다는점이다.
    //  이부분은memberRepository에서올라오는예외이기때문에memberRepository에서해결해야한다. 이부분은뒤에서예외를다룰때알아보자.
    //  MemberRepositoryV1이라는구체클래스에직접의존하고있다.
    //  MemberRepository 인터페이스를 도입하면향후MemberService의코드의변경없이다른구현기술로손쉽게변경할수있다.
}




/** 트렌젝션 적용 전 순수 자바 코드로만 이뤄진 코드!
 * @RequiredArgsConstructor
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
**/
