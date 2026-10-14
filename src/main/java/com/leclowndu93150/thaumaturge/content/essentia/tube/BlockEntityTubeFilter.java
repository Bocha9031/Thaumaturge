package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour.FilterTubeBehaviour;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeFilter extends BlockEntityTube implements IAspectQuery {
    private static final String ASPECT_FILTER_KEY = "AspectFilter";
    private static final int QUERY_AMOUNT = 1;
    private static final int RERENDER_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;

    private final FilterTubeBehaviour filter;

    public BlockEntityTubeFilter(BlockPos pos, BlockState state) {
        this(pos, state, new FilterTubeBehaviour());
    }

    private BlockEntityTubeFilter(BlockPos pos, BlockState state, FilterTubeBehaviour filter) {
        super(TTBlockEntities.TUBE_FILTER.get(), pos, state, filter);
        this.filter = filter;
    }

    public @Nullable ResourceKey<IAspect> aspectFilter() {
        return filter.suctionFilter();
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> aspect) {
        filter.setAspectFilter(aspect);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), RERENDER_FLAGS);
        }
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        EssentiaPull inherited = super.pull(face);
        ResourceKey<IAspect> configured = filter.suctionFilter();
        Holder<IAspect> wanted = inherited.aspect();
        if (configured == null || !inherited.isActive() || wanted != null && wanted.is(configured)) {
            return inherited;
        }
        return EssentiaPull.of(resolve(configured), inherited.strength());
    }

    @Override
    public AspectList queryAspects() {
        Holder<IAspect> resolved = resolve(filter.suctionFilter());
        return resolved == null ? AspectList.EMPTY : AspectList.of(new AspectInstance(resolved, QUERY_AMOUNT));
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (filter.suctionFilter() != null)
            TTNbt.store(output, ASPECT_FILTER_KEY, ASPECT_KEY_CODEC, registries, filter.suctionFilter());
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        ResourceKey<IAspect> previous = filter.suctionFilter();
        filter.setAspectFilter(TTNbt.read(input, ASPECT_FILTER_KEY, ASPECT_KEY_CODEC, registries)
                .orElse(null));
        Level current = level;
        if (current != null && current.isClientSide() && !Objects.equals(previous, filter.suctionFilter())) {
            current.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
