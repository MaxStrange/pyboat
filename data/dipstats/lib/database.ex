defmodule Database do
@moduledoc """
This module is the database accessor. It executes the requested SQL statement and streams it back via the Stream module.
The data that it returns via the Stream module is wrapped up in a map.
"""

  @doc """
  Executes the given WHERE statements as if they were SELECT * from players WHERE ...

  ## Examples TODO

      iex> Database.sql_players("WHERE game_id=123458 AND won=1") |> Enum.to_list
      [%Player{country: "G", eliminated: 0, end_turn: 29, game_id: 123458, num_scs: 13, start_turn: 1, won: 1}]

  """
  def sql_players(sql) do
    {:ok, pid} = Mariaex.start_link(username: "root", database: "diplomacy")
    {:ok, result} = Mariaex.query(pid, "SELECT * FROM players " <> sql)
    Stream.take_every(result.rows, 1)
    |> Stream.map(fn(x) -> arrange_data_player(x) end)
  end

  defp arrange_data_player([game_id, country, won, num_scs, eliminated, start_turn, end_turn]) do
    %Player{game_id: game_id, country: country, won: won, num_scs: num_scs, eliminated: eliminated, start_turn: start_turn, end_turn: end_turn}
  end

end
