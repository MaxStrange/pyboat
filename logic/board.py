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

    def formatted_display(self):
        """
        Returns the game board as a string to be displayed.
        """
        s = os.linesep
        # Combine the artwork to get each row of text

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

        # TODO: parse out the [U] tags and place the correct units
        return overall_map









