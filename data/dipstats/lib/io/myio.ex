defmodule Myio do
  @moduledoc """
  This module is where I am storing my IO functions. Especially for formatting output into charts and for
  maybe communicating with external processes, such as python for matplotlib.
  """

  @doc ~S"""
  Prints the given data as a prettily-formatted table to standard output.
  You must supply all data as a string. This will not convert the data to string representation for you.

  ## Examples

      iex> Myio.print_table(["Name", "ID", "Happy?"], [["John", "5", "Yes"], ["Mary", "2", "No"]])
      Name  |  ID  |  Happy?
      ========================
      John  |  5   |  Yes
      ------------------------
      Mary  |  2   |  No
      ------------------------
  """
  def print_table(column_names, rows) do
    cond do
      Enum.all?(rows, &(length(&1) == length(column_names))) ->
        longests = calculate_table_longests [column_names | rows]
        print_table_content(column_names, rows, longests)
      true ->
        raise ArgumentError, "Each row must be the same length as number of columns."
    end
  end

  defp calculate_table_longests(by_row) do
    # Gets the length of the longest string in each column and returns it as a list by column: [6, 8, 9], where 6 is the length of the longest string in column 0, for example.
    by_row
    |> List.zip
    |> Enum.map(&Tuple.to_list/1)
    |> Enum.map(fn(ls) -> Enum.reduce(ls, 0, fn(x, acc) -> _acc = if (String.length(x) > acc), do: String.length(x), else: acc end) end)
  end

  defp print_table_content(column_names, rows, longests) do
    print_header(column_names, longests)
    print_rest(rows, longests)
  end

  defp print_header(column_names, longests) do
    print_row(column_names, longests)
    print_n_times("=", Enum.sum(longests) + 5 * length(longests) - 2)
    IO.puts ""
  end

  defp print_rest([], _longests), do: IO.puts ""
  defp print_rest(rows, longests) do
    [row | rest] = rows
    print_row(row, longests)
    print_n_times("-", Enum.sum(longests) + 5 * length(longests) - 2)
    print_rest(rest, longests)
  end

  defp print_row([], []), do: IO.puts ""
  defp print_row(row, longests) do
    [s | rest_of_row] = row
    [n | rest_of_longests] = longests
    IO.write s
    times_to_print = if (n - String.length(s) > 0), do: (n - String.length(s)) + 2, else: 2
    print_n_times(" ", times_to_print)
    IO.write "|  "
    print_row(rest_of_row, rest_of_longests)
  end

  defp print_n_times(s, n) do
    Enum.each(1..n, fn(_) -> IO.write s end)
  end
end
