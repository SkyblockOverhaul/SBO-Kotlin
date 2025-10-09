package net.sbo.mod.mixin;

import gg.essential.universal.UScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.sbo.mod.SBOKotlin;
import net.sbo.mod.guis.AchievementsGUI;
import net.sbo.mod.guis.Guis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        GameMenuScreen self = (GameMenuScreen)(Object)this;

        ButtonWidget button = ButtonWidget.builder(Text.literal("SBO"), b -> SBOKotlin.mc.send (() -> {
            if (Guis.INSTANCE.getAchievementsGui$SBO() == null) {
                Guis.INSTANCE.setAchievementsGui$SBO(new AchievementsGUI());
            }
            UScreen.displayScreen(Guis.INSTANCE.getAchievementsGui$SBO());
        })).dimensions(self.width / 2 + 104, self.height / 4 + 32, 30, 20).build();

        this.addDrawableChild(button);
    }
}
