"""
The main entry point to the gunboat game.
"""

import logic.state as model
import ui.ui as view
import logic.logic as control

model.initialize()
view.initialize()
control.run_game(model, view)
