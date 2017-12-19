import copy

# TODO: The most important thing you must test is all the things that the dipjudge algorithm says are implementation dependent.
#       The dataset must work with this algorithm, so that is the implementation to shoot for.

convoying_armies = []
all_combat_lists = {}
convoy_succeeded_list = []

def adjudicate(gamestate):
    """
    Takes a game state and returns the new game state (with old one untouched).
    If units in the given game state that do not have orders are given hold orders. Illegal moves are marked as hold orders
    as well.
    """
    nextstate = copy.deepcopy(gamestate)
    _mark_all_invalid_convoy_orders(nextstate)
    _mark_all_invalid_move_and_support_orders(nextstate)
    _calculate_initial_combat_strengths(nextstate)
    convoy_succeeded_list_grew = True
    while convoy_succeeded_list_grew:
        _mark_support_cuts_made_by_convoyers_and_mark_endangered_convoys(nextstate)
        convoy_succeeded_list_grew = _mark_convoy_disruptions_and_support_cuts_made_by_successful_convoys(nextstate)
    bounced = True
    while bounced:
        bounced = _mark_bounces_caused_by_inability_to_swap_places(nextstate)
        if bounced:
            continue
        bounced = _mark_bounces_suffered_by_understrength_attackers(nextstate)
        if bounced:
            continue
        bounced = _mark_bounces_caused_by_inability_to_self_dislodge(nextstate)
        if bounced:
            continue
        _mark_supports_cut_by_dislodgements(nextstate)
    _move_units_that_did_not_bounce(nextstate)
    reset_global_variables()
    return nextstate

def _mark_all_invalid_convoy_orders(s):
    def fleets_did_not_order_matching_convoys(army):
        for fleet in army.order.fleets:
            if s.fleet_did_not_issue_matching_convoy(fleet, army.order):
                return True
        return False

    for army in s.convoying_armies():
        if s.is_not_legal_convoy_by_army(army.order):
            army.order.mark = "void"
        elif fleets_did_not_order_matching_convoys(army):
            army.order.mark = "no convoy"
        else:
            global convoying_armies
            convoying_armies.append(army)

    for fleet in s.convoying_fleets():
        if s.is_not_legal_convoy_by_fleet(fleet.order):
            fleet.order.mark = "void"
        elif s.army_did_not_issue_matching_convoy_order(fleet.order):
            fleet.order.mark = "void"

def _mark_all_invalid_move_and_support_orders(s):
    for unit in s.units_issuing_move_orders():
        if s.move_order_is_illegal_for_unit(unit.order):
            unit.order.mark = "void"
    for unit in s.units_issuing_support_orders():
        if s.support_order_is_illegal_for_unit(unit.order):
            unit.order.mark = "void"
        elif s.supported_unit_did_not_issue_matching_order(unit.order):
            unit.order.mark = "void"
        else:
            unit.support_count += 1
            if s.supported_unit_attacking_supporting_units_team(unit):
                # Where y = unit that is supported by x, (so unit = x in this function)
                # If y attacks owner of x, add x to y's no help list.
                unit.order.supported_unit.add_to_no_help(unit)

def _calculate_initial_combat_strengths(s):
    global all_combat_lists
    for unit in s.units_issuing_non_convoyed_move_orders():
        _cut_support(unit, s)
    for space in s.all_spaces():
        all_combat_lists[space.province_name] = [u for u in s.units_trying_to_move_to_or_remain_in(space)]

def _mark_support_cuts_made_by_convoyers_and_mark_endangered_convoys(s):
    global convoy_succeeded_list
    convoy_succeeded_list_grew = True
    while convoy_succeeded_list_grew:
        convoy_succeeded_list_grew = False
        for army in convoying_armies:
            _check_disruptions(army, s.fleet_convoying_army(army), s)
            if not army.order.mark:
                _cut_support(army, s)
                convoy_succeeded_list.append(army)
                convoy_succeeded_list_grew = True
            else if army.order.mark == "convoy endangered":
                army.order.mark = "convoy under attack"

def _mark_convoy_disruptions_and_support_cuts_made_by_successful_convoys(s):
    grew = False
    for army in convoying_armies:
        _check_disruptions(army, s.fleet_convoying_army, s)
        if army.order.mark == "convoy endangered":
            army.order.mark = "no convoy"
            army.support_count = 0
            for supporter in s.units_supporting_unit(army):
                supporter.order.mark = "no convoy"
        elif army.order.mark == "convoy under attack":
            army.order.mark = ""
            _cut_support(army, s)
            convoy_succeeded_list.append(army)
            grew = True
    return grew

def _mark_bounces_caused_by_inability_to_swap_places(s):
    bounced = False
    for unit in s.all_moving_units_not_convoying_whose_orders_are_not_marked():
        dest_unit = unit.order.destination.unit
        if dest_unit and dest_unit.order.mark == "" and dest_unit.order.destination == unit.location and not s.unit_is_being_convoyed(dest_unit):
            swapper = dest_unit
            if swapper.owner == unit.owner or unit.support_count - len(unit.no_help) <= swapper.support_count:
                _bounce(unit, s)
                bounced = True
            if swapper.owner == unit.owner or swapper.support_count - len(swapper.no_help) <= unit.support_count:
                _bounce(swapper, s)
                bounced = True
    return bounced

def _mark_bounces_suffered_by_understrength_attackers(s):
    bounced = False
    for space in s.all_spaces:
        for unit in all_combat_lists[space]:
            if s.unit_does_not_have_greater_support_count_than_every_other_unit_in(unit, space) and unit.order.order_type == "move" and unit.order.mark == "":
                _bounce(unit, s)
                bounced = True
    return bounced

def _mark_bounces_caused_by_inability_to_self_dislodge(s):
    bounced = False
    # TODO

def _mark_supports_cut_by_dislodgements(s):
    # TODO: Note: this is step nine, so _cut_support must be called with step_nine=True
    pass

def _move_units_that_did_not_bounce(s):
    pass

def reset_global_variables():
    # TODO
    ...

def _cut_support(unit, s, step_nine=False):
    """
    Takes a unit that is moving and the game state and checks if it is moving to a location
    that has a unit that is supporting. If the unit that is supporting fits certain criteria, it stop supporting.
    """
    def _units_support_is_cut(supporter, s):
        if supporter.order.mark == "cut" or supporter.order.mark == "void":
            return False
        if supporter.owner == unit.owner:
            return False
        if unit.is_being_convoyed:
            if s.unit_is_offering_support_for_or_against_convoying_fleets(supporter):
                return False
            if not (step_nine or not s.supporting_unit_is_offering_support_into(unit.location, supporter):
                return False
        return True

    occupier = s.unit_at_location(unit.order.destination)
    if occupier and occupier.order.is_support() and _supporter_fits_criteria_for_cut(occupier, s):
        occupier.order.mark = "cut"
        occupier.order.supported_unit.supports -= 1
        occupier.order.supported_unit.remove_from_no_help(occupier)

def _check_disruptions(convoying_army, fleet, s):
    """
    Takes a convoying army
    """
    space = fleet.space
    combat_list = all_combat_lists[space]
    unit_has_higher_support_than_all_others = True # TODO
    if len(combat_list) == 1 and unit_has_higher_support_than_all_others and single_unit.owner is not fleet.owner:
        convoying_army.order.mark = "convoy endangered"

