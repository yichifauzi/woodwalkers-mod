package tocraft.walkers.registry;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import tocraft.walkers.Walkers;
import tocraft.walkers.api.PlayerHostility;
import tocraft.walkers.api.PlayerShape;
import tocraft.walkers.skills.SkillRegistry;
import tocraft.walkers.skills.impl.RiderSkill;

public class WalkersEventHandlers {

    public static void initialize() {
        registerHostilityUpdateHandler();
        registerEntityRidingHandler();
        registerPlayerRidingHandler();
        registerLivingDeathHandler();
        registerHandlerForDeprecatedEntityTags();
    }

    @SuppressWarnings({"deprecation"})
    public static void registerHandlerForDeprecatedEntityTags() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register((serverLevel) -> {
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                // print warnings for deprecated entity tags
                if (entityType.is(WalkersEntityTags.BURNS_IN_DAYLIGHT)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.BURNS_IN_DAYLIGHT + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.FLYING)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.FLYING + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.SLOW_FALLING)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.SLOW_FALLING + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.WOLF_PREY)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.WOLF_PREY + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.FOX_PREY)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.FOX_PREY + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.HURT_BY_HIGH_TEMPERATURE)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.HURT_BY_HIGH_TEMPERATURE + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.RAVAGER_RIDING)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.RAVAGER_RIDING + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.LAVA_WALKING)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.LAVA_WALKING + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.FALL_THROUGH_BLOCKS)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.FALL_THROUGH_BLOCKS + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.CANT_SWIM)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.CANT_SWIM + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.UNDROWNABLE)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.UNDROWNABLE + " for " + entityType);
                }
                if (entityType.is(WalkersEntityTags.BLACKLISTED)) {
                    Walkers.LOGGER.warn("Woodwalkers Warning: Please merge to the new skills system. Found " + WalkersEntityTags.BLACKLISTED + " for " + entityType);
                }
            }
        });
    }

    public static void registerHostilityUpdateHandler() {
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (!player.level.isClientSide && Walkers.CONFIG.playerCanTriggerHostiles && entity instanceof Monster) {
                PlayerHostility.set(player, Walkers.CONFIG.hostilityTime);
            }

            return EventResult.pass();
        });
    }

    // Players with an equipped Walkers inside the `ravager_riding` entity tag
    // should
    // be able to ride Ravagers.
    public static void registerEntityRidingHandler() {
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            LivingEntity shape = PlayerShape.getCurrentShape(player);
            if (shape != null && entity instanceof LivingEntity livingEntity) {
                // checks, if selected entity is rideable
                for (RiderSkill<?> riderSkill : SkillRegistry.get(shape, RiderSkill.ID).stream().map(entry -> (RiderSkill<?>) entry).toList()) {
                    if (riderSkill.isRideable(livingEntity) || (livingEntity instanceof Player rideablePlayer && riderSkill.isRideable(PlayerShape.getCurrentShape(rideablePlayer)))) {
                        player.startRiding(entity);
                        return EventResult.pass();
                    }
                }
            }
            return EventResult.pass();
        });
    }

    // make this server-side
    public static void registerPlayerRidingHandler() {
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (entity instanceof Player playerToBeRidden) {
                if (PlayerShape.getCurrentShape(playerToBeRidden) instanceof AbstractHorse) {
                    player.startRiding(playerToBeRidden, true);
                }
            }
            return EventResult.pass();
        });
    }

    public static void registerLivingDeathHandler() {
        EntityEvent.LIVING_DEATH.register((entity, damageSource) -> {
            if (!entity.level.isClientSide()) {
                if (entity instanceof Villager villager && damageSource.getEntity() instanceof Player player && PlayerShape.getCurrentShape(player) instanceof Zombie) {
                    if (!(player.level.getDifficulty() != Difficulty.HARD && player.getRandom().nextBoolean())) {
                        ZombieVillager zombievillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
                        if (zombievillager != null) {
                            zombievillager.finalizeSpawn((ServerLevelAccessor) player.level, player.level.getCurrentDifficultyAt(zombievillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null);
                            zombievillager.setVillagerData(villager.getVillagerData());
                            zombievillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
                            zombievillager.setTradeOffers(villager.getOffers().createTag());
                            zombievillager.setVillagerXp(villager.getVillagerXp());
                        }
                    }
                }
            }
            return EventResult.pass();
        });
    }
}
