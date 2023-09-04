package net.dehydration.mixin;

import java.util.Optional;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dehydration.init.BlockInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BlockWithEntity {
    @Shadow
    private boolean emitsParticles;

    public CampfireBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "onUse", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/entity/CampfireBlockEntity;getRecipeFor(Lnet/minecraft/item/ItemStack;)Ljava/util/Optional;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onUseMixin(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info, BlockEntity blockEntity,
            CampfireBlockEntity campfireBlockEntity, ItemStack itemStack, Optional<CampfireCookingRecipe> optional) {
        if (!world.isClient && itemStack.getItem() instanceof PotionItem && PotionUtil.getPotion(itemStack) == Potions.WATER
                && campfireBlockEntity.addItem(player, player.getAbilities().creativeMode ? itemStack.copy() : itemStack, 1000)) {
            player.incrementStat(Stats.INTERACT_WITH_CAMPFIRE);
            info.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (world.getBlockState(pos.up()).getBlock() == BlockInit.CAMPFIRE_CAULDRON_BLOCK) {
            world.breakBlock(pos.up(), !player.isCreative());
        }
    }
    
    @Inject(method = "spawnSmokeParticle", at = @At("HEAD"), cancellable = true)
    private static void spawnSmokeParticleMixin(World world, BlockPos pos, boolean isSignal, boolean lotsOfSmoke, CallbackInfo ci){
        if (copperCauldronOnTop(world, pos)) {
            ci.cancel();
            pos = pos.up(2);
            Random random = world.getRandom();
            DefaultParticleType defaultParticleType = isSignal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
            world.addImportantParticle(defaultParticleType, true, (double)pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + random.nextDouble() + random.nextDouble(), (double)pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
            if (lotsOfSmoke) {
                world.addParticle(ParticleTypes.SMOKE, (double)pos.getX() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1), (double)pos.getY() + 0.4, (double)pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if ((Boolean)state.get(CampfireBlock.LIT)) {
            if (random.nextInt(10) == 0) {
                world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            if (this.emitsParticles && random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    if (copperCauldronOnTop(world, pos)){
                        world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + (double)(random.nextFloat()), (double)pos.getY() + 0.5, (double)pos.getZ() + (double)(random.nextFloat()), (double)(random.nextFloat() / 2.0F), 5.0E-5, (double)(random.nextFloat() / 2.0F));
                    } else{
                        world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, (double)(random.nextFloat() / 2.0F), 5.0E-5, (double)(random.nextFloat() / 2.0F));
                    }

                }
            }

        }
    }

    private static boolean copperCauldronOnTop(World world, BlockPos pos) { return world.getBlockState(pos.up()).getBlock() == BlockInit.CAMPFIRE_CAULDRON_BLOCK; }
}
