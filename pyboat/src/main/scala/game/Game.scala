import scala.collection.mutable.ListBuffer

/**
 * Game represents an entire game from the PlayDiplomacy database.
 * The default constructor takes a single Int (a game ID) and finds it
 * in the database, then populates itself from what it finds.
 */
class Game(val gameId: Int) {
  val (id, numTurns, numPlayers) = Database.getGame(gameId)
  require(id == gameId)

  var lb = new ListBuffer[Turn]()
  var turn = Database.getTurn(gameId, 0)
  lb += turn
  for (i <- 1 until numTurns - 1) {
    turn = turn.deriveNext()
    lb += turn
  }
  val turns = lb.toList

  /**
   * Returns a String representation of the game that is useful for debugging.
   * This representation contains every order on every turn and what the game looks like
   * at the end of each turn.
   */
  def historyString() : String = {
    var sb = StringBuilder.newBuilder
    sb.append("--------------------------------------------------------------------\n")
    sb.append(" Game " + id + " numTurns: " + numTurns + " numPlayers: " + numPlayers + "\n")
    sb.append("--------------------------------------------------------------------\n")
    for (t <- turns) {
      sb.append(t.historyString())
      sb.append("\n")
    }
    return sb.toString()
  }
}
