/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.terraTech.magicMachines.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.machines.ExtendedInventoryManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.terraTech.magicMachines.events.HealContaminationEvent;
import org.terasology.workstation.process.ProcessPart;

public class HealContaminationComponent implements Component, ProcessPart {
    public int radius = 10;

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return true;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {

    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        HealContaminationEvent event = new HealContaminationEvent(radius);
        workstation.send(event);

        if (event.getResult()) {
            // remove a piece of material
            InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
            for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, workstation, "INPUT")) {
                if (item.exists()) {
                    inventoryManager.removeItem(workstation, instigator, item, true, 1);
                }
            }
        }
    }


}
