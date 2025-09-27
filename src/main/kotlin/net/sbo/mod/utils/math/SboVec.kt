package net.sbo.mod.utils.math

import net.minecraft.util.math.Vec3d
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

data class SboVec(var x: Double, var y: Double, var z: Double) {

    fun distanceTo(other: SboVec): Double {
        return sqrt((other.x - x).pow(2) + (other.y - y).pow(2) + (other.z - z).pow(2))
    }

    fun distanceTo(x: Double, y: Double, z: Double): Double {
        return sqrt((x - this.x).pow(2) + (y - this.y).pow(2) + (z - this.z).pow(2))
    }

    operator fun plus(other: SboVec): SboVec {
        return SboVec(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: SboVec): SboVec {
        return SboVec(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(d: Double): SboVec {
        return SboVec(x * d, y * d, z * d)
    }

    fun clone(): SboVec = copy()

    fun down(amount: Double): SboVec {
        return copy(y = y - amount)
    }

    fun roundLocationToBlock(): SboVec {
        return SboVec(floor(x), floor(y), floor(z))
    }

    fun toVec3d(): Vec3d {
        return Vec3d(x, y, z)
    }

    fun center(): SboVec {
        return SboVec(x + 0.5, y + 0.5, z + 0.5)
    }

    fun toCleanString(): String {
        return "%.2f, %.2f, %.2f".format(x, y, z)
    }

    fun toDoubleArray(): DoubleArray {
        return doubleArrayOf(x, y, z)
    }

    fun length(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    companion object {
        fun fromArray(arr: List<Double>): SboVec {
            require(arr.size >= 3) { "Array must contain at least 3 elements for x, y, z." }
            return SboVec(arr[0], arr[1], arr[2])
        }
    }
}