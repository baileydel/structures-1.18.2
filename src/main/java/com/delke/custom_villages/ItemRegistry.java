package com.delke.custom_villages;


import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "structuremod");

    public static final ModCreativeTab instance = new ModCreativeTab(CreativeModeTab.TABS.length, "firstmod");
    public static final RegistryObject<Item> TELEPORT_STAFF = ITEMS.register("teleport_staff", () -> new StructureWandItem(new Item.Properties().tab(instance)));

    static class ModCreativeTab extends CreativeModeTab {
        private ModCreativeTab(int index, String label) {
            super(index, label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(TELEPORT_STAFF.get());
        }
    }
}
