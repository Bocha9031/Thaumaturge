package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

/** A whitelist or blacklist of ghost stacks, optionally carrying per-entry size limits. */
public record StackFilter(List<ItemStack> entries, List<Integer> limits, boolean blacklist, ItemMatch match) {
    private static final int NO_ENTRY = -1;

    public static StackFilter of(List<ItemStack> entries, boolean blacklist, ItemMatch match) {
        return new StackFilter(entries, List.of(), blacklist, match);
    }

    public boolean accepts(ItemStack candidate) {
        return !candidate.isEmpty() && (matchingEntry(candidate) == NO_ENTRY) == blacklist;
    }

    public ItemStack firstAccepted(List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (accepts(candidate)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    public InvHelper.FilterMatch firstAcceptedWithLimit(List<ItemStack> candidates) {
        for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            int entry = matchingEntry(candidate);
            if ((entry == NO_ENTRY) == blacklist) {
                return new InvHelper.FilterMatch(
                        candidate, blacklist || entry >= limits.size() ? 0 : limits.get(entry));
            }
        }
        return new InvHelper.FilterMatch(ItemStack.EMPTY, 0);
    }

    public ItemStack firstAccepted(@Nullable IItemHandler handler, boolean leaveOne) {
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack content = handler.getStackInSlot(slot);
            if (content.isEmpty() || !accepts(content)) {
                continue;
            }
            if (leaveOne && InvHelper.count(handler, content, match) < 2) {
                continue;
            }
            return InvHelper.capped(content, content.getMaxStackSize());
        }
        return ItemStack.EMPTY;
    }

    private int matchingEntry(ItemStack candidate) {
        for (int index = 0; index < entries.size(); index++) {
            ItemStack entry = entries.get(index);
            if (!entry.isEmpty() && match.test(entry, candidate)) {
                return index;
            }
        }
        return NO_ENTRY;
    }
}
