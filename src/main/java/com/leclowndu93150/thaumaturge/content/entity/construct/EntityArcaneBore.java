package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreCore;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreHost;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreTool;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.server.TTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;

public class EntityArcaneBore extends EntityOwnedConstruct implements ArcaneBoreHost {
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.BOOLEAN);

    private static final int HEAL_INTERVAL = 50;
    private static final double MOVE_DAMPING = 5.0;
    private static final byte EVENT_DIG_START = 16;
    private static final byte EVENT_DIG_STOP = 17;
    private static final int DIG_VISUAL_GRACE_TICKS = 4;
    private static final double EJECT_DISTANCE = 0.75;
    private static final float DISMANTLE_DROP_HEIGHT = 0.5F;
    private static final double KNOCKBACK_CLAMP = 0.1;
    private static final double OWNER_NEAR_RANGE = 2.0;
    private static final double OWNER_ABOVE_RANGE = 2.0;
    private static final double BLOCK_CENTER = 0.5;

    private final ArcaneBoreCore core = new ArcaneBoreCore();

    public boolean clientDigging;
    private long clientDigStopTime;
    private boolean serverDigging;

    public EntityArcaneBore(EntityType<? extends EntityArcaneBore> type, Level level) {
        super(type, level);
        this.lookControl = new ConstructLookControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(FACING, Direction.NORTH);
        entityData.define(ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        swivelWithHead();
        regenerate(HEAL_INTERVAL, 1.0F);
        if (!followRailSignal(serverLevel) && shouldFollowBlockSignal(isOnPlainRail(serverLevel), isPassenger())) {
            followBlockBelow(serverLevel);
        }
        core.serverTick(this, serverLevel, tickCount);
    }

    private boolean followRailSignal(ServerLevel level) {
        BlockPos pos = new BlockPos(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));
        BlockState state = level.getBlockState(pos);
        if (!state.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }
        if (!state.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            return false;
        }
        setActive(!state.getValue(BlockStateProperties.POWERED));
        return true;
    }

    private boolean isOnPlainRail(ServerLevel level) {
        BlockPos pos = blockPosition();
        return BaseRailBlock.isRail(level, pos) || BaseRailBlock.isRail(level, pos.below());
    }

    private void followBlockBelow(ServerLevel level) {
        setActive(level.hasNeighborSignal(blockPosition().below()));
    }

    static boolean shouldFollowBlockSignal(boolean onRail, boolean passenger) {
        return !onRail && !passenger;
    }

    @Override
    public Level boreLevel() {
        return level();
    }

    @Override
    public BlockPos borePos() {
        return blockPosition();
    }

    @Override
    public Vec3 borePosition() {
        return position();
    }

    @Override
    public float boreEyeHeight() {
        return getEyeHeight();
    }

    @Override
    public Direction boreFacing() {
        return getFacing();
    }

    @Override
    public boolean boreActive() {
        return isActive();
    }

    @Override
    public RandomSource boreRandom() {
        return random;
    }

    @Override
    public CollisionContext boreCollisionContext() {
        return CollisionContext.of(this);
    }

    @Override
    public ItemStack boreTool() {
        return getMainHandItem();
    }

    @Override
    public void setBoreTool(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    public void hurtBoreTool() {
        getMainHandItem().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
    }

    @Override
    public void aimBore(double x, double y, double z, float yawStep, float pitchStep) {
        getLookControl().setLookAt(x, y, z, yawStep, pitchStep);
    }

    @Override
    public void setBoreDigging(boolean digging) {
        if (serverDigging == digging) {
            return;
        }
        serverDigging = digging;
        level().broadcastEntityEvent(this, digging ? EVENT_DIG_START : EVENT_DIG_STOP);
    }

    @Override
    public void playBoreSound(SoundEvent sound, float volume, float pitch) {
        playSound(sound, volume, pitch);
    }

    @Override
    public FakePlayer boreDigger(ServerLevel level) {
        return TTFakePlayer.BORE.at(level, this);
    }

    @Override
    public void dropBoreOutput(ServerLevel level, ItemStack stack) {
        Direction back = getFacing().getOpposite();
        level.addFreshEntity(new ItemEntity(
                level,
                getX() + back.getStepX() * EJECT_DISTANCE,
                getY() + 0.5,
                getZ() + back.getStepZ() * EJECT_DISTANCE,
                stack));
    }

    @Override
    public void showBoreDig(ServerLevel level, BlockPos target, int delay) {
        Effects.boreDig(level, target, this, delay);
    }

    @Override
    public float boreHealth() {
        return getHealth();
    }

    @Override
    public float boreMaxHealth() {
        return getMaxHealth();
    }

    @Override
    public boolean boreValid() {
        return isAlive();
    }

    @Override
    public Component boreDisplayName() {
        return getDisplayName();
    }

    @Override
    public void writeBoreRef(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(false);
        buf.writeVarInt(getId());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(level() instanceof ServerLevel level)) return false;
        if (source.getEntity() instanceof LivingEntity living && isOwner(living)) {
            Direction face = placementFacing(
                    blockPosition(), living.getX(), living.getZ(), living.getEyeY(), living.getDirection());
            if (face != Direction.DOWN) {
                setFacing(face);
            }
            return false;
        }
        jolt();
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!level().isClientSide()) {
            dropHeld();
        }
    }

    private void dropHeld() {
        if (!getMainHandItem().isEmpty() && level() instanceof ServerLevel serverLevel) {
            spawnAtLocation(getMainHandItem(), DISMANTLE_DROP_HEIGHT);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved() || !isOwner(player) || player.getItemInHand(hand).is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            playSound(TTSounds.ZAP.get(), 1.0F, 1.0F);
            dropHeld();
            spawnAtLocation(new ItemStack(TTItems.ARCANE_BORE.get()), DISMANTLE_DROP_HEIGHT);
            discard();
            player.swing(hand);
        } else {
            MenuArcaneBore.open(player, this);
        }
        return InteractionResult.SUCCESS;
    }

    static Direction placementFacing(
            BlockPos pos, double ownerX, double ownerZ, double ownerEyeY, Direction ownerFacing) {
        boolean near = Math.abs(ownerX - (pos.getX() + BLOCK_CENTER)) < OWNER_NEAR_RANGE
                && Math.abs(ownerZ - (pos.getZ() + BLOCK_CENTER)) < OWNER_NEAR_RANGE;
        if (near) {
            double rise = ownerEyeY - pos.getY();
            if (rise > OWNER_ABOVE_RANGE) {
                return Direction.UP;
            }
            if (rise < 0.0) {
                return Direction.DOWN;
            }
        }
        return ownerFacing.getOpposite();
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 movement = getDeltaMovement();
        if (movement.y > KNOCKBACK_CLAMP) {
            setDeltaMovement(movement.x, KNOCKBACK_CLAMP, movement.z);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, dampHorizontalMovement(type, movement, MOVE_DAMPING));
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        core.setCharge((input.contains("charge") ? input.getFloat("charge") : 0.0F));
        setFacing(Direction.values()[(input.contains("facing") ? input.getByte("facing") : (byte) 0)]);
        setActive((input.contains("active") ? input.getBoolean("active") : false));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        output.putFloat("charge", core.charge());
        output.putByte("facing", (byte) getFacing().ordinal());
        output.putBoolean("active", isActive());
    }

    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    public int getHeadRotSpeed() {
        return 10;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_DIG_START) {
            clientDigging = true;
        } else if (event == EVENT_DIG_STOP) {
            clientDigging = false;
            clientDigStopTime = level().getGameTime();
        } else {
            super.handleEntityEvent(event);
        }
    }

    public boolean clientDiggingSmoothed() {
        return clientDigging || level().getGameTime() - clientDigStopTime <= DIG_VISUAL_GRACE_TICKS;
    }

    public boolean validInventory() {
        return ArcaneBoreTool.valid(getMainHandItem());
    }
}
