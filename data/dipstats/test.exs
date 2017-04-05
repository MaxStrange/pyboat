defmodule Blahmod do
  def blah(stream) do
    Enum.each(1..4, fn(_) -> spawn(fn -> IO.inspect stream end) end)
  end
end

Stream.take_every(1..2, 1) |> Blahmod.blah
