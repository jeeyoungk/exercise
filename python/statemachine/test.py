import unittest
from statemachine import StateMachine, new_statemachine

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
if __name__ == '__main__':
    unittest.main()
