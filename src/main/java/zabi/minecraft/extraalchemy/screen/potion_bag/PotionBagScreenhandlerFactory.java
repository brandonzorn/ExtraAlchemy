package zabi.minecraft.extraalchemy.screen.potion_bag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class PotionBagScreenhandlerFactory implements NamedScreenHandlerFactory {
	
	private final ItemStack bagStack;
	private final Hand hand;
	
	public PotionBagScreenhandlerFactory(ItemStack bag, Hand openingHand) {
		hand = openingHand;
		bagStack = bag;
	}

	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new PotionBagScreenHandler(syncId, inv, player, hand);
	}

	@Override
	public Text getDisplayName() {
		return bagStack.getName();
	}

}
