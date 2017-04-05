defmodule Player do
@moduledoc """
This is a struct representing an entry in the players table in the database.
"""

  defstruct game_id: 0, country: "A", won: false, num_scs: 0, eliminated: false, start_turn: 0, end_turn: 0
end
