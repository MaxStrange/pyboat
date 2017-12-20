/**
 * Game represents an entire game from the PlayDiplomacy database.
 * The default constructor takes a single Int (a game ID) and finds it
 * in the database, then populates itself from what it finds.
 */
class Game(val gameId: Int) {
  val t = Database.getTurn(gameId, 10)
  println(t)
}
