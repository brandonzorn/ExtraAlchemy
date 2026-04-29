package zabi.minecraft.extraalchemy.crafting;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import zabi.minecraft.extraalchemy.config.ModConfig;
import zabi.minecraft.extraalchemy.items.ModItems;
import zabi.minecraft.extraalchemy.utils.Log;

public class AlternativePotionRingRecipe extends SpecialCraftingRecipe {

	private final int cost;
	private final int length;
	private final int renew;
	private final int level;
	private final StatusEffect effect;

	public AlternativePotionRingRecipe(Identifier id, int cost, int length, int level, int renew, StatusEffect potion) {
		super(id, CraftingRecipeCategory.EQUIPMENT);
		this.cost = cost;
		this.length = length;
		this.effect = potion;
		this.renew = renew;
		this.level = level;
	}

	@Override
	public boolean matches(RecipeInputInventory inv, World world) {
		if (!ModConfig.INSTANCE.enableRings) { //Globally disabled and specifically disabled
			return false;
		}
		
		boolean foundEffect = false;
		boolean foundRing = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack is = inv.getStack(i); 
			Item s = is.getItem();
			if (s.equals(Items.POTION)) {
				if (foundEffect || !doesPotionMatch(PotionUtil.getPotionEffects(is))) {
					return false;
				} else {
					foundEffect = true;
				}
			} else if (s.equals(ModItems.EMPTY_RING)) {
				if (foundRing) {
					return false;
				} else {
					foundRing = true;
				}
			} else if (!s.equals(Items.AIR)) {
				return false;
			}
		}
		return foundRing && foundEffect;
	}

	@Override
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2) {
		ItemStack result = new ItemStack(ModItems.POTION_RING);
		PotionUtil.setCustomPotionEffects(result, Collections.singleton(new StatusEffectInstance(effect)));
		NbtCompound nbt = result.getOrCreateNbt();
		nbt.putInt("cost", cost);
		nbt.putInt("length", length);
		nbt.putInt("renew", renew);
		nbt.putInt("level", level);
		nbt.putBoolean("disabled", true);
		return result;
	}
	
	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return craft(null, registryManager);
	}
	
	@Override
	public boolean fits(int width, int height) {
		return width > 1 || height > 1;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return CraftingRecipes.RING_CRAFTING_SERIALIZER;
	}
	
	private boolean doesPotionMatch(List<StatusEffectInstance> stack) {
		if (stack.size() != 1) {
			return false;
		}
		StatusEffectInstance stackInstance = stack.get(0);
		return stackInstance.getEffectType().equals(effect) && stackInstance.getAmplifier() == level;
	}
	
	public static class Serializer implements RecipeSerializer<AlternativePotionRingRecipe> {

		@Override
		public AlternativePotionRingRecipe read(Identifier id, JsonObject json) {
			int cost = json.getAsJsonPrimitive("cost").getAsInt();
			int length = json.getAsJsonPrimitive("length").getAsInt();
			int level = json.getAsJsonPrimitive("level").getAsInt();
			int renewTime = json.has("renew") ? json.get("renew").getAsInt() : 1;

			String effectName = json.getAsJsonPrimitive("potion").getAsString();

			Identifier effectId;
			try {
				effectId = new Identifier(effectName);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid effect id in recipe " + id + ": " + effectName, e);
			}

			StatusEffect effect = Registries.STATUS_EFFECT.get(effectId);
			if (effect == null) {
				throw new IllegalArgumentException("Unknown effect in recipe " + id + ": " + effectName);
			}
			if (effect.isInstant()) {
				Log.w("The ring recipe %s has an instant effect associated with %s, this functionality is meant for long lasting effects.", id, effectName);
			}

			return new AlternativePotionRingRecipe(id, cost, length, level, renewTime, effect);
		}

		@Override
		public AlternativePotionRingRecipe read(Identifier id, PacketByteBuf buf) {
			int cost = buf.readInt();
			int length = buf.readInt();
			int renew = buf.readInt();
			int level = buf.readInt();

			Identifier effectId = buf.readIdentifier();
			StatusEffect potion = Registries.STATUS_EFFECT.get(effectId);
			if (potion == null) {
				throw new IllegalStateException("Unknown status effect: " + effectId);
			}
			return new AlternativePotionRingRecipe(id, cost, length, level, renew, potion);
		}

		@Override
		public void write(PacketByteBuf buf, AlternativePotionRingRecipe recipe) {
			buf.writeInt(recipe.cost);
			buf.writeInt(recipe.length);
			buf.writeInt(recipe.renew);
			buf.writeInt(recipe.level);

			Identifier effectId = Registries.STATUS_EFFECT.getId(recipe.effect);
			if (effectId == null) {
				throw new IllegalStateException("Unknown status effect: " + recipe.effect);
			}
			buf.writeIdentifier(effectId);
		}
	}
}
