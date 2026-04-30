package zabi.minecraft.extraalchemy.client.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreens;
import zabi.minecraft.extraalchemy.screen.ModScreenHandlerTypes;
import zabi.minecraft.extraalchemy.screen.potion_bag.PotionBagScreen;

public class ModScreens {
	
	public static void init() {
		HandledScreens.register(ModScreenHandlerTypes.POTION_BAG, (handler, inventory, title) -> new PotionBagScreen(title, handler, inventory));
	}

}
