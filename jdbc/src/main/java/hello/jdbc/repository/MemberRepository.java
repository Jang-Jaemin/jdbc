//  MemberRepository 인터페이스

package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.Connection;


public interface MemberRepository {

    Member save(Member member);

    Member findById(String memberId);

    Member findById(Connection con, String memberId);

    void update(String memberId, int money);

    void update(Connection con, String memberId, int money);

    void delete(String memberId);

}