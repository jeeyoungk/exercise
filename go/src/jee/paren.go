package main

// Balanced parenthesis.
func is_balanced(input string) bool {
	var count = 0
	for _, char := range input {
		if char == '(' {
			count += 1
		} else if char == ')' {
			count -= 1
		}

		if count < 0 {
			return false
		}
	}
	return count == 0
}
