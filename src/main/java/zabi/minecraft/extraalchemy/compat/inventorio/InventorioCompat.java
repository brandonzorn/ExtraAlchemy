package zabi.minecraft.extraalchemy.compat.inventorio;

import de.rubixdev.inventorio.api.InventorioAPI;

import zabi.minecraft.extraalchemy.items.ModItems;
import zabi.minecraft.extraalchemy.utils.LibMod;

public class InventorioCompat {

    public static void init() {

        InventorioAPI.registerInventoryTickHandler(LibMod.id("potion_ring_ticker"), (invAddon, section, stack, index) -> {
			var player = invAddon.getPlayer();
			var world = player.getEntityWorld();

			if (world.isClient) return;
			if (stack.getItem() != ModItems.POTION_RING) return;

			ModItems.POTION_RING.inventoryTick(stack, world, player, index, false);

            if (!invAddon.getPlayer().getEntityWorld().isClient && stack.getItem().equals(ModItems.POTION_RING)) {
                ModItems.POTION_RING.inventoryTick(stack, invAddon.getPlayer().getEntityWorld(), invAddon.getPlayer(), -1, false);
            }
        });

    }

}
