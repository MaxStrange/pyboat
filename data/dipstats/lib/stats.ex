defmodule Stats do
  @moduledoc """
  This module contains the statistics API functions.
  """

  @countries ["E", "A", "F", "G", "R", "I", "T"]

  @doc """
  Sorts the given stream of players by most wins to least wins and returns some useful information along with it.
  """
  def sort_by_wins(stream) do
    ## Copy the stream into seven different processes, each of which sums their country's wins.
    mypid = self()
    Enum.each(@countries, fn(country) -> spawn(fn -> sum_by_win_country(mypid, country, Stream.take_every(stream, 1)) end)end)

    recv_wins()
    |> Map.to_list
    |> Enum.sort(fn({_name1, num1}, {_name2, num2}) -> num1 >= num2 end)
  end

  defp sum_by_win_country(mypid, country, stream) do
    total_player_wins =
    stream
    |> Stream.filter(&(&1.country == country))
    |> Stream.scan(0, &(&1.won + &2))
    |> Enum.to_list
    |> List.last

    send(mypid, {country, total_player_wins})
  end

  defp recv_wins, do: do_recv_wins %{}
  defp do_recv_wins(acc) when map_size(acc) != 7 do
    receive do
      {"E", england_wins} -> do_recv_wins Map.put(acc, :england, england_wins)
      {"A", austria_wins} -> do_recv_wins Map.put(acc, :austria, austria_wins)
      {"F", france_wins}  -> do_recv_wins Map.put(acc, :france, france_wins)
      {"G", germany_wins} -> do_recv_wins Map.put(acc, :germany, germany_wins)
      {"R", russia_wins}  -> do_recv_wins Map.put(acc, :russia, russia_wins)
      {"I", italy_wins}   -> do_recv_wins Map.put(acc, :italy, italy_wins)
      {"T", turkey_wins}  -> do_recv_wins Map.put(acc, :turkey, turkey_wins)
    end
  end
  defp do_recv_wins(acc), do: acc
end
