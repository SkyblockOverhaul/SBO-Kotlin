package net.sbo.mod.guis
//
//import net.minecraft.client.gui.DrawContext
//import xyz.meowing.vexel.components.base.Pos
//import xyz.meowing.vexel.components.base.Size
//import xyz.meowing.vexel.components.core.Container
//import xyz.meowing.vexel.components.core.Rectangle
//import xyz.meowing.vexel.components.core.Text
//import xyz.meowing.vexel.core.VexelScreen
//import xyz.meowing.vexel.elements.Button
//import xyz.meowing.vexel.elements.CheckBox
//import xyz.meowing.vexel.elements.ColorPicker
//import xyz.meowing.vexel.elements.Dropdown
//import xyz.meowing.vexel.elements.Keybind
//import xyz.meowing.vexel.elements.NumberInput
//import xyz.meowing.vexel.elements.Slider
//import xyz.meowing.vexel.elements.Switch
//import xyz.meowing.vexel.elements.TextInput
//import java.awt.Color
//
//class VexelTest : VexelScreen() {
//    private val rootContainer = Rectangle()
//        .backgroundColor(0x80121212.toInt())
//        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
//        .padding(20f)
//
//    private var testCounter = 0
//
//    override fun afterInitialization() {
//        rootContainer.childOf(window)
//        setupUI()
//    }
//
//    private fun setupUI() {
//        rootContainer.children.clear()
//
//        Text("Complete Component Test Suite")
//            .color(0xFFFFFFFF.toInt())
//            .fontSize(28f)
//            .shadow(true)
//            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.ParentPixels)
//            .childOf(rootContainer)
//
//        Text("Scroll to test all components")
//            .color(0xFF9CA3AF.toInt())
//            .fontSize(16f)
//            .setPositioning(0f, Pos.ParentCenter, 10f, Pos.AfterSibling)
//            .childOf(rootContainer)
//
//        val mainScrollArea = Rectangle()
//            .backgroundColor(0x80202020.toInt())
//            .borderRadius(12f)
//            .borderColor(0xFF404040.toInt())
//            .borderThickness(2f)
//            .setSizing(90f, Size.ParentPerc, 75f, Size.ParentPerc)
//            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.AfterSibling)
//            .padding(30f)
//            .scrollable(true)
//            .childOf(rootContainer)
//
//        setupScrollableContent(mainScrollArea)
//    }
//
//    private fun setupScrollableContent(container: Rectangle) {
//        Text("Text Input Components")
//            .color(0xFF60A5FA.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
//            .childOf(container)
//
//        repeat(5) { i ->
//            TextInput("Sample text $i", "Enter text here...")
//                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .fontSize(16f)
//                .onValueChange { text ->
//                    println("Text input $i: $text")
//                }
//                .childOf(container)
//        }
//
//        Dropdown(
//            listOf(
//                "Option 1",
//                "Option 2",
//                "Option 3",
//                "Option 4",
//                "Option 5",
//                "Option 6",
//                "Option 7",
//                "Option 8",
//                "Option 9",
//                "Option A",
//                "Option B",
//                "Option C"
//            ), 0
//        )
//            .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//            .fontSize(16f)
//            .onValueChange { index ->
//                println("Dropdown selected index: $index")
//            }
//            .childOf(container)
//
//        Dropdown(listOf("Option 1", "Option 2"), 1)
//            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.MatchSibling)
//            .fontSize(16f)
//            .onValueChange { index ->
//                println("Dropdown selected index: $index")
//            }
//            .childOf(container)
//
//        Dropdown(listOf("Option 1", "Option 2", "Option 3"), 1)
//            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.MatchSibling)
//            .fontSize(16f)
//            .onValueChange { index ->
//                println("Dropdown selected index: $index")
//            }
//            .childOf(container)
//
//        Dropdown(listOf("Option 1", "Option 2", "Option 3", "Option 4"), 1)
//            .setPositioning(5f, Pos.AfterSibling, 0f, Pos.MatchSibling)
//            .fontSize(16f)
//            .onValueChange { index ->
//                println("Dropdown selected index: $index")
//            }
//            .childOf(container)
//
//        Text("Number Input Components")
//            .color(0xFF10B981.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(4) { i ->
//            NumberInput(i * 1000, "Enter number...")
//                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .fontSize(16f)
//                .onValueChange { value ->
//                    println("Number input $i: $value")
//                }
//                .childOf(container)
//        }
//
//        Text("Buttons")
//            .color(0xFFEF4444.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        val buttonColors = listOf(
//            0xFF3B82F6.toInt() to 0xFF2563EB.toInt(),
//            0xFF10B981.toInt() to 0xFF059669.toInt(),
//            0xFFEF4444.toInt() to 0xFFDC2626.toInt(),
//            0xFFF59E0B.toInt() to 0xFFD97706.toInt(),
//            0xFF8B5CF6.toInt() to 0xFF7C3AED.toInt()
//        )
//
//        repeat(8) { i ->
//            val (bg, hover) = buttonColors[i % buttonColors.size]
//            Button("Test Button $i")
//                .backgroundColor(bg)
//                .hoverColors(bg = hover)
//                .textColor(0xFFFFFFFF.toInt())
//                .borderRadius(8f)
//                .padding(15f, 30f, 15f, 30f)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .onClick { _, _, _ ->
//                    testCounter++
//                    println("Button $i clicked! Total clicks: $testCounter")
//                    true
//                }
//                .childOf(container)
//        }
//
//        Text("Checkboxes")
//            .color(0xFFF59E0B.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(6) { i ->
//            val checkContainer = Rectangle()
//                .backgroundColor(0x00000000)
//                .setSizing(100f, Size.ParentPerc, 30f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                .childOf(container)
//
//            CheckBox()
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .onValueChange { checked ->
//                    println("Checkbox $i: $checked")
//                }
//                .childOf(checkContainer)
//
//            Text("Checkbox $i")
//                .color(0xFFE5E7EB.toInt())
//                .fontSize(14f)
//                .setPositioning(30f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .childOf(checkContainer)
//        }
//
//        Text("Switches")
//            .color(0xFF8B5CF6.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(5) { i ->
//            val switchContainer = Rectangle()
//                .backgroundColor(0x00000000)
//                .setSizing(100f, Size.ParentPerc, 35f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                .childOf(container)
//
//            Switch()
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .trackEnabledColor(when (i % 3) {
//                    0 -> 0xFF10B981.toInt()
//                    1 -> 0xFF3B82F6.toInt()
//                    else -> 0xFFF59E0B.toInt()
//                })
//                .onValueChange { enabled ->
//                    println("Switch $i: $enabled")
//                }
//                .childOf(switchContainer)
//
//            Text("Switch $i")
//                .color(0xFFE5E7EB.toInt())
//                .fontSize(14f)
//                .setPositioning(60f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .childOf(switchContainer)
//        }
//
//        Text("Sliders")
//            .color(0xFF06B6D4.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(4) { i ->
//            val sliderContainer = Rectangle()
//                .backgroundColor(0x00000000)
//                .setSizing(100f, Size.ParentPerc, 50f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .childOf(container)
//
//            val sliderLabel = Text("Slider $i: 50%")
//                .color(0xFFD1D5DB.toInt())
//                .fontSize(14f)
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
//                .childOf(sliderContainer)
//
//            Slider()
//                .setValue(0.5f)
//                .setSizing(100f, Size.ParentPerc, 25f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
//                .trackFillColor(when (i % 4) {
//                    0 -> 0xFF3B82F6.toInt()
//                    1 -> 0xFF10B981.toInt()
//                    2 -> 0xFFEF4444.toInt()
//                    else -> 0xFF8B5CF6.toInt()
//                })
//                .onValueChange { value ->
//                    val v = value as Float
//                    sliderLabel.text("Slider $i: ${(v * 100).toInt()}%")
//                }
//                .childOf(sliderContainer)
//        }
//
//        Text("Color Pickers")
//            .color(0xFFEC4899.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(3) { i ->
//            val colorContainer = Rectangle()
//                .backgroundColor(0x00000000)
//                .setSizing(100f, Size.ParentPerc, 40f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .childOf(container)
//
//            val colorLabel = Text("Color Picker $i")
//                .color(0xFFE5E7EB.toInt())
//                .fontSize(14f)
//                .setPositioning(50f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .childOf(colorContainer)
//
//            ColorPicker(Color.getHSBColor(i / 3f, 0.8f, 0.9f))
//                .setSizing(35f, Size.Pixels, 30f, Size.Pixels)
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                .onValueChange { color ->
//                    val c = color as Color
//                    println("Color picker $i: #${Integer.toHexString(c.rgb).substring(2).uppercase()}")
//                }
//                .childOf(colorContainer)
//        }
//
//        Text("Keybind Controls")
//            .color(0xFF84CC16.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(3) { i ->
//            val keybindContainer = Rectangle()
//                .backgroundColor(0x00000000)
//                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                .setPositioning(0f, Pos.ParentPixels, 15f, Pos.AfterSibling)
//                .childOf(container)
//
//            Text("Keybind $i:")
//                .color(0xFFE5E7EB.toInt())
//                .fontSize(14f)
//                .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
//                .childOf(keybindContainer)
//
//            Keybind()
//                .setPositioning(0f, Pos.ParentPixels, 10f, Pos.AfterSibling)
//                .onValueChange { keyCode ->
//                    println("Keybind $i set to: $keyCode")
//                }
//                .childOf(keybindContainer)
//        }
//
//        Text("Mixed Components")
//            .color(0xFFF97316.toInt())
//            .fontSize(20f)
//            .setPositioning(0f, Pos.ParentPixels, 30f, Pos.AfterSibling)
//            .childOf(container)
//
//        repeat(10) { i ->
//            when (i % 4) {
//                0 -> {
//                    TextInput("Mixed input $i", "Type something...")
//                        .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                        .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                        .fontSize(15f)
//                        .childOf(container)
//                }
//                1 -> {
//                    NumberInput(i * 777, "Enter value...")
//                        .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
//                        .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                        .fontSize(15f)
//                        .childOf(container)
//                }
//                2 -> {
//                    Button("Mixed Button $i")
//                        .backgroundColor(0xFF6366F1.toInt())
//                        .hoverColors(bg = 0xFF4F46E5.toInt())
//                        .textColor(0xFFFFFFFF.toInt())
//                        .borderRadius(6f)
//                        .padding(12f, 24f, 12f, 24f)
//                        .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                        .onClick { _, _, _ ->
//                            println("Mixed button $i clicked!")
//                            true
//                        }
//                        .childOf(container)
//                }
//                else -> {
//                    val mixedContainer = Rectangle()
//                        .backgroundColor(0x00000000)
//                        .setSizing(100f, Size.ParentPerc, 30f, Size.Pixels)
//                        .setPositioning(0f, Pos.ParentPixels, 12f, Pos.AfterSibling)
//                        .childOf(container)
//
//                    CheckBox()
//                        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                        .childOf(mixedContainer)
//
//                    Switch()
//                        .setPositioning(40f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                        .childOf(mixedContainer)
//
//                    Text("Mixed $i")
//                        .color(0xFFD1D5DB.toInt())
//                        .fontSize(14f)
//                        .setPositioning(100f, Pos.ParentPixels, 0f, Pos.ParentCenter)
//                        .childOf(mixedContainer)
//                }
//            }
//        }
//
//        val finalButton = Button("Final Test Button")
//            .backgroundColor(0xFFDC2626.toInt())
//            .hoverColors(bg = 0xFFB91C1C.toInt())
//            .textColor(0xFFFFFFFF.toInt())
//            .borderRadius(10f)
//            .padding(20f, 40f, 20f, 40f)
//            .setPositioning(0f, Pos.ParentCenter, 40f, Pos.AfterSibling)
//            .onClick { _, _, _ ->
//                println("Final button clicked! Test complete.")
//                true
//            }
//            .childOf(container)
//
//        Text("End of test components - scroll back up!")
//            .color(0xFF9CA3AF.toInt())
//            .fontSize(14f)
//            .setPositioning(0f, Pos.ParentCenter, 20f, Pos.AfterSibling)
//            .childOf(container)
//    }
//
//    override fun shouldPause(): Boolean = false
//}