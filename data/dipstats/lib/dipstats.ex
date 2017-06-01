defmodule Dipstats do
  @moduledoc """
  This module is the API for the statistics suite. The statistics suite is a batch of Elixir functions that can be run to analyze
  the data in the Diplomacy dataset. Really this is just a fun way for me to learn Elixir.
  """
  alias Expyplot.Plot

  @countries [:england, :france, :russia, :austria, :italy, :turkey, :germany]

  @doc """
  Runs the entire statistics suite over the database.
  """
  def stats do
    winners()
    game_lengths()
  end

  @doc """
  Prints the country names in order of who is most likely to win.
  """
  def winners do
    {countries, wins} =
        Database.sql_players("WHERE won=1")
        |> Stats.sort_by_wins
        |> unzip

    str_countries = countries |> Enum.map(fn(x) -> Atom.to_string(x) end)
    str_wins = wins |> Enum.map(fn(x) -> Integer.to_string(x) end)
    Myio.print_table([str_wins], str_countries, "VICTORIES BY COUNTRY")
  end

  defp unzip(kwlist), do: {Keyword.keys(kwlist), Keyword.values(kwlist)}

  @doc """
  Prints the mean, median, mode, and standard deviation for game length.
  """
  def game_lengths do
    Database.sql_games("WHERE num_players=7")
    |> Stream.map(&(&1.num_turns))
    |> Stats.mean_median_mode_stdev
    |> Map.to_list
    |> Enum.map(fn({name, val}) -> {Atom.to_string(name), (if (is_float(val)), do: Float.to_string(val), else: Integer.to_string(val))} end)
    |> Enum.map(&Tuple.to_list/1)
    |> Myio.print_table(["Statistic", "Value"], "GAME LENGTHS")

    Database.sql_games("WHERE num_players=7")
    |> Stream.map(&(&1.num_turns))
    |> Enum.to_list
    |> Plot.hist(bins: 100)

    Plot.title("Number of turns")
    Plot.xlabel("Number of turns")
    Plot.ylabel("Number of games")
    Plot.show()
  end

  @doc """
  Prints the mean, median, mode, and standard deviation for game length where the given country is the winner.
  Also, only considers games that have all seven players.
  """
  def game_lengths_for(country) when not country in @countries do
    IO.puts "Need one of:"
    IO.inspect @countries
  end
  def game_lengths_for(country) do
    letter = country |> Atom.to_string |> String.first |> String.upcase
    Database.sql_games("INNER JOIN players ON games.id=players.game_id WHERE country='" <> letter <> "' AND won=1 AND games.num_players=7", :no_struct)
    |> Stream.map(&(&1.num_turns))
    |> Stats.mean_median_mode_stdev
    |> Map.to_list
    |> Enum.map(fn({name, val}) -> {Atom.to_string(name), (if (is_float(val)), do: Float.to_string(val), else: Integer.to_string(val))} end)
    |> Enum.map(&Tuple.to_list/1)
    |> Myio.print_table(["Statistic", "Value"], "GAME LENGTHS")
  end

  @doc """
  Prints the mean, median, mode, and stdev for the length of time that the given country played before
  being eliminated. Only looks at games where the starting number of players was 7 and where this country
  was eliminated.
  """
  def lifespan(country) when not country in @countries do
    IO.puts "Need one of:"
    IO.inspect @countries
  end
  def lifespan(country) do
    letter = country |> Atom.to_string |> String.first |> String.upcase
    stats =
        Database.sql_games("INNER JOIN players ON games.id=players.game_id WHERE country='" <> letter <> "' AND eliminated=1 AND games.num_players=7", :no_struct)
        |> Stream.map(&(&1.end_turn))
        |> Enum.to_list

    stats
    |> Stats.mean_median_mode_stdev
    |> Map.to_list
    |> Enum.map(fn({name, val}) -> {Atom.to_string(name), (if (is_float(val)), do: Float.to_string(val), else: Integer.to_string(val))} end)
    |> Enum.map(&Tuple.to_list/1)
    |> Myio.print_table(["Statistic", "Value"], "NUM TURNS BEFORE ELIM")

    Plot.hist(stats, bins: 100)
    Plot.show()
  end

  @doc """
  Graphs the typical number of SCs each country has given that the selected country won over the course of the game.
  The idea is that this will give a typical example of what a game looks like for England, given that France wins (for example).
  It will begin to answer the question, what should I do as country X in order to have the best possible chance to win?
  """
  def typical_win(country) when not country in @countries do
    IO.puts "Need one of:"
    IO.inspect @countries
  end
  def typical_win(country) do
    # get all turns from all games where country won
    letter = country |> Atom.to_string |> String.first |> String.upcase
    # Accumulate the turns into totals. Totals will look like this: %{austria: %{<turn number>: {<sum_SCs>, <num_turns_at_this_number>}}, england: etc.}
    totals =
        Database.sql_games("INNER JOIN players ON games.id=players.game_id INNER JOIN turns ON games.id=turns.game_id WHERE country='" <> letter <> "' AND won=1 AND num_players=7", :no_struct)
        #TODO
  end
end

