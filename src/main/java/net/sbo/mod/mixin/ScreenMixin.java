package net.sbo.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.sbo.mod.utils.events.SBOEvent;
import net.sbo.mod.utils.events.impl.GuiKeyEvent;
import net.sbo.mod.utils.events.impl.GuiPostRenderEvent;
import net.sbo.mod.utils.events.impl.GuiRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = (Screen) (Object) this;
        SBOEvent.INSTANCE.emit(new GuiRenderEvent(client, screen, context, mouseX, mouseY, delta));
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void onPostRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = (Screen) (Object) this;
        SBOEvent.INSTANCE.emit(new GuiPostRenderEvent(client, screen, context, mouseX, mouseY, delta));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen screen = (Screen) (Object) this;
        SBOEvent.INSTANCE.emit(new GuiKeyEvent(client, screen, keyCode, cir));
    }
}
