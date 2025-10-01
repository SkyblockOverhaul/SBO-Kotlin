package net.sbo.mod.utils.events.impl.game

import net.minecraft.text.Text

class ChatMessageAllowEvent(val message: Text, val signed: Boolean, var isAllowed: Boolean)