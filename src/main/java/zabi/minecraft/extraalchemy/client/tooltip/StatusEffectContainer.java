package zabi.minecraft.extraalchemy.client.tooltip;

import java.util.List;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;

public interface StatusEffectContainer {
	
	StatusEffectContainer DEFAULT_CONTAINER = PotionUtil::getPotionEffects;
	
	List<StatusEffectInstance> getContainedEffects(ItemStack stack);
	
	default boolean hasEffects(ItemStack stack) {
		return !this.getContainedEffects(stack).isEmpty();
	}
	
	static StatusEffectContainer of(ItemStack stack) {
		if (stack.getItem() instanceof StatusEffectContainer sec) return sec;
		return DEFAULT_CONTAINER;
	}

}
