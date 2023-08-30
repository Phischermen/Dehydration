package net.dehydration.init;

import net.dehydration.access.PlayerAccess;
import net.dehydration.access.ServerPlayerAccess;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.thirst.ThirstManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class EventInit {

    private static final Identifier ZOMBIE_LOOT_TABLE_ID = new Identifier("minecraft", "entities/zombie");

    public static void init() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            ((ServerPlayerAccess) player).compatSync();
        });

        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, supplier, setter) -> {
            if (id.equals(new Identifier(LootTables.SPAWN_BONUS_CHEST.toString()))) {
                LootPool pool = LootPool.builder().with(ItemEntry.builder(Items.GLASS_BOTTLE).build()).rolls(BinomialLootNumberProvider.create(5, 0.9F)).build();
                supplier.pool(pool);
            }
            if (setter.isBuiltin() && ZOMBIE_LOOT_TABLE_ID.equals(id)){
                // todo this does not actually add a thick potion to the zombie loot table yet. Just an uncraftable one.
                var awkwardEntry = PotionUtil.setPotion(Items.POTION.getDefaultStack(), Potions.THICK).getItem();
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.5F))
                        .with(ItemEntry.builder(awkwardEntry)).build();
                supplier.pool(pool);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, result) -> {
            if (!player.isCreative() && !player.isSpectator() && player.isSneaking() && player.getMainHandStack().isEmpty()) {
                HitResult hitResult = player.raycast(1.5D, 0.0F, true);
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                if (world.canPlayerModifyAt(player, blockPos) && world.getFluidState(blockPos).isIn(FluidTags.WATER)
                        && (world.getFluidState(blockPos).isStill() || ConfigInit.CONFIG.allow_non_flowing_water_sip)) {
                    ThirstManager thirstManager = ((ThirstManagerAccess) player).getThirstManager();
                    if (thirstManager.isNotFull()) {
                        int drinkTime = ((PlayerAccess) player).getDrinkTime();
                        boolean waterIsPure = world.getFluidState(blockPos).isIn(TagInit.PURIFIED_WATER);
                        float sipThirstChance = ConfigInit.CONFIG.water_sip_thirst_chance;
                        if (world.getBiome(blockPos).isIn(BiomeTags.IS_RIVER)){
                            // Rivers are totally safe to drink from.
                            sipThirstChance = 0;
                        } else if (world.getBiome(blockPos).isIn(BiomeTags.IS_BEACH) || world.getBiome(blockPos).isIn(BiomeTags.IS_OCEAN) || world.getBiome(blockPos).isIn(BiomeTags.IS_DEEP_OCEAN)){
                            // Ocean water is not safe at all.
                            sipThirstChance = 1;
                        }

                        boolean willApplyThirst = !waterIsPure && world.random.nextFloat() <= sipThirstChance;
                        if (world.isClient && drinkTime % 3 == 0){
                            player.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                            if (willApplyThirst){
                                // Play a sound that indicates the water is not pure.
                                player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 1.0F, world.random.nextFloat() * 0.1f + 0.9f);
                            }
                        }


                        if (drinkTime > 20) {
                            if (!world.isClient) {
                                if (!ConfigInit.CONFIG.allow_non_flowing_water_sip && world.getFluidState(blockPos).isStill())
                                    if (world.getBlockState(blockPos).contains(Properties.WATERLOGGED))
                                        world.setBlockState(blockPos, world.getBlockState(blockPos).with(Properties.WATERLOGGED, false));
                                    else
                                        world.setBlockState(blockPos, Blocks.AIR.getDefaultState());

                                thirstManager.add(ConfigInit.CONFIG.water_souce_quench);
                                if (willApplyThirst) {
                                    player.addStatusEffect(new StatusEffectInstance(EffectInit.THIRST, ConfigInit.CONFIG.water_sip_thirst_duration, 1, false, false, true));
                                }
                            } else {
                                world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundInit.WATER_SIP_EVENT, SoundCategory.PLAYERS, 1.0F, 0.9F + (world.random.nextFloat() / 5F));
                            }
                            ((PlayerAccess) player).setDrinkTime(0);
                            return ActionResult.SUCCESS;
                        }

                        ((PlayerAccess) player).setDrinkTime(drinkTime + 1);
                    }
                }
                return ActionResult.PASS;
            }
            return ActionResult.PASS;
        });
    }
}
