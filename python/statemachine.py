import json

class StateMachine(object):
    __slots__ = (
        'start_state',
        'end_state',
        'error_state',
        'states',
        'transitions',
        # states
        'state',
        'version',
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
            self.transitions[t.from_state].append(t)

        self.state = self.start_state
        self.version = 0

    def accept_value(self, input_value):
        trs = self.transitions[self.state]
        for tr in trs:
            if input_value in tr.values:
                self.state = tr.to_state
                self.version += 1
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

    @classmethod
    def from_json(cls, json_object):
        return cls(
            json_object['from'],
            json_object['to'],
            json_object['values'],
        )

class Log(object):
    __slots__ = ('from_state', 'to_state', 'value', 'version')

def main():
    input_object = json.load(open('statemachine.json', 'rb'))
    transitions = [Transition.from_json(x) for x in input_object['transitions']]
    sm = StateMachine(
        input_object['start'],
        input_object['end'],
        input_object['error'],
        states = input_object['states'],
        transitions = transitions
    )
    assert sm.state == 'start'
    sm.accept_value('begin')
    assert sm.version == 1
    assert sm.state == 'middle'
    sm.accept_value('process')
    sm.accept_value('process')
    sm.accept_value('process')
    sm.accept_value('process')
    sm.accept_value('process')
    assert sm.version == 6
    assert sm.state == 'middle'
    sm.accept_value('finish')
    assert sm.version == 7
    assert sm.state == 'end'

if __name__ == '__main__':
    main()
