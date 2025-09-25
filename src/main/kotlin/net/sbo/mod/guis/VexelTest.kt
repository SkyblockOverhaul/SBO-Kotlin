package net.sbo.mod.guis

import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.core.VexelScreen
import java.awt.Color

class VexelTest : VexelScreen() {
    override fun afterInitialization() {
        Rectangle()
            .backgroundColor(Color.WHITE.rgb)
            .borderColor(Color.BLACK.rgb)
            .borderRadius(8f)
            .borderThickness(2f)
            .padding(16f)
            .hoverColor(Color.BLUE.rgb)
            .dropShadow()
            .setSizing(200f, Size.Pixels, 100f, Size.Pixels)
            .childOf(window)
    }
}