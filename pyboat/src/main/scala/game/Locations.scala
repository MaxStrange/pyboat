package pyboat.game

object AllowedLocations {
  val allowedLocations = Set(
    "Edinburgh",
    "Liverpool",
    "London",
    "Marseilles",
    "Paris",
    "Brest",
    "Venice",
    "Rome",
    "Naples",
    "Munich",
    "Berlin",
    "Kiel",
    "Vienna",
    "Trieste",
    "Budapest",
    "Constantinople",
    "Ankara",
    "Smyrna",
    "Moscow",
    "St. Petersburg (South Coast)",
    "Warsaw",
    "Sevastopol",
    "Norwegian Sea",
    "Yorkshire",
    "North Sea",
    "Spain",
    "Picardy",
    "Mid-Atlantic Ocean",
    "Tyrrhenian Sea",
    "Silesia",
    "Holland",
    "Albania",
    "Serbia",
    "Bulgaria",
    "St. Petersburg",
    "Gulf of Bothnia",
    "Ukraine",
    "Norway",
    "Belgium",
    "Portugal",
    "Piedmont",
    "Tunis",
    "Galicia",
    "Greece",
    "Black Sea",
    "Rumania",
    "Sweden",
    "Finland",
    "Helgoland Bight",
    "Burgundy",
    "English Channel",
    "Tyrolia",
    "Ionian Sea",
    "Denmark",
    "Adriatic Sea",
    "Wales",
    "Irish Sea",
    "Armenia",
    "St. Petersburg (North Coast)",
    "North Atlantic Ocean",
    "Ruhr",
    "Apulia",
    "Aegean Sea",
    "Skagerrack",
    "Gascony",
    "Prussia",
    "Barents Sea",
    "Bohemia",
    "Western Mediterranean",
    "Tuscany",
    "Baltic Sea",
    "Gulf of Lyons",
    "Eastern Mediterranean",
    "Spain (North Coast)",
    "Livonia",
    "Syria",
    "Clyde",
    "North Africa",
    "Spain (South Coast)",
    "Bulgaria (South Coast)",
    "Bulgaria (East Coast)",
    "Switzerland"
  )

  def contains(location: String) : Boolean = {
    return allowedLocations.contains(location)
  }

  def isSC(location: String) : Boolean = {
    val tf = location match {
      case "Edinburgh" => true
      case "Liverpool" => true
      case "London" => true
      case "Brest" => true
      case "Spain" => true
      case "Spain (North Coast)" => true
      case "Spain (South Coast)" => true
      case "Portugal" => true
      case "Tunis" => true
      case "Picardy" => true
      case "Paris" => true
      case "Marseilles" => true
      case "Belgium" => true
      case "Holland" => true
      case "Rome" => true
      case "Venice" => true
      case "Naples" => true
      case "Trieste" => true
      case "Serbia" => true
      case "Greece" => true
      case "Bulgaria" => true
      case "Bulgaria (South Coast)" => true
      case "Bulgaria (East Coast)" => true
      case "Constantinople" => true
      case "Smyrna" => true
      case "Ankara" => true
      case "Sevastopol" => true
      case "Budapest" => true
      case "Vienna" => true
      case "Munich" => true
      case "Kiel" => true
      case "Berlin" => true
      case "Warsaw" => true
      case "Rumania" => true
      case "Moscow" => true
      case "Denmark" => true
      case "Norway" => true
      case "Sweden" => true
      case "St. Petersburg (South Coast)" => true
      case "St. Petersburg" => true
      case "St. Petersburg (North Coast)" => true
      case _ => false
    }
    return tf
  }

  def isWater(location: String) : Boolean = {
    val tf = location match {
      case "North Atlantic Ocean" => true
      case "Norwegian Sea" => true
      case "Barents Sea" => true
      case "Gulf of Bothnia" => true
      case "Baltic Sea" => true
      case "Skagerrack" => true
      case "Helgoland Bight" => true
      case "North Sea" => true
      case "Irish Sea" => true
      case "Mid-Atlantic Ocean" => true
      case "English Channel" => true
      case "Western Mediterranean" => true
      case "Gulf of Lyons" => true
      case "Tyrrhenian Sea" => true
      case "Ionian Sea" => true
      case "Adriatic Sea" => true
      case "Aegean Sea" => true
      case "Eastern Mediterranean" => true
      case "Black Sea" => true
      case _ => false
    }
    return tf
  }
}
