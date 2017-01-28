"""
Module for holding the Board class.
"""
import os


class Board:
    def __init__(self, board_config):
        states_as_drawings = board_config["states"]
        drawings = {}
        for state in states_as_drawings:
            drawings.update(state)

        how_to_draw = board_config["board_drawing"]
        instructions = {}
        for layer in how_to_draw:
            instructions.update(layer)

        adjacency_matrix = board_config["board_matrix"]


        self._drawings = drawings
        self._drawing_layers = instructions
        self._ids = board_config["board_ui_ids"]
        self._map_drawing = self._format_map()

    def formatted_display(self):
        """
        Returns the game board as a string to be displayed.
        """
        return self._grid.display()

    def _format_map(self):
        """
        Formats the background map once, so we don't have to do
        it everytime we display the map.
        """
        # Get the tallest item in each of the layers
        layers_heights = []
        for layer_name, state_names in self._drawing_layers.items():
            states_heights = []
            for name in state_names:
                state_drawing = self._drawings[name]
                states_height = 0
                for char in state_drawing:
                    if char == os.linesep:
                        states_height += 1
                states_heights.append((name, states_height))
            layers_heights.append(states_heights)
        max_heights = [max([tup[1] for tup in ls]) for ls in layers_heights]
        # We now have a list of numbers which correspond to the number
        # of layers of text within a layer

        # So now, for each layer, for each layer in text, write that
        # whole layer of text
        overall_map = ""
        h = 0
        for layer_name, state_names in sorted(self._drawing_layers.items()):
            layer_text = ""
            for i in range(max_heights[h]):
                for name in state_names:
                    state_drawing = self._drawings[name]
                    state_drawing = state_drawing.split(os.linesep)
                    state_drawing_layer = state_drawing[i]
                    for char in state_drawing_layer:
                        if char == os.linesep:
                            break
                        else:
                            layer_text += char
                layer_text += os.linesep
            h += 1
            overall_map += layer_text

        # Feed the result into the Grid object so it can keep track
        # of the text from now on
        self._grid = Grid(overall_map, self._ids, self._drawings,\
                self._drawing_layers)


class Grid:
    """
    Representation of the board as a grid of text lines. Gives
    ways to access the states and units within this grid as
    regions within that grid.
    """
    def __init__(self, text, state_ids, state_texts, layers):
        self.text_grid = [line for line in text.split(os.linesep)]
        for id_name_pair in state_ids:
            # Each YAML id_name_pair is a dict of one key to one val
            for k, v in id_name_pair.items():
                state_id, state_name = k, v
                drawing = state_texts[state_name]
                # Assign an x,y tuple to each char in the drawing
                # where the x,y is a point in the overall grid

        # Leave off here TODO
        assert(False)
        exit(0)












