package com.faraway;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.mojang.text2speech.Narrator.LOGGER;
@Mod("faraway")
@EventBusSubscriber(modid = "faraway")
public class faraway {
    public faraway () {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Setup code here (if needed)
    }
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
//        if (!(event.getEntity() instanceof Monster)) {
//            return;
//        }
        if (event.getEntity() instanceof Monster){
            LOGGER.info("Entering");
            Monster mobEntity = (Monster) event.getEntity();
            CompoundTag NBT = mobEntity.getPersistentData();
            if (!NBT.getBoolean("HasBoosted")) {
                AttributeInstance healthAttribute = mobEntity.getAttribute(Attributes.MAX_HEALTH);
                double baseHealth = healthAttribute.getBaseValue();
                LOGGER.info("HP: " +baseHealth);
                double hpMultiplier = (Math.sqrt(mobEntity.blockPosition().distSqr(event.getLevel().getSharedSpawnPos())))/100;
                healthAttribute.setBaseValue(baseHealth * hpMultiplier);
                // Since the entity is just joining the world, set its health to the new max
                mobEntity.setHealth(mobEntity.getMaxHealth());
                LOGGER.info("Boost to: " +mobEntity.getMaxHealth());
                NBT.putBoolean("HasBoosted", true);
            }
        }
    }
}

