import json

class StateMachine(object):
    __slots__ = (
        'start_state',
        'end_state',
        'error_state',
        'states',
        'transitions',
        'state',
    )

    def __init__(self, start_state, end_state, error_state, states, transitions):
        self.start_state = start_state
        self.end_state = end_state
        self.error_state = error_state
        self.states = set(states)
        assert start_state in self.states
        assert end_state in self.states
        assert error_state in self.states
        self.transitions = {}
        for t in transitions:
            if t.from_state not in self.transitions:
                self.transitions[t.from_state] = []
            trs = self.transitions[t.from_state]
            trs.append(t)

        self.state = self.start_state

    def accept_value(self, input_value):
        trs = self.transitions[self.state]
        for tr in trs:
            if input_value in tr.values:
                self.state = tr.to_state
                return
        self.state = self.error_state

    def __repr__(self):
        return "StateMachine[state=%s]" % self.state

class Transition(object):
    __slots__ = ('from_state', 'to_state', 'values')
    def __init__(self, from_state, to_state, values):
        self.from_state = from_state
        self.to_state = to_state
        self.values = values

    def __repr__(self):
        return "%s%s" % (type(self).__name__, (self.from_state, self.to_state, self.values))

def transition(json_object):
    return Transition(
        json_object['from'],
        json_object['to'],
        json_object['values'],
    )

def main():
    input_object = json.load(open('statemachine.json', 'rb'))
    transitions = [transition(x) for x in input_object['transitions']]
    sm = StateMachine(
        input_object['start'],
        input_object['end'],
        input_object['error'],
        states = input_object['states'],
        transitions=transitions
    )
    assert sm.state == 'start'
    sm.accept_value('xyz')
    assert sm.state == 'end'

if __name__ == '__main__':
    main()
