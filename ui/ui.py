"""
This is the UI for the game.
"""
import os

def initialize():
    """
    Does any initialization necessary.
    """
    print("Welcome to PyBoat.")


def get_valid_moves_from_user(model):
    orders_are_valid, orders = _get_valid_moves(model)
    while not orders_are_valid:
        print("Failed to understand order: ", str(orders))
        orders_are_valid, orders = _get_valid_moves(model)
    return orders


def print_help():
    """
    Prints the help string to the console.
    """
    help_str = "There are four things you can do: " +\
            os.linesep + "Move: '(A trieste -> budapest)'" +\
            os.linesep + "Hold: '(A bohemia hold)'" +\
            os.linesep + "Support a move: '(A vienna sup A trieste -> budapest)'" +\
            os.linesep + "Support a hold: '(A tyrolia sup A bohemia hold)'"
    print(help_str)
    print("Please enter your orders in parentheses and separated by semicolons ';'")
    print("You can also type 'map' or 'm' to display the map again.")


def show_board(model):
    """
    Shows the board as it currently is.
    """
    board_string = model.get_formatted_display()
    print(board_string)


def _get_valid_moves(model):
    """
    Helper function for get_valid_moves_from_user
    """
    orders = input("Please enter your orders: ")
    if orders == "help" or orders == 'h':
        print_help()
        return _get_valid_moves(model)
    elif orders == "map" or orders == 'm':
        show_board(model)
        return _get_valid_moves(model)
    else:
        return model.orders_are_valid(orders)









