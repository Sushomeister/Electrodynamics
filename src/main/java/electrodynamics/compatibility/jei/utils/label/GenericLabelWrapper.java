package electrodynamics.compatibility.jei.utils.label;

import electrodynamics.common.recipe.ElectrodynamicsRecipe;
import electrodynamics.compatibility.jei.recipecategories.ElectrodynamicsRecipeCategory;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class GenericLabelWrapper {

	protected static final String POWER = "power";

	private int COLOR;
	private int Y_POS;
	private int X_POS;
	private String NAME;

	public GenericLabelWrapper(int color, int yPos, int endXPos, String name) {
		COLOR = color;
		Y_POS = yPos;
		X_POS = endXPos;
		NAME = name;
	}

	public int getColor() {
		return COLOR;
	}

	public int getYPos() {
		return Y_POS;
	}

	public int getXPos() {
		return X_POS;
	}

	public String getLocation() {
		return "jei.guilabel." + NAME;
	}

	public BaseComponent getComponent(ElectrodynamicsRecipeCategory<?> category, ElectrodynamicsRecipe recipe) {
		return new TranslatableComponent(getLocation());
	}
}
