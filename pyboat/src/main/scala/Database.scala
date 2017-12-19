import java.sql.DriverManager
import java.sql.Connection
import scala.io.Source
import java.io.IOException

object Database {
  def readPasswordFromFile() : String = {
    for (line <- Source.fromFile("password.secrets").getLines()) {
      if (line != "") {
        return line
      }
    }
    throw new IOException("Could not get the password from the password file.")
  }

  def test() {
    val driver = "com.mysql.jdbc.Driver"
    val url = "jdbc:mysql://10.75.6.229/diplomacy?autoReconnect=true&useSSL=false"
    val username = "maxst"
    val password = readPasswordFromFile()

    var connection:Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      val statement = connection.createStatement()
      val rs = statement.executeQuery("SELECT * FROM games")//a resultSet object
      val rsmd = rs.getMetaData()//resultSetMetaData object

      //ColumnCount starts from 1
      for (i <- 1 to rsmd.getColumnCount()) {
        val name = rsmd.getColumnName(i)
        println("Name of column " + i + ": " + name)
      }
    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }
}
