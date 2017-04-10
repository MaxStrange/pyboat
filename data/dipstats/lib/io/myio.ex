defmodule Myio do
  @moduledoc """
  This module is where I am storing my IO functions. Especially for formatting output into charts and for
  maybe communicating with external processes, such as python for matplotlib.
  """

  @doc ~S"""
  Prints the given data as a prettily-formatted table to standard output.
  You must supply all data as a string. This will not convert the data to string representation for you.

  ## Examples

      iex> Myio.print_table([["John", "5", "Yes"], ["Mary", "2", "No"]], ["Name", "ID", "Happy?"], "PEOPLE")
      ========================
      |        PEOPLE        |
      ========================
      Name  |  ID  |  Happy?
      ========================
      John  |  5   |  Yes
      ------------------------
      Mary  |  2   |  No
      ------------------------
  """
  def print_table(rows, column_names, title) do
    cond do
      Enum.all?(rows, &(length(&1) == length(column_names))) ->
        longests = calculate_table_longests [column_names | rows]
        print_table_content(column_names, rows, longests, title)
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

  defp print_table_content(column_names, rows, longests, title) do
    print_title(title, longests)
    print_header(column_names, longests)
    print_rest(rows, longests)
  end

  defp print_title(title, longests) do
    table_length = Enum.sum(longests) + 5 * length(longests) - 2
    print_n_times("=", table_length)
    IO.puts ""
    IO.write "|"
    color_title = IO.ANSI.format([:blue, :bright, title], true)
    total_pad = (table_length - String.length(title)) - 2
    case total_pad do
      total_pad when total_pad <= 0 ->
        IO.write "  "
        IO.write color_title
        IO.write "  "
      total_pad when rem(total_pad, 2) == 0 ->
        print_n_times(" ", round(total_pad / 2))
        IO.write color_title
        print_n_times(" ", round(total_pad / 2))
      total_pad when rem(total_pad, 2) == 1 ->
        print_n_times(" ", round(total_pad / 2) - 1)
        IO.write color_title
        print_n_times(" ", round(total_pad / 2))
    end
    IO.write "|"
    IO.puts ""
    print_n_times("=", table_length)
    IO.puts ""
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
    IO.puts ""
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
