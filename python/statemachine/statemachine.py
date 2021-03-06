#!/usr/bin/env python3

import json
import argparse
import struct

class StateMachine(object):
    """
    State machine, where:
    - its definitions are powered by JSON.
    - states are replicated & streamed.
    - logs are maintained (and truncated regularly).
    """
    __slots__ = (
        'start_state',
        'end_state',
        'error_state',
        'states',
        'transitions',
        # states
        'state',
        'version',
        'log_writer',
    )

    def __init__(self, start_state, end_state, error_state, states, transitions,
            log_writer):
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
        self.log_writer = log_writer

    def accept_value(self, input_value):
        '''
        perform a state transition.
        '''
        trs = self.transitions[self.state]
        previous_state = self.state
        if self.state == self.error_state or self.state == self.end_state:
            return False # final state - no more input accepted.
        for tr in trs:
            if input_value in tr.values:
                self.state = tr.to_state
                self.version += 1
                self.log_writer.write(Log(
                    from_state = previous_state,
                    to_state = self.state,
                    value = input_value,
                    version = self.version
                ))
                return True
        self.state = self.error_state
        self.version += 1
        self.log_writer.write(Log(
            from_state = previous_state,
            to_state = self.error_state,
            value = input_value,
            version = self.version
        ))
        return True

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
    """
    State transition log generated by this.
    """
    __slots__ = ('from_state', 'to_state', 'value', 'version')
    def __init__(self, from_state, to_state, value, version):
        self.from_state = from_state
        self.to_state = to_state
        self.value = value
        self.version = version

    def __str__(self):
        return "%d) %s -(%s)-> %s" % (self.version, self.from_state, self.value, self.to_state)

class NullLogWriter(object):
    def write(self, log):
        pass

    def close(self):
        pass

class StreamWriter(object):
    """
    because binary formats are fun.
    """
    def __init__(self, outfile):
        self.outfile = outfile

    def write(self, log):
        self._write_long(log.version)
        self._write_string(log.from_state)
        self._write_string(log.to_state)
        self._write_string(log.value)

    def close(self):
        self.outfile.close()

    def _write_long(self, value):
        self.outfile.write(struct.pack("l", value))

    def _write_string(self, string):
        encoded = string.encode('utf-8')
        self._write_long(len(encoded))
        self.outfile.write(encoded)

class StreamReader(object):
    def __init__(self, infile):
        self.infile = infile

    def read(self):
        try:
            version = self.read_long()
            from_state = self.read_string()
            to_state = self.read_string()
            value = self.read_string()
            return Log(from_state, to_state, value, version)
        except IOError:
            return None

    def read_bytes(self, num_bytes):
        raw = self.infile.read(num_bytes)
        if len(raw) != num_bytes:
            raise IOError("premature end of file")
        return raw

    def read_long(self):
        raw_bytes = self.read_bytes(8)
        return struct.unpack("l", raw_bytes)[0]

    def read_string(self):
        length = self.read_long()
        raw_bytes = self.read_bytes(length)
        return raw_bytes.decode('utf-8')

def new_statemachine(filepath, log_writer=None):
    if log_writer is None:
        log_writer = NullLogWriter()
    with open(filepath, 'r', encoding='utf-8') as input_definition:
        input_object = json.load(input_definition)
    transitions = [Transition.from_json(x) for x in input_object['transitions']]
    sm = StateMachine(
        input_object['start'],
        input_object['end'],
        input_object['error'],
        states = input_object['states'],
        transitions = transitions,
        log_writer = log_writer
    )
    return sm

def main():
    p = argparse.ArgumentParser()
    p.add_argument(
            '--definition',
            required=True,
            metavar='definition',
            help='state machine definition JSON file.'
    )
    p.add_argument(
            '--log',
            default='statemachine.binlog',
            help='binary log file.'
    )
    p.add_argument(
            '--mode',
            required=True,
            metavar='mode',
            help='execution mode'
    )
    args = p.parse_args()
    if args.mode == 'server':
        log_writer = StreamWriter(open(args.log, 'wb'))
        sm = new_statemachine(args.definition, log_writer)
        while sm.state != sm.end_state and sm.state != sm.error_state:
            print(sm)
            try:
                v = input()
            except EOFError:
                break
            sm.accept_value(v)
        print(sm)
        log_writer.close()
    elif args.mode == 'reader':
        log_reader = StreamReader(open('statemachine.binlog', 'rb'))
        sm = new_statemachine(args.definition)
        print(sm)
        log = log_reader.read()
        while log is not None:
            sm.accept_value(log.value)
            print(log)
            print(sm)
            log = log_reader.read()

if __name__ == '__main__':
    main()
