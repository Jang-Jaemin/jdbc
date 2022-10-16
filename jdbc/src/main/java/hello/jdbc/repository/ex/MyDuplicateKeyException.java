//  MyDuplicateKeyException

package hello.jdbc.repository.ex;

public class MyDuplicateKeyException {

    public MyDuplicateKeyException() {

    }
    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}

//  기존에사용했던MyDbException을상속받아서의미있는계층을형성한다.
//  이렇게하면데이터베이스 관련예외라는계층을만들수있다.
//  그리고이름도MyDuplicateKeyException이라는이름을지었다.
//  이예외는데이터중복의경우에만 던져야한다.
//  이예외는우리가직접만든것이기때문에, JDBC나 JPA 같은특정기술에종속적이지않다
//  따라서이 예외를사용하더라도서비스계층의순수성을유지할수있다.
//  (향후 JDBC에서다른기술로바꾸어도이 예외는그대로유지할수있다.)