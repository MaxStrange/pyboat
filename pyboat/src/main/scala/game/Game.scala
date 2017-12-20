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
  for (i <- 1 until numTurns - 1) {
    turn = turn.deriveNext()
    lb += turn
  }
  val turns = lb.toList

  for (t <- turns) {
    println(t)
  }
}
