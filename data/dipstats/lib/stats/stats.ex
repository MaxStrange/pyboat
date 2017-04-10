defmodule Stats do
  @moduledoc """
  This module contains the statistics API functions.
  """

  @countries ["E", "A", "F", "G", "R", "I", "T"]

  @doc """
  Sorts the given stream of players by most wins to least wins.
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

  @doc """
  Computes the mean, median, and mode on the given stream or enumerable.

  ## Examples

      iex> Stats.mean_median_mode_stdev(1..10)
      {5.5, 5.5, nil, 3.0276503540974917}

      iex> Stats.mean_median_mode_stdev([5, 5, 5])
      {5, 5, 5, 0.0}
  """
  def mean_median_mode_stdev(stream) do
    mypid = self()
    spawn(fn -> send(mypid, {:mean, Statsfuncs.mean(Stream.take_every(stream, 1))}) end)
    spawn(fn -> send(mypid, {:median, Statsfuncs.median(Stream.take_every(stream, 1))}) end)
    spawn(fn -> send(mypid, {:mode, Statsfuncs.mode(Stream.take_every(stream, 1))}) end)
    spawn(fn -> send(mypid, {:stdev, Statsfuncs.stdev(Stream.take_every(stream, 1))}) end)

    recv_mean_median_mode_stdev()
  end

  defp recv_mean_median_mode_stdev, do: do_recv_mmms %{}
  defp do_recv_mmms(acc) when map_size(acc) != 4 do
    receive do
      {:mean, mean}     -> do_recv_mmms Map.put(acc, :mean, mean)
      {:median, median} -> do_recv_mmms Map.put(acc, :median, median)
      {:mode, mode}     -> do_recv_mmms Map.put(acc, :mode, mode)
      {:stdev, stdev}   -> do_recv_mmms Map.put(acc, :stdev, stdev)
    end
  end
  defp do_recv_mmms(acc), do: acc
end
