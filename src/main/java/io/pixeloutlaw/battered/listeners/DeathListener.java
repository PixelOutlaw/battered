/*
 * The MIT License
 * Copyright © 2015 Pixel Outlaw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.pixeloutlaw.battered.listeners;

import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathListener implements Listener {

  private final VersionedSmartYamlConfiguration config;

  public DeathListener(VersionedSmartYamlConfiguration config) {
    this.config = config;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    event.setKeepInventory(true);
    event.getDrops().clear();
    if (config.getStringList("ignored-worlds").contains(event.getEntity().getWorld().getName())) {
      return;
    }
    final Player player = event.getEntity();

    Inventory inventory = player.getInventory();
    ItemStack[] inventoryContents = inventory.getContents().clone();
    for (int i = 0; i < inventoryContents.length; i++) {
      ItemStack itemStack = inventoryContents[i];
      if (itemStack == null || itemStack.getType() == Material.AIR) {
        continue;
      }
      if (isSoulShard(itemStack)) {
        continue;
      }
      ItemStack cloned = itemStack.clone();
      if (i <= 8 || (i >= 36 && i <= 40)) {
        if (itemStack.getAmount() == 1) {
          continue;
        }
        int dropAmount = (int) Math.floor(cloned.getAmount() * 0.75);
        int keepAmount = cloned.getAmount() - dropAmount;

        cloned.setAmount(keepAmount);
        inventoryContents[i] = cloned.clone();

        cloned.setAmount(dropAmount);
        event.getDrops().add(cloned);
      } else {
        event.getDrops().add(cloned);
        inventoryContents[i] = null;
      }
    }
    inventory.setContents(inventoryContents);
    player.updateInventory();
  }

  private boolean isSoulShard(ItemStack itemStack) {
    if (itemStack.getType() != Material.QUARTZ) {
      return false;
    }
    if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
      return false;
    }
    return itemStack.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Soul Shard");
  }
}
