package net.sbo.mod.utils.accessors

import net.minecraft.entity.Entity
import net.minecraft.util.Formatting
import java.awt.Color

/**
 * Interface to inject into Entity.class via Mixin
 */
internal interface EntityAccessor {
    fun `sbo$setGlowing`(glowing: Boolean)
    fun `sbo$setGlowingColor`(color: Int)
    fun `sbo$glowTime`(time: Long)
    fun `sbo$setGlowingThisFrame`(glowing: Boolean)
}

// Extension property to easily toggle custom glowing
var Entity.isSboGlowing: Boolean
    get() = this.isGlowing
    set(value) {
        (this as? EntityAccessor)?.`sbo$setGlowing`(value)
    }

// Extension property to set the color
var Entity.sboGlowingColor: Int
    get() = this.teamColorValue
    set(value) {
        (this as? EntityAccessor)?.`sbo$setGlowingColor`(value)
    }

// Helper to set color using java.awt.Color
fun Entity.setSboGlowColor(color: Color) {
    this.sboGlowingColor = color.rgb
}