defmodule Database do
@moduledoc """
This module is the database accessor. It executes the requested SQL statement and streams it back via the Stream module.
The data that it returns via the Stream module is wrapped up in a map.
"""

  @doc ~S"""
  Executes the given WHERE statements as if they were SELECT * from players WHERE ...
  By default, it will try to return a player struct. But you may override this behavior and instead have it return the values it gets
  as a nameless map by passing the :no_struct arg as the second parameter.
  See the docs for sql_games for an example of this.

  ## Examples

      iex> Database.sql_players("WHERE game_id=123458 AND won=1") |> Enum.to_list
      [%Player{country: "G", eliminated: 0, end_turn: 29, game_id: 123458, num_scs: 13, start_turn: 1, won: 1}]

  """
  def sql_players(sql) do
    {:ok, pid} = Mariaex.start_link(username: "root", database: "diplomacy")
    {:ok, result} = Mariaex.query(pid, "SELECT * FROM players " <> sql)
    Stream.take_every(result.rows, 1)
    |> Stream.map(fn(x) -> arrange_data_player(x) end)
  end
  def sql_players(sql, :no_struct) do
    {:ok, pid} = Mariaex.start_link(username: "root", database: "diplomacy")
    {:ok, result} = Mariaex.query(pid, "SELECT * FROM players " <> sql)
    Stream.take_every(result.rows, 1)
    |> Stream.map(&(arrange_data_anonymous(&1, result.columns)))
  end

  defp arrange_data_player([game_id, country, won, num_scs, eliminated, start_turn, end_turn]) do
    %Player{game_id: game_id, country: country, won: won, num_scs: num_scs, eliminated: eliminated, start_turn: start_turn, end_turn: end_turn}
  end

  defp arrange_data_anonymous(row, columns) do
    Enum.map(columns, &String.to_atom/1)
    |> Enum.zip(row)
    |> Enum.into(%{})
  end

  @doc ~S"""
  Executes the given WHERE statements as if they were SELECT * FROM games WHERE ...
  By default, it will try to return a game struct. You may override this behavior and instead have it return the values in a
  nameless map by passing the :no_struct arg as the second parameter. This is useful for joins, since it will fail when it tries to pack the
  database results into a game struct if you used a join statement.

  ## Examples

      iex> Database.sql_games("WHERE id=123458") |> Enum.to_list
      [%Game{id: 123458, num_players: 7, num_turns: 32}]

      iex> Database.sql_games("INNER JOIN players ON games.id=players.game_id WHERE country='G' AND won=1 AND games.id=123458", :no_struct) |> Enum.to_list
      [%{id: 123458, num_turns: 32, num_players: 7, game_id: 123458, country: "G", won: 1, num_supply_centers: 13, eliminated: 0, start_turn: 1, end_turn: 29}]

  """
  def sql_games(sql) do
    {:ok, pid} = Mariaex.start_link(username: "root", database: "diplomacy")
    {:ok, result} = Mariaex.query(pid, "SELECT * FROM games " <> sql)
    Stream.take_every(result.rows, 1)
    |> Stream.map(&(arrange_data_game(&1)))
  end
  def sql_games(sql, :no_struct) do
    {:ok, pid} = Mariaex.start_link(username: "root", database: "diplomacy")
    {:ok, result} = Mariaex.query(pid, "SELECT * FROM games " <> sql)
    Stream.take_every(result.rows, 1)
    |> Stream.map(&(arrange_data_anonymous(&1, result.columns)))
    |> Enum.to_list
  end

  defp arrange_data_game([id, num_turns, num_players]) do
    %Game{id: id, num_turns: num_turns, num_players: num_players}
  end
end
