package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {
    public static Connection getConnection(){
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}", connection,connection getClass());
            return connection;
        }catch (SQLException e){
            throw new IllegalAccessException(e);
        }
    }
}

//  데이터베이스에연결하려면 JDBC가제공하는DriverManager.getConnection(..)를사용하면된다.
//  이렇게하면라이브러리에있는데이터베이스드라이버를찾아서해당드라이버가제공하는커넥션을 반환해준다.
//  여기서는 H2 데이터베이스드라이버가작동해서실제데이터베이스와커넥션을맺고그 결과를반환해준다.