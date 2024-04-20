package tocraft.walkers.skills.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tocraft.walkers.Walkers;
import tocraft.walkers.skills.ShapeSkill;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ReinforcementsSkill<E extends LivingEntity> extends ShapeSkill<E> {
    public static final ResourceLocation ID = Walkers.id("reinforcements");
    public static final Codec<ReinforcementsSkill<?>> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.optionalFieldOf("range", 32).forGetter(o -> o.range),
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("reinforcements", new ArrayList<>()).forGetter(o -> o.reinforcementTypes.stream().map(BuiltInRegistries.ENTITY_TYPE::getKey).toList()),
            Codec.list(ResourceLocation.CODEC).optionalFieldOf("reinforcement_tags", new ArrayList<>()).forGetter(o -> o.reinforcementTags.stream().map(TagKey::location).toList())
    ).apply(instance, instance.stable((range, reinforcementsLocations, reinforcementTagsLocations) -> {
        List<EntityType<?>> reinforcements = new ArrayList<>();
        List<TagKey<EntityType<?>>> reinforcementTags = new ArrayList<>();
        for (ResourceLocation resourceLocation : reinforcementsLocations) {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(resourceLocation)) {
                reinforcements.add(BuiltInRegistries.ENTITY_TYPE.get(resourceLocation));
            }
        }
        for (ResourceLocation resourceLocation : reinforcementTagsLocations) {
            reinforcementTags.add(TagKey.create(Registries.ENTITY_TYPE, resourceLocation));
        }
        return new ReinforcementsSkill<>(range, reinforcements, reinforcementTags);
    })));
    private final int range;
    private final List<EntityType<?>> reinforcementTypes;
    private final List<TagKey<EntityType<?>>> reinforcementTags;


    public ReinforcementsSkill() {
        this(32);
    }

    public ReinforcementsSkill(int range) {
        this(range, new ArrayList<>());
    }

    public ReinforcementsSkill(List<EntityType<?>> reinforcementTypes) {
        this(32, reinforcementTypes);
    }

    public ReinforcementsSkill(int range, @NotNull List<EntityType<?>> reinforcementTypes) {
        this(range, reinforcementTypes, new ArrayList<>());
    }

    public ReinforcementsSkill(int range, @NotNull List<EntityType<?>> reinforcementTypes, @NotNull List<TagKey<EntityType<?>>> reinforcementTags) {
        this.range = range;
        this.reinforcementTypes = reinforcementTypes;
        this.reinforcementTags = reinforcementTags;
    }

    public boolean hasReinforcements() {
        return !reinforcementTypes.isEmpty() || !reinforcementTags.isEmpty();
    }

    public boolean isReinforcement(Entity entity) {
        if (reinforcementTypes.contains(entity.getType())) return true;
        for (TagKey<EntityType<?>> reinforcementTag : reinforcementTags) {
            if (entity.getType().is(reinforcementTag)) return true;
        }
        return false;
    }

    public int getRange() {
        return range;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Codec<? extends ShapeSkill<?>> codec() {
        return CODEC;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable TextureAtlasSprite getIcon() {
        BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(Items.IRON_SWORD);
        if (itemModel != null) {
            return itemModel.getParticleIcon();
        } else {
            return super.getIcon();
        }
    }
}
