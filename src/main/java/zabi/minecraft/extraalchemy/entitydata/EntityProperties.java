package zabi.minecraft.extraalchemy.entitydata;

import net.minecraft.entity.LivingEntity;
import zabi.minecraft.extraalchemy.utils.DimensionalPosition;

public interface EntityProperties {
	
	DimensionalPosition getRecallPosition();
	
	void setRecallData(DimensionalPosition pos);

	void markEffectsDirty();
	
	static EntityProperties of(LivingEntity entity) {
		return (EntityProperties) entity;
	}
	
}
