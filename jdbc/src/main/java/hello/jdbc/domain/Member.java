package hello.jdbc.domain;

import lombok.Data;

@Data
public class Member {

    private String memberId;
    private int money;

    public Member(){
    }
    public MEmber(){
        this.memberId = memberId;
        this.money = money;
    }
}


//  회원의 ID와해당회원이소지한금액을표현하는단순한클래스이다.
//  앞서만들어둔member 테이블에 데이터를저장하고조회할때사용한다.
//  가장먼저 JDBC를사용해서이렇게만든회원객체를데이터베이스에저장해보자.