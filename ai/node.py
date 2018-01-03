"""
Module for holding the Node class.
"""

import copy
#from mynetworks import unit_softmax as softmax
import os

class Node:
    """
    This class is essentially just a GameState, but with some
    helper functions and additional data (such as parent and children).
    """
    def __init__(self, state):
        self.name = None
        self.state = state
        self.parent = None
        self.children = []

        # Theoretical:
        self.num_times_children_seen = {} # dict from child to num_times generated
        self.softmaxes = [(unit.id, softmax(unit, state) for unit in state.units()]
        # This would leave you with:
        # [(A0, (0.8, 0.2)), (A1, (0.87, 0.13)), ...] <-- Currently these numbers are much closer to a 50/50 split
        # Which describes the softmax function's belief in each unit's odds of
        # (participating in a move order, participating in a hold order)
        # This softmax function would be a neural network that has been trained
        # to predict a single unit's moves from a game board, trained on actual
        # players' choices (supervised learning)
        # Currently, I am testing out a CNN architecture, but it doesn't quite yet work
        # We are hoping for just slightly better than chance (as that is probably the best you can do)

    def __str__(self):
        s = "Node: "
        for key, val in self.__dict__.items():
            s += os.linesep + "    " + key + ": " + str(val)
        return s

    def derive_child(self, action_set):
        """
        Derives a new Node from this one and the given action.
        Does not care if action_set is lowres or highres.
        """
        child_state = copy.deepcopy(self.state)
        child_state.take_turn(action_set) # decides what to do based on lowres vs highres
        # When take_turn uses a lowres action_set, it will need to run the state through
        # another neural network. This network takes a game board with unit labels
        # in low res space and predicts the next game board. Again, this will be
        # supervised and take a board as its input, outputting a new board.
        child_node = Node(child_state)
        # The child state should keep the orders that were used to derive
        # it. So each unit should have an order associated with it which
        # represents the order that it used last turn to get where it is now.
        child_node.parent = self
        child_node.name = self.name + "_" + str(child_node.move_that_derived_this_node())
        self.children.append(child_node)
        return child_node

    def generate_child(self):
        """
        Generates a new child from this node and increments
        the count of that child.
        Does not actually return the child.
        """
        order_set = self.pull_from_most_likely_action_sets()
        child_node = self.derive_child(action_set)
        try:
            self.num_times_children_seen[child_node] += 1
        except KeyError:
            self.num_times_children_seen[child_node] = 0

    def most_likely_children(num_to_pick):
        """
        Returns the num_to_pick most 'likely' children.
        Specifically, it returns the children that were generated
        the largest number of times.
        """
        children = [(times, c) for c, times in self.num_times_children_seen.items()]
        sorted_children = sorted(children)
        sorted_children = [tup[1] for tup in sorted_children]
        assert(num_to_pick <= len(sorted_children))
        return [sorted_children[0:num_to_pick]

    def move_that_derived_this_node(self):
        """
        Returns the action that created this Node's game state.
        """
        return self.state._move_that_derived_this_state

    def pull_from_most_likely_action_sets(self):
        """
        Returns a low-res action set.
        It generates these sets by choosing an order for each unit independently
        of each other unit using that unit's softmax distribution.
        """
        action_set = []
        for unit in self.softmaxes:
            r = random.uniform(0, 1)
            # unit looks like this: (id, (probMove, probHold))
            if r <= unit[1][0]:
                choice = lowres_move
            else:
                choice = lowres_hold
            action_set.append(choice)
        return action_set

    def is_non_terminal(self):
        """
        A Node is terminal if its state is at game over.
        So this returns True as long as that is not the case.
        """
        return not self.state.game_over()






