import java.sql.*;
public class CheckRoles {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:postgresql://103.176.179.139:5433/mydb";
    try (Connection c = DriverManager.getConnection(url, "admin", "123456")) {
      DatabaseMetaData md = c.getMetaData();
      try (ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"})) {
        while (rs.next()) {
          String name = rs.getString("TABLE_NAME");
          if (name.toLowerCase().contains("role") || name.toLowerCase().contains("user")) {
            System.out.println("TABLE=" + name);
          }
        }
      }
      for (String sql : new String[]{"select * from \"Roles\"", "select * from roles", "select * from \"UserRoles\"", "select * from userroles"}) {
        try (Statement st = c.createStatement()) {
          System.out.println("SQL=" + sql);
          try (ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData rsm = rs.getMetaData();
            int cols = rsm.getColumnCount();
            int row = 0;
            while (rs.next() && row < 10) {
              for (int i = 1; i <= cols; i++) {
                System.out.print(rsm.getColumnName(i) + "=" + rs.getString(i) + (i < cols ? ", " : ""));
              }
              System.out.println();
              row++;
            }
          }
        } catch (Exception ex) {
          System.out.println("ERR=" + ex.getMessage());
        }
      }
    }
  }
}
