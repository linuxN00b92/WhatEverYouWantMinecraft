package emd24.minecraftrpgmod.spells;

import emd24.minecraftrpgmod.ExtendedPlayerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class ItemManaHeal extends Item{

	public ItemManaHeal(int id) {
		super(id);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		if (!par3EntityPlayer.capabilities.isCreativeMode)
		{
			--par1ItemStack.stackSize;
		}
		else{
			ExtendedPlayerData data = ExtendedPlayerData.get(par3EntityPlayer);
			data.setCurrMana(data.getMaxMana());
			if(!par2World.isRemote){
				par3EntityPlayer.sendChatToPlayer((new ChatMessageComponent()).addText("Mana Recovered"));
			}
		}
		return par1ItemStack;

	}

}
