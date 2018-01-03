Okay new plan:

1. Examine each unit individually (though include convoys) and rank each unit's possible moves for one turn and two turns ahead based on how much damage it could do
1. Multiply by likelihood of happening and normalize
1. ??


```python
def evaluate_single_unit_moves():
    """
    Creates a list of each unit's possible moves and assigns them a utility score.
    :returns: A dict of MoveCombos to values
    """
    values = {}
    for loc in locations:
        units_for_this_loc = [u for u in units if unit_can_move_to(loc) or u.loc == loc]
        for u in units_for_this_loc:
            if u.loc != loc:
                mc_move = MoveCombo(u, MOVE, units_that_can_move_to(loc))  # A MOVE order that can be supported by up to n units
            else:
                mc_hold = MoveCombo(u, HOLD, units_that_can_move_to(loc))  # A HOLD order that can be supported by up to n units
            values.add(evaluate_move(mc_move))
            values.add(evaluate_move(mc_hold))
    return values
```
