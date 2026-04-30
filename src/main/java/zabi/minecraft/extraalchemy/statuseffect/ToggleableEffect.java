package zabi.minecraft.extraalchemy.statuseffect;

import net.minecraft.entity.LivingEntity;

public interface ToggleableEffect {

	boolean isActive(LivingEntity e);
	
}
