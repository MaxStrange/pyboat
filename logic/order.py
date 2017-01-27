"""
This module holds the Order class.
"""

class ParseError(Exception):
    """
    An Error used to indicated that the command line orders given
    cannot be parsed into a sensible order.
    """
    pass



class Order:
    """
    An Order class to represent an order that can be made
    for a unit.
    """
    def __init__(self, order):
        """
        A correct order is of the form:
        "(A trieste -> budapest)"
        "(A vienna sup A trieste -> budapest)"
        "(A bohemia hold)"
        "(A Tyrolia sup A bohemia hold)"
        If it cannot be made to match one of these, then a ParseError
        is raised.
        """
        order = order.strip()
        order = order.strip('()')
        order = order.strip()
        if order[0] != 'A':
            raise ParseError
        order = order.split(' ')
        if len(order) == 4:
            # A trieste -> budapest
            self.order_type = "move"
            self.src = order[1]
            self.dest = order[3]
        elif len(order) == 7:
            # A vienna sup A trieste -> budapest
            self.order_type = "sup_move"
            self.src = order[1]
            self.dest = order[1]
            self.sup_src = order[4]
            self.sup_dest = order[6]
        elif len(order) == 3:
            # A bohemia hold
            self.order_type = "hold"
            self.src = order[1]
            self.dest = order[1]
        elif len(order) == 6:
            # A tyrolia sup A bohemia hold
            self.order_type = "sup_hold"
            self.src = order[1]
            self.dest = order[1]
            self.sup_src = order[4]
            self.sup_dest = order[4]
        else:
            raise ParseError

    def __repr__(self):
        s = "Order: "
        for k, v in self.__dict__.items():
            s += str(k) + ": " + str(v)
        return s

    def __str__(self):
        as_str = "(" + self.order_type + ": "
        if self.order_type == "move":
            as_str += self.src + " -> " + self.dest + ")"
        elif self.order_type == "sup_move":
            as_str += self.src + " sup "
            as_str += self.sup_src + " -> " + self.sup_dest + ")"
        elif self.order_type == "hold":
            as_str += self.src + " hold " + ")"
        elif sel.order_type == "sup_hold":
            as_str += self.src + " sup "
            as_str += self.sup_src + " hold " + ")"
        return as_str









