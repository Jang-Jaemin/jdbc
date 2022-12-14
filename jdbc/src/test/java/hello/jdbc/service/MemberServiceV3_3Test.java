package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

public class MemberServiceV3_3Test {

    /**
     * νΈλμ­μ -  @Transactional AOP
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
            @Bean
            DataSource dataSource() {
                return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
            }
            @Bean
            PlatformTransactionManager transactionManager() {
                return new DataSourceTransactionManager(dataSource());
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
         * AOP νλ‘μμ μ©νμΈ
         * **/
        @Test
        void AopCheck() {
            log.info("memberService class={}", memberService.getClass());
            log.info("memberRepository class={}", memberRepository.getClass());
            Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
            Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
            //  λ¨Όμ  AOP νλ‘μκ°μ μ©λμλμ§νμΈν΄λ³΄μ. AopCheck()μμ€νκ²°κ³Όλ₯Όλ³΄λ©΄memberServiceμ EnhancerBySpringCGLIB..λΌλλΆλΆμν΅ν΄νλ‘μ(CGLIB)κ°μ μ©λκ²μνμΈν μμλ€.
            //  memberRepositoryμλ AOPλ₯Όμ μ©νμ§μμκΈ°λλ¬Έμνλ‘μκ°μ μ©λμ§μλλ€.
            //  λλ¨Έμ§νμ€νΈμ½λλ€μμ€νν΄λ³΄λ©΄νΈλμ­μμ΄μ μμνλκ³ , μ€ν¨μμ μλ‘€λ°±λκ²μνμΈν μμλ€.
        }

        @Test
        @DisplayName("μ μ μ΄μ²΄")
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
        @DisplayName("μ΄μ²΄μ€ μμΈ λ°μ")
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
            Member findMemberEx = memberRepository.findById(memberEx.getMemberId());

            //memberAμ λμ΄ λ‘€λ°± λμ΄μΌν¨
            assertThat(findMemberA.getMoney()).isEqualTo(10000);
            assertThat(findMemberEx.getMoney()).isEqualTo(10000);
        }
    }
}

//  @SpringBootTest: μ€νλ§ AOPλ₯Όμ μ©νλ €λ©΄μ€νλ§μ»¨νμ΄λκ°νμνλ€. μ΄μ λΈνμ΄μμ΄μμΌλ©΄ νμ€νΈμμ€νλ§λΆνΈλ₯Όν΅ν΄μ€νλ§μ»¨νμ΄λλ₯Όμμ±νλ€.
//  κ·Έλ¦¬κ³ νμ€νΈμμ@Autowiredλ±μν΅ν΄ μ€νλ§μ»¨νμ΄λκ°κ΄λ¦¬νλλΉλ€μμ¬μ©ν μμλ€.

//  @TestConfiguration: νμ€νΈμμμλ΄λΆμ€μ ν΄λμ€λ₯Όλ§λ€μ΄μμ¬μ©νλ©΄μμ΄μλΈνμ΄μμλΆμ΄λ©΄, μ€νλ§λΆνΈκ°μλμΌλ‘λ§λ€μ΄μ£ΌλλΉλ€μμΆκ°λ‘νμνμ€νλ§λΉλ€μλ±λ‘νκ³ νμ€νΈλ₯Όμνν μ μλ€.

//  TestConfig
//      DataSource μ€νλ§μμκΈ°λ³ΈμΌλ‘μ¬μ©ν λ°μ΄ν°μμ€λ₯Όμ€νλ§λΉμΌλ‘λ±λ‘νλ€. μΆκ°λ‘νΈλμ­μ λ§€λμ μμλμ¬μ©νλ€.
//      DataSourceTransactionManager νΈλμ­μλ§€λμ λ₯Όμ€νλ§λΉμΌλ‘λ±λ‘νλ€.
//      μ€νλ§μ΄μ κ³΅νλνΈλμ­μ AOPλμ€νλ§λΉμλ±λ‘λνΈλμ­μλ§€λμ λ₯Όμ°Ύμμμ¬μ©νκΈ° λλ¬ΈμνΈλμ­μλ§€λμ λ₯Όμ€νλ§λΉμΌλ‘λ±λ‘ν΄λμ΄μΌνλ€.