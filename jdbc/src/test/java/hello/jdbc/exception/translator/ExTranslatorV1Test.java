//  ExTranslatorV1Test

package hello.jdbc.exception.translator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.springframework.jdbc.support.JdbcUtils.closeStatement;

public class ExTranslatorV1Test {
    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }
    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");//같은 ID 저장 시도
    }
    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;
        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId={}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("키 중복, 복구 시도");
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e) {
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }
        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000); }
    }
    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource; public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?, ?)";
            Connection con = null;
            PreparedStatement pstmt = null;
        }

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            //h2 db
            if (e.getErrorCode() == 23505) {
                throw new MyDuplicateKeyException(e);
            }
            throw new MyDbException(e);
        } finally {
            closeStatement(pstmt);
            closeConnection(con);
        }
    }
}

//  처음에저장을시도한다. 만약리포지토리에서MyDuplicateKeyException 예외가올라오면이예외를 잡는다.
//  예외를잡아서generateNewId(memberId)로새로운 ID 생성을시도한다. 그리고다시저장한다.
//  여기가 예외를복구하는부분이다.
//  만약복구할수없는예외( MyDbException)면로그만남기고다시예외를던진다.
//  참고로이경우여기서예외로그를남기지않아도된다. 어차피복구할수없는예외는예외를 공통으로처리하는부분까지전달되기때문이다.
//  따라서이렇게복구할수없는예외는공통으로 예외를처리하는곳에서예외로그를남기는것이좋다.
//  여기서는다양하게예외를잡아서처리할수 있는점을보여주기위해이곳에코드를만들어두었다.

//  정리
//  SQL ErrorCode로데이터베이스에어떤오류가있는지확인할수있었다.
//  예외변환을통해SQLException을특정기술에의존하지않는직접만든예외인 MyDuplicateKeyException로변환할수있었다.
//  리포지토리계층이예외를변환해준덕분에서비스계층은특정기술에의존하지않는 MyDuplicateKeyException을사용해서문제를복구하고, 서비스계층의순수성도유지할수있었다.
