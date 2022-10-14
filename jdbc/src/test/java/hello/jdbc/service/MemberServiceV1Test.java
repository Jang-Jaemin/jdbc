package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class MemberServiceV1Test {

    /**
     * 기본 동작, 트랜잭션이 없어서 문제 발생  */
    class MemberServiceV1Test {
        public static final String MEMBER_A = "memberA";
        public static final String MEMBER_B = "memberB";
        public static final String MEMBER_EX = "ex";
        private MemberRepositoryV1 memberRepository; private MemberServiceV1 memberService;

        @BeforeEach
        void before(){
            DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,USERNAME,PASSWORD);
            memberRepository = new MemberRepositoryV1(dataSource);
            memberService = new MemberServiceV1(memberService);
    }

    @AfterEach
        void after() throws SQLException{
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
        }

        @Test
        @DisplayName("정상이체")
        void accountTransfer() throws SQLException{
            Member memberA = new Member();
            Member memberB = new Member();
            memberRepository.save(memberA);
            memberRepository.save(memberB);

            //when
            memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

            //then
            Member findMemberA = memberRepository.findById(memberA.getMemberId());
            Member findMemberB = memberRepository.findById(memberB.getMemberId());
            assertThat(findMemberA.getMoney()).isEqualTo(8000);
            assertThat(findMemberB.getMoney()).isEqualTo(12000);
        }
        
        @Test
        @DisplayName("이제충 예외 발생")
        void accountTransferEx() throws SQLException{
            //given
            Member memberA = new Member();
            Member memberEx = new Member();
            memberRepository.save(memberA);
            memberRepository.save(memberEx);

            //when
            assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                    .isInstanceOf(IllegalStateException.class);

            //then
            Member findMemberA = memberRepository.findById(memberA.getMemberId());
            Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

            //memberA의 돈만 2000원 줄었고, ex의 돈은 10000원 그대로이다.
            assertThat(findMemberA.getMoney()).isEqualTo(8000);
            assertThat(findMemberEx.getMoney()).isEqualTo(10000);
        }
    }
}

//  주의 : 테스트를수행하기전에데이터베이스의데이터를삭제해야한다.
//  delete from member;

//  정상이체 - accountTransfer()
//  given: 다음데이터를저장해서테스트를준비한다.
//      memberA 10000원
//      memberB 10000원
//  when: 계좌이체로직을실행한다.
//      memberService.accountTransfer()를실행한다.
//      memberA   memberB로 2000원계좌이체한다.
//        memberA의금액이 2000원감소한다.
//        memberB의금액이 2000원증가한다.
//  then: 계좌이체가정상수행되었는지검증한다.
//       memberA 8000원 - 2000원감소
//       memberB 12000원 - 2000원증가

//  테스트데이터제거
//  테스트가끝나면다음테스트에영향을주지않기위해@AfterEach에서테스트에사용한데이터를모두 삭제한다.
//  @BeforeEach: 각각의테스트가수행되기전에실행된다.
//  @AfterEach: 각각의테스트가실행되고난이후에실행된다.

//  테스트데이터를제거하는과정이불편하지만, 다음테스트에영향을주지않으려면테스트에서사용한 데이터를모두제거해야한다.
//  그렇지않으면이번테스트에서사용한데이터때문에다음테스트에서 데이터중복으로오류가발생할수있다.
//  테스트에서사용한데이터를제거하는더나은방법으로는트랜잭션을활용하면된다.
//  테스트전에 트랜잭션을시작하고, 테스트이후에트랜잭션을롤백해버리면데이터가처음상태로돌아온다.

//  정리
//  이체중예외가발생하게되면memberA의금액은 10000원  8000원으로 2000원감소한다.
//  그런데 memberB의돈은그대로 10000원으로남아있다.
//  결과적으로memberA의돈만 2000원감소한것이다!