package net.sbo.mod.utils.events.impl.game

import net.minecraft.text.Text

/**
 * Event fired when a chat message is sent or received.
 * @param message The chat message text.
 * @param signed Whether the message is signed.
 */
class ChatMessageEvent(val message: Text, val signed: Boolean)