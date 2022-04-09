import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SQLResultTracer {
    private static final String url = "jdbc:postgresql://localhost:5432/Datalog2SQL";
    private static final String driver = "org.postgresql.Driver";
    private static final String user = "postgres";
    private static final String password = "990122";

    private Connection connection = null;

    public Connection getConnection() {
        return connection;
    }

    public PreparedStatement getStatement() {
        return statement;
    }

    private PreparedStatement statement = null;

    public SQLResultTracer(String sql) {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.connection.close();
            this.statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
