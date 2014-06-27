/**
 * Permute the given input array, calling callback for each of the
 * permutation.
 */
function permute(input, callback) {
  var contains = []; // boolean flags.
  var permutation = [];
  var N = input.length;
  for (var i = 0; i < N; i++) {
    contains.push(false);
  }
  // internal implementation detail.
  function _permute() {
    if (permutation.length == N) {
      callback(permutation)
    } else {
      for (var i = 0; i < N; i++) {
        if (!contains[i]) {
          contains[i] = true;
          permutation.push(input[i]);
          _permute();
          permutation.pop();
          contains[i] = false;
        }
      }
    }
  }
  return _permute();
}

// utility functions to test.
function make(count) {
  var result = [];
  for (var i = 0; i < count; i++) { result.push(i); }
  return result;
}

function countperm(seq) {
  var count = 0;
  permute(seq, function(x) { count++ })
  console.log("Sequence", seq, "has", count, "permutations.");
}

permute(make(3), function(x) { console.log(x) })
countperm(make(5));
countperm(make(10));
/*
[ 0, 1, 2 ]
[ 0, 2, 1 ]
[ 1, 0, 2 ]
[ 1, 2, 0 ]
[ 2, 0, 1 ]
[ 2, 1, 0 ]
Sequence [ 0, 1, 2, 3, 4 ] has 120 permutations.
Sequence [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ] has 3628800 permutations.
*/
