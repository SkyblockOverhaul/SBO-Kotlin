package net.sbo.mod.utils.math

import kotlin.math.pow

class PolynomialFitter(private val degree: Int) {
    private val xPointMatrix = mutableListOf<List<Double>>()
    private val yPoints = mutableListOf<List<Double>>()

    fun addPoint(x: Double, y: Double) {
        yPoints.add(listOf(y))
        val xArray = MutableList(degree + 1) { 0.0 }
        for (i in xArray.indices) {
            xArray[i] = x.pow(i.toDouble())
        }
        xPointMatrix.add(xArray)
    }

    fun fit(): List<Double> {
        val xMatrix = Matrix(xPointMatrix)
        val yMatrix = Matrix(yPoints)

        val coeffsMatrix = xMatrix.transpose()
            .multiply(xMatrix)
            .inverse()
            .multiply(xMatrix.transpose())
            .multiply(yMatrix)
        
        val coeffsRow = coeffsMatrix.transpose().data[0]
        return coeffsRow
    }
}