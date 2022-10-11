/** 런타임예외사용변환 - 코드 - UncheckedAppTest **/

package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UnCheckedAppTest {
    @Test
    void Unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }
    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            //e.printStackTrace();
            log.info("ex", e);
        }
    }

    static class Controller {
        Service service = new Service();
        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();
        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

        static class NetworkClient {
            public void call() {
                throw new RuntimeConnectException("연결 실패"); }
        }

        static class Repository {
            public void call() {
                try {
                    runSQL();
                } catch (SQLException e) {
                    throw new RuntimeSQLException(e); }
            }
            private void runSQL() throws SQLException {
                throw new SQLException("ex"); }
        }

        static class RuntimeConnectException extends RuntimeException {
            public RuntimeConnectException(String message) {
                super(message); }
        }

        static class RuntimeSQLEException extends RuntimeException {
            public RuntimeException() {
            }
        }

        public RuntimeException(Throwable cause) {
            super(cause);
        }
    }
}

// 설명
//  SQLException을런타임 예외인 RuntimeSQLException으로 변환했다.
//  ConnectException 대신에 RuntimeConnectException을 사용 하도록 바꾸었다.
//  런타임예외이기때문에 서비스, 컨트롤러는 해당 예외들을 처리할 수 없다면 별도의 선언 없이 그냥두면 된다.

//  런타임예외를사용하면중간에기술이변경되어도해당예외를사용하지않는컨트롤러, 서비스에서는 코드를 변경 하지 않아도 된다.
//  구현기술이 변경되는 경우, 예외를 공통으로 처리하는곳에서는 예외에 따른 다른 처리가 필요할 수 있다.
//  하지만 공통처리하는 한 곳만 변경하면 되기 때문에 변경의 영향 범위는 최소화된다.
