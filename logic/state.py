"""
This is the MVC API for the model.
"""
import yaml

def initialize(config_file):
    """
    Does any initialization necessary.
    """
    with open(config_file) as f:
        try:
            configs = yaml.load(f)
        except yaml.YAMLError as e:
            print(e)
            exit(-1)

    board_config = configs["board"]
    states_ui = board_config["states"]
    board_ui = board_config["board_drawing"]
    board_matrix = board_config["board_matrix"]
    print("Here is all the crap: ", str(board_config))
    print("Here are the states: ", str(states_ui))
    print("Here is the ui: ", str(board_ui))
    print("Here is the adjacency matrix: ", str(board_matrix))
    exit(0)

def game_over():
    """
    Returns if the game is over or not.
    """
    # TODO
    pass
