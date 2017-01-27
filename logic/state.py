"""
This is the MVC API for the model.
"""
from logic.board import Board
from logic.order import Order
from logic.order import ParseError
import yaml


class GameState:
    def __init__(self, config_file):
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

    def get_formatted_display(self):
        """
        Returns the board in a view suitable for the user on the
        command line.
        """
        return self.board.formatted_display()

    def orders_are_valid(orders):
        """
        Checks if the given orders are valid. They may be invalid because of
        parsing problems (due to the fact that these are raw inputs from the command
        line) or due to them being illegal. Both of these cases are handled,
        and in these cases, False is returned. If the orders can be parsed and they
        are not illegal, True is returned.
        """
        try:
            parsed_orders = [Order(o) for o in orders.split(';')]
        except ParsingError:
            return False

        # TODO: check each order for legality
        return True










