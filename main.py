"""
The main entry point to the gunboat game.
"""

import logic.state as model
import ui.ui as view
import logic.logic as control
import sys

if len(sys.argv) != 2:
    print("USAGE: " + str(sys.argv[0]) + " path/to/yaml")
    exit(1)
else:
    config_file = sys.argv[1]
    model.initialize(config_file)
    view.initialize()
    control.run_game(model, view)
