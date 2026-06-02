import java.sql.*;
public class CheckUsers {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:postgresql://103.176.179.139:5433/mydb";
    try (Connection c = DriverManager.getConnection(url, "admin", "123456");
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("select \"Id\", \"Username\", \"PasswordHash\", \"Status\" from \"Users\" order by \"Id\" desc limit 5")) {
      while (rs.next()) {
        System.out.println(rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4));
      }
    }
  }
}
