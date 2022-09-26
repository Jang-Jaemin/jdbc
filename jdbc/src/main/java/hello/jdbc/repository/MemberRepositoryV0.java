package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import java.util.NoSuchElementException;

import java.sql.*;

import static hello.jdbc.connection.DBConnectionUtil.getConnection;
import static org.springframework.boot.SpringApplication.close;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    // 회원 등록 save
    public Member save(Member member) throws SQLException {
        String sql = "insert int member(member_id, momey) values(?,?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    //  MemberRepositoryV0 - 회원조회추가
    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }catch(SQLException e){
                log.error("db error", e);
                throw e;
            }finally{
                close(con, pstmt, rs);
                //  executeUpdate()는쿼리를실행하고영향받은 row수를반환한다.
                //  여기서는하나의데이터만변경하기 때문에결과로 1이반환된다.
                //  만약회원이 100명이고, 모든회원의데이터를한번에수정하는 update sql 을실행하면결과는 100이된다.
            }
        }

        //   MemberRepositoryV0 - 회원 수정 추가.
        public void update(String memberId, int money) throws SQLException {
            String sql = "update member set money=? where member_id=?";

            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setInt(1, money);
                pstmt.setString(2, memberId);
                int resultSize = pstmt.executeUpdate();
                log.info("resultSize={}", resultSize);
            } catch (SQLException e) {
                log.error("db error", e);
                throw e;
            } finally {
                close(con, pstmt, null);
            }
        }

        //  회원 삭제 추가.

        public void delete(String memberId) throws SQLException {
            String sql = "delete from member where member_id=?";

            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, memberId);

                pstmt.executeUpdate();

            } catch (SQLException e) {
                log.error("db error", e);
                throw e;
            } finally {
                close(con, pstmt, null);
            }
        }

        // 회원 등록 close
        private void close (Connection con, Statement stmt, ResultSet rs){
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.info("error", e);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.info("error", e);
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.info("error", e);
                }
            }
        }
        private Connection getConnection () {
            return (Member) getConnection();
        }
    }
}


//  커넥션획득
//  getConnection(): 이전에만들어둔DBConnectionUtil를통해서데이터베이스커넥션을획득한다.

//  save() - SQL 전달
//  sql: 데이터베이스에전달할 SQL을정의한다. 여기서는데이터를등록해야하므로insert sql을 준비했다.
//  con.prepareStatement(sql): 데이터베이스에전달할 SQL과파라미터로전달할데이터들을 준비한다.
//  sql: insert into member(member_id, money) values(?, ?)"
//  pstmt.setString(1, member.getMemberId()): SQL의첫번째?에값을지정한다. 문자이므로 setString을사용한다.
//  pstmt.setInt(2, member.getMoney()): SQL의두번째?에값을지정한다. Int형숫자이므로 setInt를지정한다.
//  pstmt.executeUpdate(): Statement를통해준비된 SQL을커넥션을통해실제데이터베이스에 전달한다.
//  참고로executeUpdate()은int를반환하는데영향받은 DB row 수를반환한다. 여기서는 하나의 row를등록했으므로 1을반환한다.

//  executeUpdate()
//  int executeUpdate() throws SQLException;

//  리소스정리
//  쿼리를실행하고나면리소스를정리해야한다. 여기서는Connection, PreparedStatement를 사용했다.
//  리소스를정리할때는항상역순으로해야한다.
//  Connection을먼저획득하고Connection을 통해PreparedStatement를만들었기때문에리소스를반환할때는PreparedStatement를먼저 종료하고, 그다음에Connection을종료하면된다.
//  참고로여기서사용하지않은ResultSet은결과를 조회할때사용한다.
//  조금뒤에조회부분에서알아보자.

//  주의
//  리소스정리는꼭! 해주어야한다. 따라서예외가발생하든, 하지않든항상수행되어야하므로finally 구문에주의해서작성해야한다.
//  만약이부분을놓치게되면커넥션이끊어지지않고계속유지되는문제가 발생할수있다.
//  이런것을리소스누수라고하는데, 결과적으로커넥션부족으로장애가발생할수있다

//  참고
//  PreparedStatement는Statement의자식타입인데, ?를통한파라미터바인딩을가능하게해준다.
//  참고로 SQL Injection 공격을예방하려면PreparedStatement를통한파라미터바인딩방식을
//  사용해야한다.