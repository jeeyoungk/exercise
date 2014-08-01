package main

// implementation of the gaussian elimination.

func normalize_row(matrix [][]float64, row int, col int) {
	factor := matrix[row][col]
	for idx := 0; idx < len(matrix[row]); idx++ {
		matrix[row][idx] = matrix[row][idx] / factor
	}
}

func subtract_row(matrix [][]float64, fromRow int, toRow int, factor float64) {
	for idx := 0; idx < len(matrix[fromRow]); idx++ {
		matrix[fromRow][idx] -= matrix[toRow][idx] * factor
	}
}

func swap_row(matrix [][]float64, rowA int, rowB int) {
	tmp := matrix[rowA]
	matrix[rowA] = matrix[rowB]
	matrix[rowB] = tmp
}

func Gaussian(matrix [][]float64, nrow int, ncol int) [][]float64 {
	// TODO - implement shuffling rows.
	for row := 0; row < nrow; row++ {
		normalized := false
		if matrix[row][row] != 0 {
			normalize_row(matrix, row, row)
			normalized = true
		} else {
			for newrow := row + 1; newrow < nrow && !normalized; newrow++ {
				if matrix[newrow][row] != 0 {
					swap_row(matrix, newrow, row)
					normalize_row(matrix, row, row)
					normalized = true
				}
			}
		}

		if !normalized {
			continue
		}
		for newrow := 0; newrow < nrow; newrow++ {
			if newrow == row {
				continue
			}
			factor := matrix[newrow][row]
			subtract_row(matrix, newrow, row, factor)
		}
	}
	return nil
}
