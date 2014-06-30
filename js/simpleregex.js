// author : Jeeyoung Kim
// 2014-06-29
// simple regex (a-z, *, .) matcher. without parenthesis


function match(pattern, seq) {
  return _match(_sanitize(pattern), seq, 0, 0);
}

function _sanitize(pattern) {
  var character = null;
  var result = "";
  for (var i = 0; i < pattern.length; i++) {
    var cur = pattern[i];
    if (character == null && cur == '*') continue;

    if (cur == '*') {
      result += character + '*';
      character = null;
    } else {
      if (character !== null) result += character;
      character = cur;
    }
  }
  if (character !== null) result += character;
  return result;
}

function is_char(x) { return x >= 'a' && x <= 'z'; }

function _match(pattern, seq, idx_pattern, idx_seq) {
  // ending condition. when the pattern is all consumed, sequence should be consumed also.
  if (idx_pattern >= pattern.length) { return (idx_seq == seq.length); }
  var has_star = idx_pattern + 1 < pattern.length && pattern[idx_pattern + 1] == '*';

  // consume klein star.
  if (has_star && _match(pattern, seq, idx_pattern + 2, idx_seq)) return true;

  // consume sequence. advance pattern if there's no klein star.
  if ((is_char(pattern[idx_pattern]) && pattern[idx_pattern] === seq[idx_seq]) || pattern[idx_pattern] == '.') {
    return _match(pattern, seq, idx_pattern + (has_star ? 0 : 1), idx_seq + 1)
  }
  return false;
}

console.log(_sanitize('a') === 'a')
console.log(_sanitize('a*') === 'a*')
console.log(_sanitize('a**') === 'a*')
console.log(_sanitize('*') === '')
console.log(_sanitize('*aa**bb**cc') === 'aa*bb*cc')

// true matches
console.log(match("a","a"))
console.log(match("a*","aaa"))
console.log(match("a*b*","aaaabbbb"))
console.log(match("a*b*..","aaaabbbb"))
console.log(match("*", ""))
// false matches
console.log(!match("a*b*c","aaaabbbb"))
console.log(!match("a*b*c","aaaabbbb"))
