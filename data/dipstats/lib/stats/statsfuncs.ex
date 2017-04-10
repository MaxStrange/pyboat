defmodule Statsfuncs do
  @moduledoc """
  This module is for providing project-agnostic stats functions. This is so that I may be able to reuse it in the future.
  """

  @doc """
  Calculates the mean of the given enumerable.

  ## Examples

      iex> Statsfuncs.mean 1..4
      2.5

      iex> Statsfuncs.mean []
      nil

  """
  def mean([]), do: nil
  def mean(items) do
    numerator = items |> Enum.to_list |> Enum.sum
    denom = items |> Enum.to_list |> length
    numerator / denom
  end

  @doc """
  Calculates the median of the given enumerable.

  ## Examples

      iex> Statsfuncs.median 1..4
      2.5

      iex> Statsfuncs.median [1, 2, 3, 4, 5, 100]
      3.5

      iex> Statsfuncs.median [1, 2, 20]
      2

      iex> Statsfuncs.median []
      nil
  """
  def median(items) do
    do_median(items)
  end

  defp do_median([]), do: nil
  defp do_median([val]), do: val
  defp do_median([l, r]), do: (l + r) / 2
  defp do_median(items) do
    items
    |> Stream.drop(1)
    |> Stream.drop(-1)
    |> Enum.to_list
    |> do_median
  end

  @doc """
  Calculates the mode of the given enumerable. If there is no mode (i.e., all elements of the enumerable
  are unique or there is a tie), then it returns nil.

  ## Examples

      iex> Statsfuncs.mode 1..4
      nil

      iex> Statsfuncs.mode [1, 2, 3, 4, 5, 3]
      3

      iex> Statsfuncs.mode []
      nil

      iex> Statsfuncs.mode [1, 1, 2, 2]
      nil
  """
  def mode([]), do: nil
  def mode(items) do
    chunked =
        items
        |> Enum.to_list             #[1, 5, 3, 9, 2, 2, 8, 1]
        |> Enum.sort                #[1, 1, 2, 2, 3, 5, 8, 9]
        |> Enum.chunk_by(&(&1))     #[[1, 1], [2, 2], [3], [5], [8], [9]]

    # Get the largest number of common items
    largest_num_common_items =
        chunked
        |> Enum.map(&(length(&1)))
        |> Enum.reduce(0, fn(x, acc) -> if (x > acc), do: x, else: acc end)

    # Check the length of each chunk to see if there is more than one list of this length
    more_than_one_largest? =
        chunked
        |> Enum.map(&(length(&1)))
        |> Enum.filter(&(&1 != largest_num_common_items))
        |> unique?

    if more_than_one_largest? do
      nil
    else
      # If there isn't, return the value of the elements in the largest chunk
      largest_chunk = chunked |> Enum.sort(&(length(&1) >= length(&2))) |> Enum.fetch!(0)
      Enum.fetch!(largest_chunk, 0)
    end
  end

  defp unique?(items) do
    length(items) == length(Enum.uniq items)
  end

  @doc """
  Calculates the SAMPLE standard deviation for the given enumerable.

  ## Examples

      iex> Statsfuncs.stdev 1..3
      1.0

      iex> Statsfuncs.stdev [1, 2, 3, 3, 3]
      0.8944271909999159

      iex> Statsfuncs.stdev []
      nil
  """
  def stdev([]), do: nil
  def stdev(items) do
    mu = mean items
    n = length(Enum.to_list(items))
    ss = items |> Enum.map(fn(x) -> (x - mu) * (x - mu) end) |> Enum.sum
    :math.sqrt((1 / (n - 1)) * ss)
  end
end
