package panda.simplespawners.menus;

import org.bukkit.Material;

import java.util.List;

public class MenuInterface {

    private int rows;
    private String menuTitle;
    private Material backgroundMaterial;
    private List<MenuItem> menuItems;

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public Material getBackgroundMaterial() {
        return backgroundMaterial;
    }

    public void setBackgroundMaterial(Material backgroundMaterial) {
        this.backgroundMaterial = backgroundMaterial;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
