package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DBConnectionUtilTest {
    @Test
    void connection(){
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }
}

//  주의
//  실행전에 H2 데이터베이스서버를실행해두어야한다. ( h2.sh, h2.bat)

//  실행결과
//  DBConnectionUtil - get connection=conn0: url=jdbc:h2:tcp://localhost/~/test user=SA,
//  class=class org.h2.jdbc.JdbcConnection

//  실행결과를보면class=class org.h2.jdbc.JdbcConnection 부분을확인할수있다.
//  이것이바로 H2 데이터베이스드라이버가제공하는 H2 전용커넥션이다.
//  물론이커넥션은 JDBC 표준커넥션 인터페이스인java.sql.Connection 인터페이스를구현하고있다.

//  참고 - 오류
//  다음과같은오류가발생하면 H2 데이터베이스가실행되지않았거나, 설정에오류가있는것이다.
//  H2 데이터베이스설정부분을다시확인하자.
//  Connection is broken: "java.net.ConnectException: Connection refused
//  (Connection refused): localhost" [90067-200]

//  DriverManager 커넥션요청흐름
//  JDBC가제공하는DriverManager는라이브러리에등록된 DB 드라이버들을관리하고, 커넥션을 획득하는기능을제공한다.

//  1. 애플리케이션로직에서커넥션이필요하면DriverManager.getConnection()을호출한다.

//  2. DriverManager는라이브러리에등록된드라이버목록을자동으로인식한다.
//  이드라이버들에게 순서대로다음정보를넘겨서커넥션을획득할수있는지확인한다.
//  URL: 예) jdbc:h2:tcp://localhost/~/test 이름, 비밀번호등접속에필요한추가정보
//  여기서각각의드라이버는 URL 정보를체크해서본인이처리할수있는요청인지확인한다.
//  예를 들어서 URL이jdbc:h2로시작하면이것은 h2 데이터베이스에접근하기위한규칙이다.
//  따라서 H2 드라이버는본인이처리할수있으므로실제데이터베이스에연결해서커넥션을획득하고이커넥션을 클라이언트에반환한다.
//  반면에 URL이jdbc:h2로시작했는데 MySQL 드라이버가먼저실행되면 이경우본인이처리할수없다는결과를반환하게되고, 다음드라이버에게순서가넘어간다.

//  3. 이렇게찾은커넥션구현체가클라이언트에반환된다.
//  H2 데이터베이스드라이버만라이브러리에등록했기때문에 H2 드라이버가제공하는 H2 커넥션을제공받는다.
//  물론이 H2 커넥션은 JDBC가제공하는java.sql.Connection 인터페이스를 구현하고있다.

