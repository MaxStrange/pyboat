"""
This is the control in the MVC scheme.
"""

import ai.ai as ai
import logic.config as config

def run_game(model, view):
    """
    This is the actual game logic.
    """
    while not model.game_over():
        if config.USING_AI:
            # TODO: tell the AI thread to start doing its thing
            pass

        view.show_board(model)
        moves = view.get_valid_moves_from_user(model)

        if config.USING_AI:
            while not ai.ready():
                # Hang out until the AI thread is done with this batch
                # of orders
                pass
            moves += ai.get_ai_orders()

        _apply_moves(model, moves)





def _apply_moves(model, moves):
    """
    Applies the set of moves to the game state to
    derive the new state.
    """
    # TODO
    pass







