import unittest
from io import BytesIO
from statemachine import StateMachine, new_statemachine, StreamReader, StreamWriter

class Test(unittest.TestCase):
    def test_statemachine(self):
        sm = new_statemachine('statemachine.json')
        self.assertEqual(sm.state, 'start')
        sm.accept_value('begin')
        self.assertEqual(sm.version, 1)
        self.assertEqual(sm.state, 'middle')
        sm.accept_value('process')
        sm.accept_value('process')
        sm.accept_value('process')
        sm.accept_value('process')
        sm.accept_value('process')
        self.assertEqual(sm.version, 6)
        self.assertEqual(sm.state, 'middle')
        sm.accept_value('finish')
        self.assertEqual(sm.version, 7)
        self.assertEqual(sm.state, 'end')

    def test_mod3(self):
        sm = new_statemachine('mod3.json')
        sm.accept_value('1')
        sm.accept_value('1')
        sm.accept_value('1')
        sm.accept_value('$')
        self.assertEqual(sm.state, 'end')

    def test_binlog(self):
        buf = BytesIO()
        sm = new_statemachine('mod3.json', StreamWriter(buf))
        sm.accept_value('1')
        sm.accept_value('2')
        sm.accept_value('1')
        buf.seek(0)
        log_reader = StreamReader(buf)
        self.assertEqual(log_reader.read().value, '1')
        self.assertEqual(log_reader.read().value, '2')
        self.assertEqual(log_reader.read().value, '1')
        self.assertEqual(log_reader.read(), None)

if __name__ == '__main__':
    unittest.main()
