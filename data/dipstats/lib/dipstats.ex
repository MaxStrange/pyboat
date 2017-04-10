defmodule Dipstats do
  @moduledoc """
  This module is the API for the statistics suite. The statistics suite is a batch of Elixir functions that can be run to analyze
  the data in the Diplomacy dataset. Really this is just a fun way for me to learn Elixir.
  """

  @doc """
  Runs the entire statistics suite over the database.

  ## Examples

      iex> Dipstats.stats
      :ok

  """
  def stats do
    :ok
  end

  @doc """
  Prints the country names in order of who is most likely to win, along with some frequentist statistics for them.
  """
  def print_winners do
    {countries, wins} =
        Database.sql_players("WHERE won=1")
        |> Stats.sort_by_wins
        |> unzip

    str_countries = countries |> Enum.map(fn(x) -> Atom.to_string(x) end)
    str_wins = wins |> Enum.map(fn(x) -> Integer.to_string(x) end)
    Myio.print_table(str_countries, [str_wins])
  end

  defp unzip(kwlist), do: {Keyword.keys(kwlist), Keyword.values(kwlist)}
end
