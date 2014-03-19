/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.terraTech.gameType.systems;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.machines.ExtendedInventoryManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem
public class StarterPackAuthoritySystem extends BaseComponentSystem {
    @In
    BlockManager blockManager;
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Gives items to get started with TerraTech", runOnServer = true)
    public String terraTechStarterPack(EntityRef client) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        EntityRef player = client.getComponent(ClientComponent.class).character;

        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("Windmill"), 5));
        inventoryManager.giveItem(player, EntityRef.NULL, ExtendedInventoryManager.createItem(entityManager, "TerraTech:WindmillSail", 5));

        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("Axle"), 32));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("Engine"), 5));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GearBox"), 5));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("FlywheelBox"), 5));

        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("EnduringlyHotFurnace"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("Bloomery"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("FireboxHeater"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("Anvil"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("IronOre"), 10));
        inventoryManager.giveItem(player, EntityRef.NULL, ExtendedInventoryManager.createItem(entityManager, "TerraTech:Coal", 32));
        inventoryManager.giveItem(player, EntityRef.NULL, ExtendedInventoryManager.createItem(entityManager, "TerraTech:Hammer", 1));

        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("ConveyorBelt"), 16));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("ConveyorTube"), 16));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("ItemExtractor"), 16));


        return "You received the TerraTech starter pack";

    }

    @Command(shortDescription = "Resets your inventory and gives items to get started with TerraTech", runOnServer = true)
    public String terraTechDemoPack(EntityRef client) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        EntityRef player = client.getComponent(ClientComponent.class).character;

        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, player)) {
            player.send(new RemoveItemAction(EntityRef.NULL, item, true));
        }

        return terraTechStarterPack(client);
    }

}
