"""
Module for holding the Node class.
"""

import copy
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
        self.total_reward = 0
        self.num_times_visited = 0
        self.already_tried_action_sets = []

        # Theoretical:
        self.action_sets_given_so_far = set()
        self.softmaxes = [(unit.id, softmax(unit, state) for unit in state.units()]
        # This would leave you with:
        # [(A0, (0.8, 0.2)), (A1, (0.87, 0.13)), ...]
        # Which describes the softmax function's belief in each unit's odds of
        # (participating in a move order, participating in a hold order)
        # This softmax function would be a neural network that has been trained
        # to predict a single unit's moves from a game board, trained on actual
        # players' choices (supervised learning)

    def __str__(self):
        s = "Node: "
        for key, val in self.__dict__.items():
            s += os.linesep + "    " + key + ": " + str(val)
        return s

    def available_actions(self):
        """
        Returns the set of available moves that could be applied to
        this Node's state.
        """
        return self.state.possible_moves()

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
        child_node.parent = self
        child_node.name = self.name + "_" + str(child_node.move_that_derived_this_node())
        self.children.append(child_node)
        self.already_tried_action_sets.append(action)
        return child_node

    def move_that_derived_this_node(self):
        """
        Returns the action that created this Node's game state.
        """
        return self.state._move_that_derived_this_state

    def pull_from_most_likely_action_sets(self):
        """
        Returns True and a low-res action set if it successfully randomly
        generates an action set that this node hasn't tried before.
        If it fails to do so, it returns False, None.
        It generates these sets by choosing an order for each unit independently
        of each other unit, using that unit's softmax distribution.
        """
        action_set = []
        for unit in self.softmaxes:
            r = random.uniform(0, 1)
            # unit looks like this: (id, (prob, prob))
            if r <= unit[1][0]:
                choice = lowres_move
            else:
                choice = lowres_hold
            action_set.append(choice)

        if action_set in self.already_tried_action_sets:
            return False, None
        else:
            self.already_tried_action_sets.append(action_set)
            return True, action_set

    def is_non_terminal(self):
        """
        A Node is terminal if its state is at game over.
        So this returns True as long as that is not the case.
        """
        return not self.state.game_over()

    def is_not_fully_expanded(self):
        """
        Returns True unless all possible children have been added to this
        Node's children list.
        """
        return len(self.available_actions()) != len(self.children)







