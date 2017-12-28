package pyboat.game

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.INDArrayIndex
import org.nd4j.linalg.indexing.NDArrayIndex

/**
 * BoardState represents the state of the game board at a given turn.
 *
 * This class has units and a location ownership matrix.
 * @param units The list of DipUnit objects present on the board.
 * @param ownershipMatrix A Map of the form Map(location: String -> player: CountryType)
 */
class BoardState(val units: List[DipUnit], val ownershipMatrix: Map[String, CountryType]) {
  def getUnit(unitId: Int) : DipUnit = {
    for (u <- units) {
      if (u.unitId == unitId)
        return u
    }
    throw new NullPointerException("No unit with unitId: " + unitId)
  }

  /**
   * Returns a String representation of the map with
   * the value 1 at loc and 0 everywhere else.
   */
  def testLookupTable(loc: String) : String = {
    require(AllowedLocations.contains(loc))
    var arr = Nd4j.zeros(1, 21, 21)
    arr = putValueByLocation(1, loc, arr)
    return "=================" + loc + "==============\n" + arr.toString()
  }

  /**
   * Returns a String representation of the map
   * with a different value for each location, where each
   * value is part of a linear space across the range [0, 255].
   */
  def testAllMasks() : String = {
    var arr = Nd4j.zeros(1, 21, 21)
    val vals = Nd4j.linspace(0, 255, AllowedLocations.allowedLocations.size)
    var index = 0
    for (loc <- AllowedLocations.allowedLocations) {
      val v = vals.getDouble(index).toInt
      arr = putValueByLocation(v, loc, arr)
      index += 1
    }
    return arr.toString()
  }

  def historyString() : String = {
    var sb = StringBuilder.newBuilder
    sb.append("    BOARD STATE:\n")
    for (u <- units) {
      sb.append("      " + u + "\n")
    }
    sb.append("\n")
    for ((location, owner) <- ownershipMatrix) {
      sb.append("      " + location + " owned by " + owner + "\n")
    }
    return sb.toString()
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> no unit here
   * 255 -> unit present
   */
  def getUnitMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (u <- units) {
      arr = putValueByLocation(255, u.location, arr)
    }
    return arr
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> unit is not present
   * 128 -> unit is an army
   * 255 -> unit is a fleet
   */
  def getUnitTypeMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (u <- units) {
      val v = if (u.unitType == Army()) 128 else 255
      arr = putValueByLocation(v, u.location, arr)
    }
    return arr
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> unit is owned by nobody
   * 36  -> unit is owned by Austria
   * 72  -> unit is owned by England
   * 109 -> unit is owned by France
   * 145 -> unit is owned by Germany
   * 182 -> unit is owned by Italy
   * 218 -> unit is owned by Russia
   * 255 -> unit is owned by Turkey
   */
  def getUnitOwnershipMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (u <- units) {
      val v = u.country match {
        case Austria() => 36
        case England() => 72
        case France()  => 109
        case Germany() => 145
        case Italy()   => 182
        case Russia()  => 218
        case Turkey()  => 255
      }
      arr = putValueByLocation(v, u.location, arr)
    }
    return arr
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> location is land
   * 128 -> location is sea
   * 255 -> location is impassable
   */
 def getLandTypeMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (loc <- AllowedLocations.allowedLocations) {
      if (AllowedLocations.isWater(loc))
        arr = putValueByLocation(128, loc, arr)
      else if (loc == "Switzerland")
        arr = putValueByLocation(255, loc, arr)
    }
    return arr
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> location is owned by nobody
   * 36  -> location is owned by Austria
   * 72  -> location is owned by England
   * 109 -> location is owned by France
   * 145 -> location is owned by Germany
   * 182 -> location is owned by Italy
   * 218 -> location is owned by Russia
   * 255 -> location is owned by Turkey
   */
  def getLandOwnershipMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (loc <- AllowedLocations.allowedLocations) {
      if (ownershipMatrix.contains(loc)) {
        val v = ownershipMatrix(loc) match {
          case Austria() => 36
          case England() => 72
          case France()  => 109
          case Germany() => 145
          case Italy()   => 182
          case Russia()  => 218
          case Turkey()  => 255
        }
        arr = putValueByLocation(v, loc, arr)
      } else {
        arr = putValueByLocation(0, loc, arr)
      }
    }
    return arr
  }

  /**
   * 1 x 21 x 21 INDArray where:
   * 0   -> location is not an SC
   * 255 -> location is an SC
   */
  def getSCMask() : INDArray = {
    var arr = Nd4j.zeros(1, 21, 21)
    for (loc <- AllowedLocations.allowedLocations) {
      if (AllowedLocations.isSC(loc))
        arr = putValueByLocation(255, loc, arr)
    }
    return arr
  }

