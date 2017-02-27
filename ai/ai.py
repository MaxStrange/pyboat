"""
This is the AI module's main API.
It contains only one API function: get_best_move,
which, usurprisingly, gets what the AI believes is its best set of
orders.
"""

from ai.node import Node
import copy
import math
import random
from time import process_time


def get_best_move(cur_state):
    """
    Gets what the AI believes is its best set of orders.
    """
    root = Node(game_state)
    root.name = "root"

    start_time = process_time()
    while _within_computational_budget(start_time):
        root.generate_child()
    most_likely_children = root.most_likely_children(10)

    # You now have the 10 most likely next game states.
    # We would like to figure out what orders people wrote
    # in order to bring them to this state, which would
    # ideally just be a matter of running an inverse
    # abjudicator, but the abjudicator function is not
    # a bijective function, and as such has no inverse.
    # So we will need some way of approximating the inverse.
    # LUCKILY, we also happen to have low resolution orders tagged to each unit
    # to help us determine how the units got here (that is, each unit in the
    # child has a tag that says either that it participated in some fashion in
    # a HOLD order to get here or that it participated in a MOVE order in some
    # way to get here).
    most_likely_order_sets = [_inverse_abjudicate(\
            child.state, child.parent.state) for child\
            in most_likely_children]

    # Now we have the 10 most likely sets of orders that will be written
    # down during this planning phase.
    # We therefore have near-perfect information, and can plan
    # according to what we believe other people are most likely to do
    optimal_orders = _choose_best_orders(cur_state, most_likely_order_sets)

    return optimal_orders


def _choose_best_orders(cur_state, most_likely_order_sets):
    """
    Chooses the optimal set of orders to give the AI's units
    based on a list of most likely orders that will be given
    to all the other units on the game board.
    This function essentially returns what the AI thinks it
    should do, given that it now already has a few guesses as to what
    each of the other players is going to do with their units.
    """
    pass # TODO

def _inverse_abjudicate(derived_state, original_state):
    """
    Takes a state and the state from which it was derived
    and guesses as to what the orders were that created it.
    The derived state has tags that tell us whether each unit participated in a
    HOLD or in a MOVE. So this can just be a neural network that takes two game
    states A and B, where A is a low-res game state, and which outputs the
    high-res game state A*, given that B was the result of applying the orders
    in A*.
    """
    pass # TODO


def _within_computational_budget(start):
    """
    Returns True if time still hasn't run out for the computer's turn.
    """
    elapsed_time = process_time() - start
    return elapsed_time < 1.5

