/** 체크 예외 문제점 코드 App Test **/

package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedAppTest {
    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }
    /** 체크 예외 throws 선언 **/
    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();

        }
        /** 체크 예외 throws 선언 **/
        static class Service {
            Repository repository = new Repository();
            NetworkClient networkClient = new NetworkClient();

            public void logic() throws SQLException, ConnectException {
                repository.call();
                networkClient.call();
            }
        }

        static class NetworkClient {
            public void call() throws ConnectException {
                throw new ConnectException("연결 실패");
            }
        }

        static class Repository {
            public void call() throws SQLException {
                throw new SQLException("ex");
            }
        }
    }
}

//  서비스 : 체크예외를처리하지못해서밖으로던지기위해logic() throws SQLException, ConnectException를선언했다

//  컨트롤러 : 체크예외를처리하지못해서밖으로던지기위해request() throws SQLException, ConnectException를선언했다.

//  1. 복구 불가능한 예외
//  SQLException을예를들면데이터베이스에무언가문제가있어서발생하는예외이다. SQL 문법에
//  문제가있을수도있고, 데이터베이스자체에뭔가문제가발생했을수도있다. 데이터베이스서버가중간에 다운되었을수도있다.
//  이런문제들은대부분복구가불가능하다. 특히나대부분의서비스나컨트롤러는 이런문제를해결할수는없다.
//  따라서이런문제들은일관성있게공통으로처리해야한다. 오류로그를 남기고개발자가해당오류를빠르게인지하는것이필요하다.
//  서블릿필터, 스프링인터셉터, 스프링의 ControllerAdvice를사용하면이런부분을깔끔하게공통으로해결할수있다.

//  2. 의존 관계에 대한 문제
//  체크예외의또다른심각한문제는예외에대한의존관계문제이다.
//  앞서대부분의예외는복구불가능한예외라고했다.
//  그런데체크예외이기때문에컨트롤러나서비스 입장에서는본인이처리할수없어도어쩔수없이throws를통해던지는예외를선언해야한다.
