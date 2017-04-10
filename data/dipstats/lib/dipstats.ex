defmodule Dipstats do
  @moduledoc """
  This module is the API for the statistics suite. The statistics suite is a batch of Elixir functions that can be run to analyze
  the data in the Diplomacy dataset. Really this is just a fun way for me to learn Elixir.
  """

  @doc """
  Runs the entire statistics suite over the database.
  """
  def stats do
    print_winners()
    print_game_lengths()
  end

  @doc """
  Prints the country names in order of who is most likely to win.
  """
  def print_winners do
    {countries, wins} =
        Database.sql_players("WHERE won=1")
        |> Stats.sort_by_wins
        |> unzip

    str_countries = countries |> Enum.map(fn(x) -> Atom.to_string(x) end)
    str_wins = wins |> Enum.map(fn(x) -> Integer.to_string(x) end)
    Myio.print_table([str_wins], str_countries, "GAMES")
  end

  defp unzip(kwlist), do: {Keyword.keys(kwlist), Keyword.values(kwlist)}

  @doc """
  Prints the mean, median, mode, and standard deviation for game length.
  """
  def print_game_lengths do
    Database.sql_games("WHERE num_players=7")
    |> Stream.map(&(&1.num_turns))
    |> Stats.mean_median_mode_stdev
    |> Map.to_list
    |> Enum.map(fn({name, val}) -> {Atom.to_string(name), (if (is_float(val)), do: Float.to_string(val), else: Integer.to_string(val))} end)
    |> Enum.map(&Tuple.to_list/1)
    |> Myio.print_table(["Statistic", "Value"], "GAME LENGTHS")
  end
end
