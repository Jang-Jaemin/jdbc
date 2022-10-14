package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //save
        Member member = new Member();
        repository.save(member);

        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);

        //update: money: 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);
        //  회원데이터의money를 10000 -> 20000으로수정하고, DB에서데이터를다시조회해서 20000으로 변경되었는지검증한다.

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);

        //  회원을삭제한다음findById()를통해서조회한다.
        //  회원이없기때문에NoSuchElementException이 발생한다.
        //  assertThatThrownBy는해당예외가발생해야검증에성공한다.

        //> 참고
        //> 마지막에회원을삭제하기때문에테스트가정상수행되면, 이제부터는같은테스트를반복해서실행할수 있다.
        // 물론테스트중간에오류가발생해서삭제로직을수행할수없다면테스트를반복해서실행할수 없다.
        //> 트랜잭션을활용하면이문제를깔끔하게해결할수있는데, 자세한내용은뒤에서설명한다.
    }
}

