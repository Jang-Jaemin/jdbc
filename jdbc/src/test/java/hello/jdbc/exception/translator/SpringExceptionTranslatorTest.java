//   스프링이 제공하는 예외 변환기

//  스프링은데이터베이스에서발생하는오류코드를스프링이정의한예외로자동으로변환해주는변환기를 제공한다.

/** SpringExceptionTranslatorTest **/
package hello.jdbc.exception.translator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class SpringExceptionTranslatorTest {

   DataSource dataSource;

   @BeforeEach
    void init(){
       dataSource = new DriverManagerDataSource(URL,USERNAME,PASSWORD);
   }

    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammar";
        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            //org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
        }
   }

   @Test
    void exceptionTranslatoor(){
       String sql = "select bad grammer";
       try {
           Connection con = dataSource.getConnection();
           PreparedStatement stmt = con.prepareStatement(sql);
           stmt.executeQuery();
       }catch (SQLException e) {
           assertThat(e.getErrorCode()).isEqualTo(42122);

           SQLErrorCodeSQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
           DataAccessException resultEx = exTranslator.translate("select,sql,e");
           log.info("resultEx",resultEx);
           assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
       }
   }
}

//  스프링이제공하는 SQL 예외변환기는다음과같이사용하면된다.
/** SQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    DataAccessException resultEx = exTranslator.translate("select", sql, e);
 **/
//  translate() 메서드의첫번째파라미터는읽을수있는설명이고, 두번째는실행한 sql, 마지막은발생된
//  SQLException을전달하면된다. 이렇게하면적절한스프링데이터접근계층의예외로변환해서 반환해준다.
//  예제에서는 SQL 문법이잘못되었으므로BadSqlGrammarException을반환하는것을확인할수있다.
//  눈에보이는반환타입은최상위타입인DataAccessException이지만실제로는 BadSqlGrammarException 예외가반환된다.
//  마지막에assertThat() 부분을확인하자. 참고로BadSqlGrammarException은최상위타입인DataAccessException를상속받아서 만들어진다.

//  정리 :
//  스프링은 데이터 접근 계층에 대한 일관된 예외 추상화를 제공한다.
//  스프링은 예외 변환기를 통해서 SQLException의 ErrorCode에 맞는 적절한 스프링 데이터 접근 예외로 변환해준다.
//  만약서비스, 컨트롤러 계층에서 예외처리가 필요하면 특정 기술에 종속적인 SQLException 같은 예외를 직접 사용하는것이 아니라, 스프링이 제공하는 데이터 접근 예외를 사용하면된다.
//  스프링 예외 추상화와 덕분에 특정 기술에 종속적이지 않게 되었다.
//  이제 JDBC에서 JPA같은 기술로 변경되어도 예외로 인한 변경을 최소화할 수 있다.
//  향후 JDBC에서 JPA로구현 기술을 변경하더라도, 스프링은 JPA 예외를 적절한 스프링데이터접근예외로변환해준다.
//  물론 스프링이 제공하는 예외를 사용하기 때문에 스프링에대한 기술 종속성은 발생한다.
//  스프링에 대한 기술종속성까지 완전히제거하려면예외를모두직접정의하고 예외 변환도 직접하면 되지만, 실용적인 방법은 아니다.