/* Copyright (c) 2021-2022 DeflatedPickle under the MIT license */

package net.dehydration.mixin;

import net.dehydration.config.DehydrationConfig;
import net.dehydration.init.ConfigInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnusedMixin")
@Mixin(CowEntity.class)
public abstract class CowEntityMixin extends AnimalEntity {

    @Shadow
    protected abstract SoundEvent getAmbientSound();

    private static final TrackedData<Integer> MILK_TICKS =
            DataTracker.registerData(PassiveEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final String NBT_KEY = "Milk";

    protected CowEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    public int getMilkTicks() {
        return this.dataTracker.get(MILK_TICKS);
    }

    public void setMilkTicks(int ticks) {
        this.dataTracker.set(MILK_TICKS, ticks);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MILK_TICKS, ConfigInit.CONFIG.milk_ticks_max);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    public void interactMob(
            PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (ConfigInit.CONFIG.milk_ticks_max <= 0){
            // Milking should be instant if 0 or negative.
            return;
        }
        var handItemsIterator = player.getHandItems().iterator();
        var hasBucket = false;
        if (handItemsIterator.next().isOf(Items.BUCKET)){
            hand = Hand.MAIN_HAND;
            hasBucket = true;
        } else if (handItemsIterator.next().isOf(Items.BUCKET)){
            hand = Hand.OFF_HAND;
            hasBucket = true;
        }
        if (hasBucket && getMilkTicks() > 0) {
            float pitch = 1.0F +
                    (1 - getMilkTicks() / (float) ConfigInit.CONFIG.milk_ticks_max) * 0.5F;
            this.playSound(getHurtSound(null), 0.5F,  pitch);
            spawnPlayerReactionParticles(player);
            player.swingHand(hand);
            cir.setReturnValue(super.interactMob(player, hand));
        }
    }

    @Inject(
            method = "interactMob",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;",
                    shift = At.Shift.BEFORE))
    public void interactMobMilkTicks(
            PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        setMilkTicks(ConfigInit.CONFIG.milk_ticks_max);
    }

    @Override
    public void tick() {
        super.tick();

        setMilkTicks(Math.max(0, getMilkTicks() - 1));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(NBT_KEY, this.getMilkTicks());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setMilkTicks(nbt.getInt(NBT_KEY));
    }

    public void spawnPlayerReactionParticles(PlayerEntity player) {
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;

            player.getWorld().addParticle(
                    ParticleTypes.SMOKE,
                    this.getParticleX(1.0D),
                    this.getRandomBodyY() + 0.5D,
                    this.getParticleZ(1.0D),
                    d,
                    e,
                    f);
        }
    }
}