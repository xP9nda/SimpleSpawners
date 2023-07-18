package panda.simplespawners.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.interfaces.core.click.ClickHandler;
import org.incendo.interfaces.paper.PlayerViewer;
import org.incendo.interfaces.paper.element.ItemStackElement;
import org.incendo.interfaces.paper.transform.PaperTransform;
import org.incendo.interfaces.paper.type.ChestInterface;
import org.incendo.interfaces.paper.view.PlayerView;

import java.awt.*;
import java.util.List;

public class MenuHandler {

    public ChestInterface createMenuInterface(MenuInterface menu) {
        // Create a chest interface
        ChestInterface.Builder chestInterface = new ChestInterface.Builder();

        // Set the menu title
        chestInterface.title(Component.text(menu.getMenuTitle()));

        // Set the number of rows the interface should have
        chestInterface.rows(menu.getRows());

        // Set all click events to be cancelled
        chestInterface.clickHandler(ClickHandler.cancel());

        // If a background material has been set, fill the interface with it
        Material backgroundMaterial = menu.getBackgroundMaterial();
        if (backgroundMaterial != null) {
            chestInterface.addTransform(PaperTransform.chestFill(
                    ItemStackElement.of(new ItemStack(backgroundMaterial))
            ));
        }

        // Add the menu items to the interface
        chestInterface.addTransform((pane, view) -> {
            // Get the menu items
            List<MenuItem> menuItems = menu.getMenuItems();

            // Check if items exist
            if (!menuItems.isEmpty() && menuItems != null) {
                // Add each item to the pane
                for (MenuItem item : menuItems) {
                    pane = pane.element(ItemStackElement.of(item.convertMenuItemToItemStack()), item.getSlot().x(), item.getSlot().y());
                }
            }
            return pane;
        });

        // Build the interface and return it
        ChestInterface builtInterface = chestInterface.build();
        return builtInterface;
    }

    public void showMenuInterfaceToPlayer(ChestInterface chestInterface, Player playerToShow) {
        chestInterface.open(PlayerViewer.of(playerToShow));
    }

}
