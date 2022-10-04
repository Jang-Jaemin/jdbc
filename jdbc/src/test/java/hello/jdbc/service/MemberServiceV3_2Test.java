package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberServiceV3_2Test {

    /**
     * 트랜잭션 - 트랜잭션 템플릿
     *
     **/

    class MemberServiceV3_1Test {
        public static final String MEMBER_A = "memberA";
        public static final String MEMBER_B = "memberB";
        public static final String MEMBER_EX = "ex";

        private MemberRepositoryV3 memberRepository;
        private MemberServiceV3_2 memberService;

        @BeforeEach
        void before() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
            PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
            memberRepository = new MemberRepositoryV3(dataSource);
            memberService = new MemberServiceV3_2(transactionManager, memberRepository);
        }

        @AfterEach
        void after() throws SQLException {
            memberRepository.delete(MEMBER_A);
            memberRepository.delete(MEMBER_B);
            memberRepository.delete(MEMBER_EX);
        }

        @Test
        @DisplayName("정상 이체")
        void accountTransfer() throws SQLException {
            //given
            Member memberA = new Member(MEMBER_A, 10000);
            Member memberB = new Member(MEMBER_B, 10000);
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
        @DisplayName("이체중 예외 발생")
        void accountTransferEx() throws SQLException {
            //given
            Member memberA = new Member(MEMBER_A, 10000);
            Member memberEx = new Member(MEMBER_EX, 10000);
            memberRepository.save(memberA);
            memberRepository.save(memberEx);

            //when
            assertThatThrownBy(() ->
                    memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                    .isInstanceOf(IllegalStateException.class);

            //then
            Member findMemberA = memberRepository.findById(memberA.getMemberId());
            Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

            //memberA의 돈이 롤백 되어야함
            assertThat(findMemberA.getMoney()).isEqualTo(10000);
            assertThat(findMemberEx.getMoney()).isEqualTo(10000);
        }
    }
}

// 정리
//  테스트 내용은 기존과 같다.
//  테스트를 실행해보면 정상 동작하는 하고, 실패시 롤백도 잘 수행되는 것을 확인할수있다.

//  트랜잭션템플릿덕분에, 트랜잭션을사용할때반복하는코드를제거할수있었다.
//  하지만이곳은서비스로직인데비즈니스로직뿐만아니라트랜잭션을처리하는기술로직이함께 포함되어있다.
//  애플리케이션을구성하는로직을핵심기능과부가기능으로구분하자면서비스입장에서비즈니스로직은 핵심기능이고, 트랜잭션은부가기능이다.
//  이렇게비즈니스로직과트랜잭션을처리하는기술로직이한곳에있으면두관심사를하나의클래스에서 처리하게된다. 결과적으로코드를유지보수하기어려워진다.
