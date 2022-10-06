package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest

public class MemberServiceV3_4Test {

    /**
     * 트랜잭션 -  @DataSource, transactionManager 자동 등록
     *
     * 기존( MemberServiceV3_3Test)과같은코드이고TestConfig 부분만다르다.
     * 데이터소스와트랜잭션매니저를스프링빈으로등록하는코드가생략되었다.
     * 따라서스프링부트가 application.properties에지정된속성을참고해서데이터소스와트랜잭션매니저를자동으로 생성해준다.
     * 코드에서보는것처럼생성자를통해서스프링부트가만들어준데이터소스빈을주입받을수도있다.
     *
     **/

    static class MemberServiceV3_3Test {
        public static final String MEMBER_A = "memberA";
        public static final String MEMBER_B = "memberB";
        public static final String MEMBER_EX = "ex";

        @Autowired
        MemberRepositoryV3 memberRepository;
        @Autowired
        MemberServiceV3_3 memberService;

        @AfterEach
        void after() throws SQLException {
            memberRepository.delete(MEMBER_A);
            memberRepository.delete(MEMBER_B);
            memberRepository.delete(MEMBER_EX);
        }

        @TestConfiguration
        static class TestConfig {

            private final DataSource dataSource;

            public TestConfig(DataSource dataSource){
                this.dataSource = dataSource;
            }

            @Bean
            MemberRepositoryV3 memberRepositoryV3() {
                return new MemberRepositoryV3(dataSource());
            }
            @Bean
            MemberServiceV3_3 memberServiceV3_3() {

                return new MemberServiceV3_3(memberRepositoryV3());
            }
        }
        /**
         * AOP 프록시적용확인
         * **/
        @Test
        void AopCheck() {
            log.info("memberService class={}", memberService.getClass());
            log.info("memberRepository class={}", memberRepository.getClass());
            Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
            Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
            //  먼저 AOP 프록시가적용되었는지확인해보자. AopCheck()의실행결과를보면memberService에 EnhancerBySpringCGLIB..라는부분을통해프록시(CGLIB)가적용된것을확인할수있다.
            //  memberRepository에는 AOP를적용하지않았기때문에프록시가적용되지않는다.
            //  나머지테스트코드들을실행해보면트랜잭션이정상수행되고, 실패시정상롤백된것을확인할수있다.
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

//  @SpringBootTest: 스프링 AOP를적용하려면스프링컨테이너가필요하다. 이애노테이션이있으면 테스트시스프링부트를통해스프링컨테이너를생성한다.
//  그리고테스트에서@Autowired등을통해 스프링컨테이너가관리하는빈들을사용할수있다.

//  @TestConfiguration: 테스트안에서내부설정클래스를만들어서사용하면서이에노테이션을붙이면, 스프링부트가자동으로만들어주는빈들에추가로필요한스프링빈들을등록하고테스트를수행할수 있다.

//  TestConfig
//      DataSource 스프링에서기본으로사용할데이터소스를스프링빈으로등록한다. 추가로트랜잭션 매니저에서도사용한다.
//      DataSourceTransactionManager 트랜잭션매니저를스프링빈으로등록한다.
//      스프링이제공하는트랜잭션 AOP는스프링빈에등록된트랜잭션매니저를찾아서사용하기 때문에트랜잭션매니저를스프링빈으로등록해두어야한다.