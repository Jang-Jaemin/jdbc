package hello.jdbc.repository;


import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 *   JDBCTEMPLATE 사용
 *
 * */

@Slf4j
public class MemberRepositoryV5 implements MemberRepository {


    private final JdbcTemplate template ;


    public MemberRepositoryV5(DataSource dataSource) {

        template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    @Override
    public Member findById(Connection con, String memberId) {
        return null;
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    @Override
    public void update(Connection con, String memberId, int money) {

    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }
    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }
}

//  JDBC 반복문제
//  커넥션조회, 커넥션동기화
//  PreparedStatement 생성및파라미터바인딩
//  쿼리실행
//  결과바인딩
//  예외발생시스프링예외변환기실행
//  리소스종료

//  리포지토리의각각의메서드를살펴보면상당히많은부분이반복된다.
//  이런반복을효과적으로처리하는 방법이바로템플릿콜백패턴이다.
//  스프링은 JDBC의반복문제를해결하기위해JdbcTemplate이라는템플릿을제공한다.
//  JdbcTemplate에대한자세한사용법은뒤에서설명하겠다.
//  지금은전체구조와, 이기능을사용해서반복 코드를제거할수있다는것에초점을맞춘다.

//  MemberRepository 인터페이스가제공되므로등록하는빈만MemberRepositoryV5로변경해서 등록하면된다.

//  JdbcTemplate은 JDBC로개발할때발생하는반복을대부분해결해준다.
//  그뿐만아니라지금까지 학습했던, 트랜잭션을위한커넥션동기화는물론이고, 예외발생시스프링예외변환기도자동으로 실행해준다.

//  정리
//  완성된 코드
//  서비스 계층의 순수성
//  1. 트랜잭션추상화 + 트랜잭션 AOP 덕분에서비스계층의순수성을최대한유지하면서서비스 계층에서트랜잭션을사용할수있다.
//  2. 스프링이제공하는예외추상화와예외변환기덕분에, 데이터접근기술이변경되어도서비스계층의 순수성을유지하면서예외도사용할수있다.
//  3. 서비스계층이리포지토리인터페이스에의존한덕분에향후리포지토리가다른구현기술로 변경되어도서비스계층을순수하게유지할수있다.
//  리포지토리에서 JDBC를사용하는반복코드가JdbcTemplate으로대부분제거되었다.

