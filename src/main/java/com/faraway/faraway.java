package com.faraway;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
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
import net.minecraftforge.server.ServerLifecycleHooks;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.mojang.text2speech.Narrator.LOGGER;

import static org.apache.logging.log4j.Level.getLevel;


@Mod("faraway")
@EventBusSubscriber(modid = "faraway")
public class faraway {
    private static BlockPos SpawnPos;
    private static MinecraftServer server;
    private static final Random RANDOM = new Random();
    private static ServerLevel overworld;
    public static final Map<Integer, EntityType<?>[]> ALLOWED_ENTITIES = new HashMap<>();

    static {
        ALLOWED_ENTITIES.put(500, new EntityType<?>[]{
                EntityType.ZOMBIE,
        });
        ALLOWED_ENTITIES.put(1000, new EntityType<?>[]{
                EntityType.ZOMBIE,
                EntityType.SKELETON
        });
        ALLOWED_ENTITIES.put(1500, new EntityType<?>[]{
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.CREEPER
        });
//        ALLOWED_ENTITIES.put(2000, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });
//        ALLOWED_ENTITIES.put(2500, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });
//        ALLOWED_ENTITIES.put(3000, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });
//        ALLOWED_ENTITIES.put(3500, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });
//        ALLOWED_ENTITIES.put(4000, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });
//        ALLOWED_ENTITIES.put(4500, new EntityType<?>[]{
//                EntityType.ZOMBIE,
//                EntityType.SKELETON
//        });

    }
    public faraway () {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }
    private void setup(final FMLCommonSetupEvent event) {
    }


    //record spawnposition when serverstarted
    //优化——能否只记录一次，而不是每次启动都记录
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        //LOGGER.info("SERVER STARTING: Fetching original spawn position.");
        server = event.getServer();
        overworld = server.getLevel(Level.OVERWORLD);
        SpawnPos = overworld.getSharedSpawnPos();
        //LOGGER.info("Original Spawn Position: " + SpawnPos);
    }



    @SubscribeEvent  //adjust HP/ATK when mob first joined
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Monster){
            Monster mobEntity = (Monster) event.getEntity();
            strengthMob(mobEntity,mobEntity.blockPosition());

        }
    }

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
           if (event.getEntity() instanceof Monster){
               BlockPos entityPos = event.getEntity().blockPosition();
               double distance = SpawnPos.distManhattan(entityPos);
               LOGGER.info("away by "+distance);
               Integer maxAllowedDistance = faraway.ALLOWED_ENTITIES.keySet().stream()
                       .filter(d -> d >= distance) // Keep only keys greater than or equal to the distance
                       .min(Integer::compareTo) // Find the smallest of those keys
                       .orElse(null);
                if (maxAllowedDistance == null) return;
               LOGGER.info("Only allowed outside "+maxAllowedDistance);
               EntityType<?>[] allowedEntities = faraway.ALLOWED_ENTITIES.get(maxAllowedDistance);
               if (Arrays.asList(allowedEntities).contains(event.getEntity().getType())) return;
               event.setSpawnCancelled(true);
               LOGGER.info("Denied "+event.getEntity().getType());

               //Spawn correct one
               EntityType<?> entityAllowed = allowedEntities[RANDOM.nextInt(allowedEntities.length)];
               Entity newEntity = entityAllowed.create(overworld);  //!!!!!!!!!!test if mob from epic fight would not work
               newEntity.setPos(entityPos.getX(),entityPos.getY(),entityPos.getZ());
               overworld.addFreshEntity(newEntity);
               Monster mobEntity = (Monster) newEntity;
               strengthMob(mobEntity,newEntity.blockPosition());
               };

           }
public static void strengthMob(Monster mobEntity,BlockPos entityPos) {
        CompoundTag NBT = mobEntity.getPersistentData();
        if (!NBT.getBoolean("HasBoosted")) {
            AttributeInstance healthAttribute = mobEntity.getAttribute(Attributes.MAX_HEALTH);
            double baseHealth = healthAttribute.getBaseValue();
           // LOGGER.info("HP: " +baseHealth);
            double hpMultiplier = (Math.sqrt(entityPos.distSqr(SpawnPos)))/100;
            healthAttribute.setBaseValue(baseHealth * hpMultiplier);
            mobEntity.setHealth(mobEntity.getMaxHealth());
            LOGGER.info("Boost to: " +mobEntity.getMaxHealth());
            NBT.putBoolean("HasBoosted", true);
        }
    }

//

      //  private static final double INNER_RADIUS = 1000.0;
      // private static final double OUTER_RADIUS = 2000.0;
//        @SubscribeEvent
//        public static void onMobSpawn(MobSpawnEvent event) {
//            if (event.getEntity() instanceof Monster){
//                //LOGGER.info("spawned");
//                BlockPos entityPos = event.getEntity().blockPosition();
//                double distance = SpawnPos.distManhattan(entityPos);
//                LOGGER.info("away by "+distance);
//                Integer maxAllowedDistance = faraway.ALLOWED_ENTITIES.keySet().stream()
//                        .filter(d -> distance <= d)
//                        .max(Integer::compareTo)
//                        .orElse(null);
//                if (maxAllowedDistance == null) return;
//                LOGGER.info("Only allowed in "+maxAllowedDistance);
//                EntityType<?>[] allowedEntities = faraway.ALLOWED_ENTITIES.get(maxAllowedDistance);
//                if (Arrays.asList(allowedEntities).contains(event.getEntity().getType())) return;
//                LOGGER.info("this is "+event.getEntity().getType());
//                // Cancel the original spawn
//                event.setCanceled(true);
//                LOGGER.info("Denied "+event.getEntity().getType());
//
//                EntityType<?> entityAllowed = allowedEntities[RANDOM.nextInt(allowedEntities.length)];
//                Entity newEntity = entityAllowed.create(overworld);
//                newEntity.moveTo(entityPos.getX() + 0.5, entityPos.getY(), entityPos.getZ() + 0.5, 0.0F, 0.0F);
//                overworld.addFreshEntity(newEntity);
//
//        }
//        }
}


