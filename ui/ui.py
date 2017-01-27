"""
This is the UI for the game.
"""

def initialize():
    """
    Does any initialization necessary.
    """
    print("Welcome to PyBoat.")


def get_valid_moves_from_user(model):
    orders = input("Please enter your orders: ")
    while not model.orders_are_valid(orders):
        print("Those orders are invalid.")
        orders = input("Please enter your orders: ")
    return orders


def show_board(model):
    """
    Shows the board as it currently is.
    """
    board_string = model.get_formatted_display()
    print(board_string)
