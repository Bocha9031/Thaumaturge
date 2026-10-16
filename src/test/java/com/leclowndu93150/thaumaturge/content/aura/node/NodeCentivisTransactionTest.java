package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import org.junit.jupiter.api.Test;

class NodeCentivisTransactionTest {
    @Test
    void rollbackRestoresHeldVisCreditAndAllowance() {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, 1)));
        node.aspects = AspectList.ofEntries(List.of(new AspectInstance(aspect, 2)));
        node.energized = true;
        NodeCentivis centivis = new NodeCentivis(node);
        centivis.accrue();

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(1, centivis.drain(aspect, 1, transaction));
        }

        assertEquals(2, node.aspects.amountOf(aspect));
        assertEquals(1, centivis.available(aspect));

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(1, centivis.drain(aspect, 1, transaction));
            transaction.commit();
        }

        assertEquals(1, node.aspects.amountOf(aspect));
        assertEquals(0, centivis.available(aspect));
    }

    @Test
    void spendingWholeVisPointAndAbortingRestoresItsContentsAndAllowance() {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, 100)));
        node.aspects = AspectList.ofEntries(List.of(new AspectInstance(aspect, 1)));
        node.energized = true;
        NodeCentivis centivis = new NodeCentivis(node);
        centivis.accrue();

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(100, centivis.drain(aspect, 100, transaction));
            assertEquals(0, node.aspects.amountOf(aspect));
        }

        assertEquals(1, node.aspects.amountOf(aspect));
        assertEquals(100, centivis.available(aspect));
    }
}
