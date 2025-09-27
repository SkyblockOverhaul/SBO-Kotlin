package net.sbo.mod.utils.math

import kotlin.math.abs

class Matrix(val data: List<List<Double>>) {
    val rows: Int = data.size
    val cols: Int = if (data.isNotEmpty()) data[0].size else 0

    fun transpose(): Matrix {
        val result = MutableList(cols) { MutableList(rows) { 0.0 } }
        for (j in 0 until cols) {
            for (i in 0 until rows) {
                result[j][i] = data[i][j]
            }
        }
        return Matrix(result)
    }

    fun multiply(other: Matrix): Matrix {
        if (cols != other.rows) {
            throw IllegalArgumentException("Matrix dimensions do not match for multiplication")
        }
        val result = MutableList(rows) { MutableList(other.cols) { 0.0 } }
        for (i in 0 until rows) {
            for (j in 0 until other.cols) {
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += data[i][k] * other.data[k][j]
                }
                result[i][j] = sum
            }
        }
        return Matrix(result)
    }

    fun inverse(): Matrix {
        if (rows != cols) {
            throw IllegalArgumentException("Only square matrices can be inverted")
        }
        val n = rows
        val augmented = Array(n) { DoubleArray(2 * n) }

        for (i in 0 until n) {
            for (j in 0 until n) {
                augmented[i][j] = data[i][j]
            }
            for (j in 0 until n) {
                augmented[i][j + n] = if (i == j) 1.0 else 0.0
            }
        }

        for (i in 0 until n) {
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(augmented[k][i]) > abs(augmented[maxRow][i])) {
                    maxRow = k
                }
            }

            val temp = augmented[i]
            augmented[i] = augmented[maxRow]
            augmented[maxRow] = temp

            if (abs(augmented[i][i]) < 1e-12) {
                throw IllegalArgumentException("Matrix is singular and cannot be inverted")
            }

            val pivot = augmented[i][i]
            for (j in 0 until 2 * n) {
                augmented[i][j] /= pivot
            }

            for (k in 0 until n) {
                if (k != i) {
                    val factor = augmented[k][i]
                    for (j in 0 until 2 * n) {
                        augmented[k][j] -= factor * augmented[i][j]
                    }
                }
            }
        }

        val inv = MutableList(n) { MutableList(n) { 0.0 } }
        for (i in 0 until n) {
            for (j in 0 until n) {
                inv[i][j] = augmented[i][j + n]
            }
        }
        return Matrix(inv)
    }
}