package net.sbo.mod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.sbo.mod.utils.events.SBOEvent;
import net.sbo.mod.utils.events.impl.entity.PlayerMeleeAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "attack", at = @At("HEAD"))
    private void onMeleeAttack(Entity target, CallbackInfo ci) {
        PlayerEntity attacker = (PlayerEntity) (Object) this;
        if (attacker.getUuid().version() == 4) {
            SBOEvent.INSTANCE.emit(new PlayerMeleeAttackEvent(
                    attacker,
                    target,
                    attacker.getMainHandStack()
            ));
        }
    }
}