package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class MemberServiceV2Test {

    /**
     * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
     */
    class MemberServiceV2Test {
        private MemberRepositoryV2 memberRepository;
        private MemberServiceV2 memberService;

        @BeforeEach
        void before() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
            memberRepository = new MemberRepositoryV2(dataSource);
            memberService = new MemberServiceV2(dataSource, memberRepository);
        }

        @AfterEach
        void after() throws SQLException {
            memberRepository.delete("memberA");
            memberRepository.delete("memberB");
            memberRepository.delete("ex");
        }

        @Test
        @DisplayName("정상 이체")
        void accountTransfer() throws SQLException {
            //given
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
        @DisplayName("이체중 예외 발생")
        void accountTransferEx() throws SQLException {
            //given
            Member memberA = new Member();
            Member memberEx = new Member();
            memberRepository.save(memberA);
            memberRepository.save(memberEx);
            //when
            assertThatThrownBy(() ->
                    memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                    .isInstanceOf(IllegalStateException.class);
            //then
            Member findMemberA = memberRepository.findById(memberA.getMemberId());
            Member findMemberEx =
                    memberRepository.findById(memberEx.getMemberId());
            //memberA의 돈이 롤백 되어야함
            assertThat(findMemberA.getMoney()).isEqualTo(10000);
            assertThat(findMemberEx.getMoney()).isEqualTo(10000);
        }
    }
}

// 정상이체 - accountTransfer()
// 기존 로직과 같아서 생략

// 이체중 예외 발생 - accountTransferEx()
// 다음 데이터를 저장해서 테스트를 준비한다.
// memberA 10000원
// memberB 10000원

// 계좌이체 로직을 실행한다.
// memberService.accountTransfer()를실행한다. 커넥션을생성하고트랜잭션을시작한다. memberA   memberEx로 2000원계좌이체한다.
// memberA의금액이 2000원감소한다.
// memberEx회원의 ID는ex이므로중간에예외가발생한다. 예외가발생했으므로트랜잭션을롤백한다.

// 계좌이체는실패했다. 롤백을수행해서memberA의돈이기존 10000원으로복구되었다. memberA 10000원 - 트랜잭션롤백으로복구된다.
// memberB 10000원 - 중간에실패로로직이수행되지않았다. 따라서그대로 10000원으로남아있게 된다.
// 트랜잭션덕분에계좌이체가실패할때롤백을수행해서모든데이터를정상적으로초기화할수있게 되었다. 결과적으로계좌이체를수행하기직전으로돌아가게된다.
