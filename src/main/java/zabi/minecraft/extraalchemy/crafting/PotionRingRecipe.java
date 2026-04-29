package zabi.minecraft.extraalchemy.crafting;

import com.google.gson.JsonObject;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
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

public class PotionRingRecipe extends SpecialCraftingRecipe {

	private final int cost;
	private final int length;
	private final int renew;
	private final Potion potion;

	public PotionRingRecipe(Identifier id, int cost, int length, int renew, Potion potion) {
		super(id, CraftingRecipeCategory.EQUIPMENT);
		this.cost = cost;
		this.length = length;
		this.potion = potion;
		this.renew = renew;
	}

	@Override
	public boolean matches(RecipeInputInventory inv, World world) {
		if (!ModConfig.INSTANCE.enableRings || potion.getEffects().size() != 1) { //Globally disabled and specifically disabled
			return false;
		}
		
		if (potion.getEffects().isEmpty()) return false;

		boolean foundPotion = false;
		boolean foundRing = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack is = inv.getStack(i); 
			Item s = is.getItem();
			if (s.equals(Items.POTION)) {
				if (foundPotion || !doesPotionMatch(PotionUtil.getPotion(is))) {
					return false;
				} else {
					foundPotion = true;
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
		return foundRing && foundPotion;
	}

	@Override
	public ItemStack craft(RecipeInputInventory inv, DynamicRegistryManager var2) {
		ItemStack result = new ItemStack(ModItems.POTION_RING);
		PotionUtil.setPotion(result, potion);
		NbtCompound nbt = result.getOrCreateNbt();
		nbt.putInt("cost", cost);
		nbt.putInt("length", length);
		nbt.putInt("renew", renew);
		nbt.putBoolean("disabled", true);
		return result;
	}
	
	@Override
	public ItemStack getOutput(DynamicRegistryManager regMan) {
		return craft(null, regMan);
	}
	
	@Override
	public boolean fits(int width, int height) {
		return width > 1 || height > 1;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return CraftingRecipes.RING_CRAFTING_SERIALIZER;
	}
	
	private boolean doesPotionMatch(Potion stack) {
		if (stack.getEffects().size() != 1) {
			return false;
		}
		StatusEffectInstance stackInstance = stack.getEffects().get(0);
		StatusEffectInstance recipeInstance = potion.getEffects().get(0);
		return stackInstance.getEffectType().equals(recipeInstance.getEffectType()) && stackInstance.getAmplifier() == recipeInstance.getAmplifier();
	}
	
	public static class Serializer implements RecipeSerializer<PotionRingRecipe> {

		@Override
		public PotionRingRecipe read(Identifier id, JsonObject json) {
			int cost = json.getAsJsonPrimitive("cost").getAsInt();
			int length = json.getAsJsonPrimitive("length").getAsInt();
			int renewTime = json.has("renew") ? json.get("renew").getAsInt() : 1;

			String potionName = json.getAsJsonPrimitive("potion").getAsString();

			Identifier potionId;
			try {
				potionId = new Identifier(potionName);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid potion id in recipe " + id + ": " + potionName, e);
			}

			Potion potion = Registries.POTION.get(potionId);
			if (potion.getEffects().size() > 1) {
				Log.w("The ring recipe %s has more than 1 effect associated with it, this functionality is meant for 1-effect potions.");
			}
			if (potion.getEffects().stream().allMatch(sei -> sei.getEffectType().isInstant())) {
				Log.w("The ring recipe %s has no non-instant effects associated with %s, this functionality is meant for long lasting effects.", id, potionName);
			}

            return new PotionRingRecipe(id, cost, length, renewTime, potion);
		}

		@Override
		public PotionRingRecipe read(Identifier id, PacketByteBuf buf) {
			int cost = buf.readInt();
			int length = buf.readInt();
			int renew = buf.readInt();
			String potion_name = buf.readString();
			Potion potion = Registries.POTION.get(new Identifier(potion_name));
			return new PotionRingRecipe(id, cost, length, renew, potion);
		}

		@Override
		public void write(PacketByteBuf buf, PotionRingRecipe recipe) {
			buf.writeInt(recipe.cost);
			buf.writeInt(recipe.length);
			buf.writeInt(recipe.renew);
			buf.writeString(Registries.POTION.getId(recipe.potion).toString());
		}

	}
}
