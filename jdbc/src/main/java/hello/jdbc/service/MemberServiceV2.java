package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


//  트랜젝션을 적용한 코드이다
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false); //트랜잭션 시작
            //비즈니스 로직
            bizLogic(con, fromId, toId, money);
            con.commit(); //성공시 커밋
        } catch (Exception e) {
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e); } finally {
            release(con);
        }
    }
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);
        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    // 설명
    // 트랜잭션은비즈니스로직이있는서비스계층에서시작하는것이좋다.
    // 그런데문제는트랜잭션을사용하기위해서javax.sql.DataSource, java.sql.Connection, java.sql.SQLException 같은 JDBC 기술에의존해야한다는점이다.
    // 트랜잭션을사용하기위해 JDBC 기술에의존한다.
    // 결과적으로비즈니스로직보다 JDBC를사용해서 트랜잭션을처리하는코드가더많다.
    // 향후 JDBC에서 JPA 같은다른기술로바꾸어사용하게되면서비스코드도모두함께변경해야한다. (JPA는트랜잭션을사용하는코드가 JDBC와다르다.)
    // 핵심비즈니스로직과 JDBC 기술이섞여있어서유지보수하기어렵다.
 }









/**
 *  트렌젝션 적용 하기 전 코드
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); //트랜잭션 시작
        //비즈니스 로직
            bizLogic(con, fromId, toId, money);
            con.commit(); //성공시 커밋

        } catch (Exception e) {
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }
    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); //커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}


//  설명 )
//  Connection con = dataSource.getConnection(); : 트렌잭션을 시작하려면 커넥션이 필요하다.

//  con.setAutoCommit(false); //트랜잭션 시작
//  트랜잭션을 시작하려면 자동커밋 모드를 꺼야한다. 이렇게 하면 커넥션을 통해 세션에 set autocommit false가 전달되고, 이후부터는수동커밋모드로동작한다.
//  이렇게 자동커밋모드를 수동 커밋모드로 변경하는것을 트랜잭션을 시작한다고 보통 표현한다.

//  bizLogic(con, fromId, toId, money);
//  트랜잭션이 시작된 커넥션을 전달하면서 비즈니스로직을 수행한다.
//  이렇게 분리한이유는 트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 구분하기 위함이다.
//  memberRepository.update(con..): 비즈니스로직을 보면 리포지토리를 호출할 때 커넥션을 전달하는것을 확인할 수 있다.

//  con.commit(); //성공시 커밋 : 비즈니스로직이정상수행되면트랜잭션을커밋한다.

//  con.rollback(); //실패시 롤백 : catch(Ex){..}를사용해서비즈니스로직수행도중에예외가발생하면트랜잭션을롤백한다.

//  release(con); : finally {..}를사용해서커넥션을모두사용하고나면안전하게종료한다.
//  그런데커넥션풀을 사용하면con.close()를호출했을때커넥션이종료되는것이아니라풀에반납된다.
//  현재수동 커밋모드로동작하기때문에풀에돌려주기전에기본값인자동커밋모드로변경하는것이 안전하다.
 **/