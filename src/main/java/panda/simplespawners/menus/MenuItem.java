package panda.simplespawners.menus;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.incendo.interfaces.core.util.Vector2;

import java.util.List;

public class MenuItem {

    private MiniMessage miniMsg = MiniMessage.miniMessage();

    private String itemName;
    private Material material;
    private List<String> lore;
    private int stackQuantity;
    private Vector2 slot;

    public ItemStack convertMenuItemToItemStack() {
        // Create a new itemstack with the appropriate material
        ItemStack itemStack = new ItemStack(this.material);

        // Edit the itemstacks metadata
        itemStack.editMeta(meta -> {
            meta.displayName(miniMsg.deserialize(itemName));
            meta.lore(
                lore.stream().map(
                    it -> miniMsg.deserialize(it).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                ).toList()
            );
        });

        itemStack.setAmount(stackQuantity);

        return itemStack;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public int getStackQuantity() {
        return stackQuantity;
    }

    public void setStackQuantity(int stackQuantity) {
        this.stackQuantity = stackQuantity;
    }

    public Vector2 getSlot() {
        return slot;
    }

    public void setSlot(Vector2 slot) {
        this.slot = slot;
    }
}
