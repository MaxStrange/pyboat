defmodule DatabaseTest do
  use ExUnit.Case
  doctest Database

  test "sql_players(\"WHERE game_id=123458 AND won=1\")" do
    Database.sql_players("WHERE game_id=123458 AND won=1")
  end
end
