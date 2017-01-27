"""
This is the MVC API for the model.
"""
from logic.board import Board
import yaml


class GameState:
    def __init__(self, config_file):
        """
        Does any initialization necessary.
        """
        # Parse the YAML file and set up the state from its parameters
        with open(config_file) as f:
            try:
                configs = yaml.load(f)
            except yaml.YAMLError as e:
                print("Problem with the YAML file.")
                print(e)
                exit(-1)

        board_config = configs["board"]
        self.board = Board(board_config)

    def game_over(self):
        """
        Returns if the game is over or not.
        """
        # TODO
        pass





