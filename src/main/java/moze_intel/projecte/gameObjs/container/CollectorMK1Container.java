package moze_intel.projecte.gameObjs.container;

import moze_intel.projecte.gameObjs.container.slots.SlotPredicates;
import moze_intel.projecte.gameObjs.container.slots.ValidatedSlot;
import moze_intel.projecte.utils.PELogger;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.container.slots.collector.SlotCollectorLock;
import moze_intel.projecte.gameObjs.tiles.CollectorMK1Tile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CollectorMK1Container extends Container
{
	final CollectorMK1Tile tile;
	public int sunLevel = 0;
	public int emc = 0;
	public double kleinChargeProgress = 0;
	public double fuelProgress = 0;
	public int kleinEmc = 0;

	public CollectorMK1Container(InventoryPlayer invPlayer, CollectorMK1Tile collector)
	{
		this.tile = collector;
		initSlots(invPlayer);
	}

	void initSlots(InventoryPlayer invPlayer)
	{
		IItemHandler aux = tile.getAux();
		IItemHandler main = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

		//Klein Star Slot
		this.addSlotToContainer(new ValidatedSlot(aux, CollectorMK1Tile.KLEIN_SLOT, 124, 58, SlotPredicates.COLLECTOR_INV));

		int counter = main.getSlots() - 1;
		//Fuel Upgrade storage
		for (int i = 0; i <= 1; i++)
			for (int j = 0; j <= 3; j++)
				this.addSlotToContainer(new ValidatedSlot(main, counter--, 20 + i * 18, 8 + j * 18, SlotPredicates.COLLECTOR_INV));

		//Upgrade Result
		this.addSlotToContainer(new ValidatedSlot(aux, CollectorMK1Tile.UPGRADE_SLOT, 124, 13, SlotPredicates.COLLECTOR_INV));

		//Upgrade Target
		this.addSlotToContainer(new SlotCollectorLock(aux, CollectorMK1Tile.LOCK_SLOT, 153, 36));

		//Player inventory
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		//Player hotbar
		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
	}

	@Override
	public void onCraftGuiOpened(ICrafting par1ICrafting)
	{
		super.onCraftGuiOpened(par1ICrafting);
		par1ICrafting.sendProgressBarUpdate(this, 0, tile.getSunLevel());
		par1ICrafting.sendProgressBarUpdate(this, 1, (int) tile.getStoredEmc());
		par1ICrafting.sendProgressBarUpdate(this, 2, (int) (tile.getItemChargeProportion() * 8000));
		par1ICrafting.sendProgressBarUpdate(this, 3, (int) (tile.getFuelProgress() * 8000));
		par1ICrafting.sendProgressBarUpdate(this, 4, (int) (tile.getItemCharge() * 8000));
	}
	
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if (sunLevel != tile.getSunLevel())
		{
			for (ICrafting icrafting : this.crafters)
			{
				icrafting.sendProgressBarUpdate(this, 0, tile.getSunLevel());
			}

			sunLevel = tile.getSunLevel();
		}

		if (emc != ((int) tile.getStoredEmc()))
		{
			for (ICrafting icrafting : this.crafters)
			{
				icrafting.sendProgressBarUpdate(this, 1, ((int) tile.getStoredEmc()));
			}

			emc = ((int) tile.getStoredEmc());
		}

		if (kleinChargeProgress != tile.getItemChargeProportion())
		{
			for (ICrafting icrafting : this.crafters)
			{
				icrafting.sendProgressBarUpdate(this, 2, (int) (tile.getItemChargeProportion() * 8000));
			}

			kleinChargeProgress = tile.getItemChargeProportion();
		}

		if (fuelProgress != tile.getFuelProgress())
		{
			for (ICrafting icrafting : this.crafters)
			{
				icrafting.sendProgressBarUpdate(this, 3, (int) (tile.getFuelProgress() * 8000));
			}

			fuelProgress = tile.getFuelProgress();
		}

		if (kleinEmc != ((int) tile.getItemCharge()))
		{
			for (ICrafting icrafting : this.crafters)
			{
				icrafting.sendProgressBarUpdate(this, 4, (int) (tile.getItemCharge()));
			}

			kleinEmc = ((int) tile.getItemCharge());
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data)
	{
		switch (id)
		{
			case 0: sunLevel = data; break;
			case 1: emc = data; break;
			case 2: kleinChargeProgress = data / 8000.0; break;
			case 3: fuelProgress = data / 8000.0; break;
			case 4: kleinEmc = data; break;
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		Slot slot = this.getSlot(slotIndex);
		
		if (slot == null || !slot.getHasStack()) 
		{
			return null;
		}
		
		ItemStack stack = slot.getStack();
		ItemStack newStack = stack.copy();
		
		if (slotIndex <= 10)
		{
			if (!this.mergeItemStack(stack, 11, 46, false))
			{
				return null;
			}
		}
		else if (slotIndex >= 11 && slotIndex <= 46)
		{
			if (!FuelMapper.isStackFuel(stack) || FuelMapper.isStackMaxFuel(stack) || !this.mergeItemStack(stack, 1, 8, false))
			{
				return null;
			}
		}
		else
		{
			return null;
		}
		
		if (stack.stackSize == 0)
		{
			slot.putStack(null);
		}
		else
		{
			slot.onSlotChanged();
		}
		
		slot.onPickupFromSlot(player, stack);
		return newStack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
	}
}