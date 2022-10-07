package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch(){
        Service service = new Service();
        service.callCatch();
    }


    @Test
    void unchecked_throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * UnChecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */


    // 언 체크 예외를 잡아서 처리하는 코드
    static class Service {
        Repository repository = new Repository();
        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }


        //  언체크예외를밖으로던지는코드 - 생략
        //  언체크예외는체크예외와다르게throws 예외를선언하지않아도된다.
        //  컴파일러가이런부분을체크하지않기때문에언체크예외이다.
        /**
         * 예외를 잡지 않아도 된다. 자연스럽게 상위로 넘어간다.
         * * 체크 예외와 다르게 throws 예외 선언을 하지 않아도 된다.
         */
        public void callThrow() {
            repository.call();
        }
    }


        static class Repository {
            public void call() {
                throw new MyUncheckedException("ex");
            }
    }
}

//  언체크 장단점
//  장 점 : 신경쓰고싶지않은언체크예외를무시할수있다.
//  체크예외의경우처리할수없는예외를밖으로 던지려면항상throws 예외를선언해야하지만, 언체크예외는이부분을생략할수있다.
//  이후에 설명하겠지만, 신경쓰고싶지않은예외의의존관계를참조하지않아도되는장점이있다.
//  단 점 : 언체크예외는개발자가실수로예외를누락할수있다. 반면에체크예외는컴파일러를통해예외 누락을잡아준다.

// 그러므로 체크예외와언체크예외의차이는사실예외를처리할수없을때예외를밖으로던지는부분에있다.
// 이 부분을필수로선언해야하는가생략할수있는가의차이다.