  /**
   * Puts the given value `v` into the given INDArray `arr` at the right location as
   * specified by `loc`.
   *
   * @param v The value to put into the Array
   * @param loc The location name
   * @param arr A 1 x 21 x 21 INDArray to put the value into
   */
  def putValueByLocation(v: Double, loc: String, arr: INDArray) : INDArray = {
    require(AllowedLocations.contains(loc))

    val (rIndexes, cIndexes) = getLocationIndexes(loc)
    require(rIndexes.length == cIndexes.length)

    var newArr = arr
    val zero = NDArrayIndex.interval(0, 1)
    for (i <- 0 until rIndexes.length) {
      val (rIndex, cIndex) = (rIndexes(i), cIndexes(i))
      val vs = Nd4j.ones(1, rIndex.length().toInt, cIndex.length().toInt).mul(v)
      newArr.put(Array(zero, rIndex, cIndex), vs)
    }
    return newArr
  }

  /**
   * Looks up the given location `loc` in a big table and returns a List of row indexes and
   * a corresponding List of column indexes that describe the country or water location.
   *
   * @example London => (List[NDArrayIndex]{2 until 4}, List[NDArrayIndex{3 until 4})
   *          North Atlantic Ocean => (List[NDArrayIndex]{0 until 1, 1 until 2, 2 until 3},
                                       List[NDArrayIndex]{0 until 3, 0 until 2, 0 until 3})
   */
  private def getLocationIndexes(loc: String) : (List[INDArrayIndex], List[INDArrayIndex]) = {
    val (rIndexes, cIndexes): (List[INDArrayIndex], List[INDArrayIndex]) = loc match {
      case "Edinburgh" =>       (List(NDArrayIndex.interval(1, 3)), List(NDArrayIndex.interval(4, 5)))
      case "Liverpool" =>       (List(NDArrayIndex.interval(2, 4)), List(NDArrayIndex.interval(3, 4)))
      case "London" =>          (List(NDArrayIndex.interval(5, 6)), List(NDArrayIndex.interval(4, 6)))
      case "Marseilles" =>      (List(NDArrayIndex.interval(11, 14)), List(NDArrayIndex.interval(4, 6)))
      case "Paris" =>           (List(NDArrayIndex.interval(9, 10)), List(NDArrayIndex.interval(2, 4)))
      case "Brest" =>           (List(NDArrayIndex.interval(8, 10)), List(NDArrayIndex.interval(1, 2)))
      case "Venice" =>          (List(NDArrayIndex.interval(14, 17)), List(NDArrayIndex.interval(7, 9)))
      case "Rome" =>            (List(NDArrayIndex.interval(17, 19)), List(NDArrayIndex.interval(7, 8)))
      case "Naples" =>          (List(NDArrayIndex.interval(19, 20)), List(NDArrayIndex.interval(7, 10)))
      case "Munich" =>          (List(NDArrayIndex.interval(10, 11)), List(NDArrayIndex.interval(6, 12)))
      case "Berlin" =>          (List(NDArrayIndex.interval(8, 10)), List(NDArrayIndex.interval(11, 13)))
      case "Kiel" =>            (List(NDArrayIndex.interval(8, 10)), List(NDArrayIndex.interval(7, 11)))
      case "Vienna" =>          (List(NDArrayIndex.interval(12, 13)), List(NDArrayIndex.interval(9, 13)))
      case "Trieste" =>         (List(NDArrayIndex.interval(13, 16)), List(NDArrayIndex.interval(9, 12)))
      case "Budapest" =>        (List(NDArrayIndex.interval(13, 14)), List(NDArrayIndex.interval(12, 15)))
      case "Constantinople" =>  (List(NDArrayIndex.interval(16, 19)), List(NDArrayIndex.interval(18, 19)))
      case "Ankara" =>          (List(NDArrayIndex.interval(16, 18)), List(NDArrayIndex.interval(19, 20)))
      case "Smyrna" =>          (List(NDArrayIndex.interval(18, 20)), List(NDArrayIndex.interval(19, 20)))
      case "Moscow" =>          (List(NDArrayIndex.interval(9, 11)), List(NDArrayIndex.interval(16, 21)))
      case "St. Petersburg (South Coast)" =>
                                (List(NDArrayIndex.interval(5, 9)), List(NDArrayIndex.interval(20, 21)))
      case "Warsaw" =>          (List(NDArrayIndex.interval(10, 12)), List(NDArrayIndex.interval(14, 16)))
      case "Sevastopol" =>      (List(NDArrayIndex.interval(11, 12)), List(NDArrayIndex.interval(17, 21)))
      case "Norwegian Sea" =>   (List(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(1, 3)),
                                 List(NDArrayIndex.interval(3, 11), NDArrayIndex.interval(9, 11)))
      case "Yorkshire" =>       (List(NDArrayIndex.interval(3, 5)), List(NDArrayIndex.interval(4, 5)))
      case "North Sea" =>       (List(NDArrayIndex.interval(1, 4), NDArrayIndex.interval(4, 5), NDArrayIndex.interval(5, 6), NDArrayIndex.interval(6, 7)),
                                 List(NDArrayIndex.interval(5, 9), NDArrayIndex.interval(5, 8), NDArrayIndex.interval(5, 9), NDArrayIndex.interval(5, 8)))
      case "Spain" =>           (List(NDArrayIndex.interval(12, 13), NDArrayIndex.interval(13, 16)),
                                 List(NDArrayIndex.interval(2, 4), NDArrayIndex.interval(3, 4)))
      case "Picardy" =>         (List(NDArrayIndex.interval(8, 9)), List(NDArrayIndex.interval(2, 5)))
      case "Mid-Atlantic Ocean" =>
                                (List(NDArrayIndex.interval(3, 19), NDArrayIndex.interval(12, 15), NDArrayIndex.interval(15, 16)),
                                 List(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(0, 2), NDArrayIndex.interval(0, 3)))
      case "Tyrrhenian Sea" =>  (List(NDArrayIndex.interval(17, 19), NDArrayIndex.interval(18, 20)),
                                 List(NDArrayIndex.interval(4, 5), NDArrayIndex.interval(5, 7)))
      case "Silesia" =>         (List(NDArrayIndex.interval(10, 11)), List(NDArrayIndex.interval(12, 14)))
      case "Holland" =>         (List(NDArrayIndex.interval(7, 8)), List(NDArrayIndex.interval(6, 8)))
      case "Albania" =>         (List(NDArrayIndex.interval(16, 17)), List(NDArrayIndex.interval(11, 13)))
      case "Serbia" =>          (List(NDArrayIndex.interval(14, 16)), List(NDArrayIndex.interval(12, 15)))
      case "Bulgaria" =>        (List(NDArrayIndex.interval(15, 18)), List(NDArrayIndex.interval(15, 18)))
      case "St. Petersburg" =>  (List(NDArrayIndex.interval(3, 9)), List(NDArrayIndex.interval(20, 21)))
      case "Gulf of Bothnia" => (List(NDArrayIndex.interval(6, 8)), List(NDArrayIndex.interval(15, 20)))
      case "Ukraine" =>         (List(NDArrayIndex.interval(11, 12)), List(NDArrayIndex.interval(15, 17)))
      case "Norway" =>          (List(NDArrayIndex.interval(3, 4)), List(NDArrayIndex.interval(9, 20)))
      case "Belgium" =>         (List(NDArrayIndex.interval(7, 9)), List(NDArrayIndex.interval(5, 6)))
      case "Portugal" =>        (List(NDArrayIndex.interval(13, 15)), List(NDArrayIndex.interval(2, 3)))
      case "Piedmont" =>        (List(NDArrayIndex.interval(12, 15)), List(NDArrayIndex.interval(5, 7)))
      case "Tunis" =>           (List(NDArrayIndex.interval(19, 21)), List(NDArrayIndex.interval(3, 5)))
      case "Galicia" =>         (List(NDArrayIndex.interval(11, 13)), List(NDArrayIndex.interval(13, 15)))
      case "Greece" =>          (List(NDArrayIndex.interval(16, 18)), List(NDArrayIndex.interval(13, 15)))
      case "Black Sea" =>       (List(NDArrayIndex.interval(12, 16)), List(NDArrayIndex.interval(18, 20)))
      case "Rumania" =>         (List(NDArrayIndex.interval(12, 15)), List(NDArrayIndex.interval(15, 18)))
      case "Sweden" =>          (List(NDArrayIndex.interval(4, 6)), List(NDArrayIndex.interval(10, 16)))
      case "Finland" =>         (List(NDArrayIndex.interval(4, 6)), List(NDArrayIndex.interval(16, 20)))
      case "Helgoland Bight" => (List(NDArrayIndex.interval(6, 8)), List(NDArrayIndex.interval(8, 9)))
      case "Burgundy" =>        (List(NDArrayIndex.interval(9, 11)), List(NDArrayIndex.interval(4, 6)))
      case "English Channel" => (List(NDArrayIndex.interval(6, 8)), List(NDArrayIndex.interval(1, 5)))
      case "Tyrolia" =>         (List(NDArrayIndex.interval(11, 14)), List(NDArrayIndex.interval(7, 9)))
      case "Ionian Sea" =>      (List(NDArrayIndex.interval(17, 18), NDArrayIndex.interval(18, 20), NDArrayIndex.interval(20, 21)),
                                 List(NDArrayIndex.interval(10, 13), NDArrayIndex.interval(10, 14), NDArrayIndex.interval(5, 14)))
      case "Denmark" =>         (List(NDArrayIndex.interval(5, 8)), List(NDArrayIndex.interval(9, 10)))
      case "Adriatic Sea" =>    (List(NDArrayIndex.interval(16, 17)), List(NDArrayIndex.interval(9, 11)))
      case "Wales" =>           (List(NDArrayIndex.interval(4, 6)), List(NDArrayIndex.interval(3, 4)))
      case "Irish Sea" =>       (List(NDArrayIndex.interval(3, 6)), List(NDArrayIndex.interval(1, 3)))
      case "Armenia" =>         (List(NDArrayIndex.interval(12, 19)), List(NDArrayIndex.interval(20, 21)))
      case "St. Petersburg (North Coast)" =>
                                (List(NDArrayIndex.interval(3, 4)), List(NDArrayIndex.interval(20, 21)))
      case "North Atlantic Ocean" =>
                                (List(NDArrayIndex.interval(0, 1), NDArrayIndex.interval(1, 2), NDArrayIndex.interval(2, 3)),
                                 List(NDArrayIndex.interval(0, 3), NDArrayIndex.interval(0, 2), NDArrayIndex.interval(0, 3)))
      case "Ruhr" =>            (List(NDArrayIndex.interval(8, 10)), List(NDArrayIndex.interval(6, 7)))
      case "Apulia" =>          (List(NDArrayIndex.interval(17, 19)), List(NDArrayIndex.interval(8, 10)))
      case "Aegean Sea" =>      (List(NDArrayIndex.interval(18, 19), NDArrayIndex.interval(19, 20)),
                                 List(NDArrayIndex.interval(14, 18), NDArrayIndex.interval(14, 19)))
      case "Skagerrack" =>      (List(NDArrayIndex.interval(4, 5)), List(NDArrayIndex.interval(8, 10)))
      case "Gascony" =>         (List(NDArrayIndex.interval(10, 12)), List(NDArrayIndex.interval(1, 4)))
      case "Prussia" =>         (List(NDArrayIndex.interval(8, 10)), List(NDArrayIndex.interval(13, 14)))
      case "Barents Sea" =>     (List(NDArrayIndex.interval(0, 3)), List(NDArrayIndex.interval(11, 21)))
      case "Bohemia" =>         (List(NDArrayIndex.interval(11, 12)), List(NDArrayIndex.interval(9, 13)))
      case "Western Mediterranean" =>
                                (List(NDArrayIndex.interval(16, 19)), List(NDArrayIndex.interval(1, 4)))
      case "Tuscany" =>         (List(NDArrayIndex.interval(15, 18)), List(NDArrayIndex.interval(5, 7)))
      case "Baltic Sea" =>      (List(NDArrayIndex.interval(6, 8)), List(NDArrayIndex.interval(10, 15)))
      case "Gulf of Lyons" =>   (List(NDArrayIndex.interval(14, 17)), List(NDArrayIndex.interval(4, 5)))
      case "Eastern Mediterranean" =>
                                (List(NDArrayIndex.interval(20, 21)), List(NDArrayIndex.interval(14, 20)))
      case "Spain (North Coast)" =>
                                (List(NDArrayIndex.interval(12, 13)), List(NDArrayIndex.interval(2, 3)))
      case "Livonia" =>         (List(NDArrayIndex.interval(8, 9)), List(NDArrayIndex.interval(14, 20)))
      case "Syria" =>           (List(NDArrayIndex.interval(19, 21)), List(NDArrayIndex.interval(20, 21)))
      case "Clyde" =>           (List(NDArrayIndex.interval(1, 2)), List(NDArrayIndex.interval(2, 4)))
      case "North Africa" =>    (List(NDArrayIndex.interval(19, 21)), List(NDArrayIndex.interval(0, 3)))
      case "Spain (South Coast)" =>
                                (List(NDArrayIndex.interval(13, 16)), List(NDArrayIndex.interval(3, 4)))
      case "Bulgaria (South Coast)" =>
                                (List(NDArrayIndex.interval(17, 18)), List(NDArrayIndex.interval(15, 18)))
      case "Bulgaria (East Coast)" =>
                                (List(NDArrayIndex.interval(15, 17)), List(NDArrayIndex.interval(16, 18)))
      case "Switzerland" =>     (List(NDArrayIndex.interval(11, 12)), List(NDArrayIndex.interval(5, 7)))
    }
    return (rIndexes, cIndexes)
  }
}
