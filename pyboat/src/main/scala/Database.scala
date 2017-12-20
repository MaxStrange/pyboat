import java.sql.DriverManager
import java.sql.Connection
import scala.io.Source
import java.io.IOException

object Database {
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://10.75.6.229/diplomacy?autoReconnect=true&useSSL=false"
  val username = "maxst"
  val password = readPasswordFromFile()

  def readPasswordFromFile() : String = {
    for (line <- Source.fromFile("password.secrets").getLines()) {
      if (line != "") {
        return line
      }
    }
    throw new IOException("Could not get the password from the password file.")
  }

  def getTurn(gameId: Int, turnNum: Int) : Turn = {
    val sqlStatement = "SELECT * FROM turns WHERE game_id=" + gameId + " AND turn_num=" + turnNum
    var (rs, connection) = query(sqlStatement)
    //implicitly assuming that there is only one row in the resultSet (there is no good way to assert this)

    rs.next()
    val rsGameId = rs.getInt("game_id")
    val rsTurnNum = rs.getInt("turn_num")
    val year = rs.getInt("year")
    val season : SeasonType = rs.getString("season") match {
      case "Winter" => Winter()
      case "Spring" => Spring()
      case "Fall" => Fall()
    }
    val phase : PhaseType = rs.getString("phase") match {
      case "Winter" => WinterPhase()
      case "Orders" => Orders()
      case "Build" => Build()
      case "Retreat" => Retreat()
    }
    connection.close()

    require(gameId == rsGameId)
    require(turnNum == rsTurnNum)

    return new Turn(gameId, turnNum, phase, year, season)
  }

  /**
   * Don't forget to close the connection!
   */
  def query(sqlStatement: String) : (java.sql.ResultSet, Connection) = {
    var connection : Connection = null
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val stat = connection.createStatement()
    val rs = stat.executeQuery(sqlStatement)
    return (rs, connection)
  }
}
