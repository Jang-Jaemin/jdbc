package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.DBConnectionUtil.getConnection;
import static org.springframework.boot.SpringApplication.close;

/**
 * JDBC - DataSourece 사용, JdbcUtils 사용
 */

@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource){
        this.dataSource = dataSource;
    }

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

            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeConnection();
            JdbcUtils.closeStatement(stmt);
        }

        private Connection getConnection () throw SQLException{
            Connection con = dataSource.getConnection();
            log.info("get connection={}, class={}", con, con.getClass());
            return con;
        }
    }
}

//  DataSource 의존관계주입
//  외부에서DataSource를주입받아서사용한다. 이제직접만든DBConnectionUtil을사용하지 않아도된다.
//  DataSource는표준인터페이스이기때문에DriverManagerDataSource에서 HikariDataSource로변경되어도해당코드를변경하지않아도된다.
//  JdbcUtils 편의메서드
//  스프링은 JDBC를편리하게다룰수있는JdbcUtils라는편의메서드를제공한다. JdbcUtils을사용하면커넥션을좀더편리하게닫을수있다
