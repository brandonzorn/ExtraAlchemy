package zabi.minecraft.extraalchemy.client.tooltip;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

public record PotionTooltipData(ItemStack stack) implements TooltipData { }
