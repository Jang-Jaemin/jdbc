//  체크 예외 전체 코드

package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class CheckedTest {


    @Test
    void checked_catch(){
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_thorw(){
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }
    /**
     * Exception을 상속받은 예외는 체크 예외가 된다.
     *
     * */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
        super(message);
        }
    }

    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
     *
     * */

    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         *
         * */
        public void callCatch() {
            try{
                repository.call();
            }catch (MyCheckedException e){
                // 예외 처리 로직
                log.info("예외처리, mewssage={}", e.getMessage(),e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야한다.
         */

        public void callThrow()throws MyCheckedException {
            repository.call();
        }
    }

    static class Pepository {
        public void call() throws MyCheckedException { // 밖으로 던지는걸 반드시 설정해줘야한다. throws ..
            throw new MyCheckedException("ex");
        }
    }
}

// Exception을상속받은예외는체크예외가된다.
//  MyCheckedException는Exception을상속받았다.
//  Exception을상속받으면체크예외가된다.
//  참고로RuntimeException을상속받으면언체크예외가된다.
//  이런규칙은자바언어에서문법으로정한 것이다.
//  예외가제공하는여러가지기본기능이있는데, 그중에오류메시지를보관하는기능도있다.
//  예제에서보는 것처럼생성자를통해서해당기능을그대로사용하면편리하다.
