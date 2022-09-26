// ConnectionTest - 드라이버매니저

package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
        //  실행 결과
        //  connection=conn0: url=jdbc:h2:tcp://..test user=SA, class=class org.h2.jdbc.JdbcConnection
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());

        //  dataSourceDriverManager() - 실행결과
        //  DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp:..test]
        //  DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp:..test]
        //  connection=conn0: url=jdbc:h2:tcp://..test user=SA, class=class org.h2.jdbc.JdbcConnection
        //  connection=conn1: url=jdbc:h2:tcp://..test user=SA, class=class org.h2.jdbc.JdbcConnection
    }

    @Test
    void dataSoureceConnectionPool() throws SQLException, InterruptedException{
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        useDataSource(dataSource);
        Thread.sleep(1000); // 커넥션 풀에서 커넥션 생성 시간 대기
        //  HikariCP 커넥션풀을사용한다. HikariDataSource는DataSource 인터페이스를구현하고있다.
        //  커넥션풀최대사이즈를 10으로지정하고, 풀의이름을MyPool이라고지정했다.

        //  커넥션풀에서커넥션을생성하는작업은애플리케이션실행속도에영향을주지않기위해별도의 쓰레드에서작동한다.
        //  별도의쓰레드에서동작하기때문에테스트가먼저종료되어버린다.
        //  예제처럼 Thread.sleep을통해대기시간을주어야쓰레드풀에커넥션이생성되는로그를확인할수있다.

    }
}

//  ConnectionTest - 데이터소스드라이버매니저추가 관련 내용... ()

//  DriverManager 는 아래 처럼 나온다.
//  DriverManager.getConnection(URL, USERNAME, PASSWORD)
//  DriverManager.getConnection(URL, USERNAME, PASSWORD)

//  반면에 DataSource는
//  DataSource를만들고필요한속성들을사용해서URL, USERNAME, PASSWORD같은부분을 입력하는것을말한다.
//  이렇게설정과관련된속성들은한곳에있는것이향후변경에더유연하게대처할 수있다.
//  사용 : 설정은신경쓰지않고, DataSource의getConnection()만호출해서사용하면된다.


//  기존코드와비슷하지만DriverManagerDataSource는DataSource를통해서커넥션을획득할수 있다.
//  참고로DriverManagerDataSource는스프링이제공하는코드이다.

//  파라미터차이
//  기존DriverManager를통해서커넥션을획득하는방법과DataSource를통해서커넥션을획득하는 방법에는큰차이가있다.


//  ConnectionTest - 데이터소스커넥션풀추가 관련 내용...(~)
//  HikariConfig : HikariCP 관련설정을확인할수있다. 풀의이름( MyPool)과최대풀수( 10)을확인할수있다.

//  MyPool connection adder
//  별도의쓰레드사용해서커넥션풀에커넥션을채우고있는것을확인할수있다.
//  이쓰레드는커넥션풀에 커넥션을최대풀수( 10)까지채운다.
//  그렇다면왜별도의쓰레드를사용해서커넥션풀에커넥션을채우는것일까?
//  커넥션풀에커넥션을채우는것은상대적으로오래걸리는일이다.
//  애플리케이션을실행할때커넥션풀을 채울때까지마냥대기하고있다면애플리케이션실행시간이늦어진다.
//  따라서이렇게별도의쓰레드를 사용해서커넥션풀을채워야애플리케이션실행시간에영향을주지않는다.

//  커넥션풀에서커넥션획득
//  커넥션풀에서커넥션을획득하고그결과를출력했다. 여기서는커넥션풀에서커넥션을 2개획득하고 반환하지는않았다.
//  따라서풀에있는 10개의커넥션중에 2개를가지고있는상태이다. 그래서마지막 로그를 보면 사용중인 커넥션 active=2,
//  풀에서 대기 상태인 커넥션 idle=8을 확인할 수 있다.