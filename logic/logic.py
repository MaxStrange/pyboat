"""
This is the control in the MVC scheme.
"""

import ai.ai as ai

def run_game(model, view):
    """
    This is the actual game logic.
    """
    while not model.game_over():
        view.show_board(model)
        moves = view.get_valid_moves_from_user(model)
        if config.USING_AI:
            moves += ai.get_best_move(model.current_state())
        _apply_moves(model, moves)





def _apply_moves(model, moves):
    """
    Applies the set of moves to the game state to
    derive the new state.
    """
    # TODO
    pass
