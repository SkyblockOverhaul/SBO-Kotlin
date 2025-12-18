package net.sbo.mod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.sbo.mod.utils.events.SBOEvent;
import net.sbo.mod.utils.events.impl.entity.EntityPlayerDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player && entity.getUuid().version() != 4) {
            if (status == 2 || status == 3) {
                SBOEvent.INSTANCE.emit(new EntityPlayerDamageEvent(
                        player.getId(),
                        player,
                        player.getName().getString(),
                        status
                ));
            }
        }
    }

    @Inject(method = "animateDamage", at = @At("HEAD"))
    private void onAnimateDamage(float yaw, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PlayerEntity player && entity.getUuid().version() != 4) {
            SBOEvent.INSTANCE.emit(new EntityPlayerDamageEvent(
                    player.getId(),
                    player,
                    player.getName().getString(),
                    (byte) 2
            ));
        }
    }
}