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
package org.terasology.terraTech.magic.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.machines.ExtendedInventoryManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.terraTech.magic.components.CompactEssenceOutputComponent;
import org.terasology.terraTech.magic.components.EssenceContainerComponent;
import org.terasology.terraTech.magic.systems.EssenceRegistry;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.Map;

public class CompactEssenceComponent implements Component, ProcessPart, DescribeProcess, ValidateInventoryItem {

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        EssenceRegistry essenceRegistry = CoreRegistry.get(EssenceRegistry.class);
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

        boolean canBeValid = false;

        EssenceContainerComponent essenceContainer = new EssenceContainerComponent();
        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, workstation, "REQUIREMENTS")) {
            canBeValid = true;
            EssenceContainerComponent itemEssenceContainer = essenceRegistry.getContainedEssence(item);
            if (itemEssenceContainer == null) {
                return false;
            } else {
                essenceContainer.add(itemEssenceContainer);
            }
        }


        Map<Prefab, Integer> essences = DistillEssenceComponent.createEssenceToPrefabMapping(essenceContainer);
        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, workstation, "INPUT")) {
            if (essences.keySet().contains(item.getParentPrefab())
                    && InventoryUtils.getStackCount(item) >= essences.get(item.getParentPrefab())) {
                essences.remove(item.getParentPrefab());
            }
        }

        return canBeValid && essences.size() == 0;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 1000 * getComplexity();
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        EssenceRegistry essenceRegistry = CoreRegistry.get(EssenceRegistry.class);
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);

        CompactEssenceOutputComponent compactEssenceOutput = new CompactEssenceOutputComponent();
        EssenceContainerComponent essenceContainer = new EssenceContainerComponent();
        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, workstation, "REQUIREMENTS")) {
            EssenceContainerComponent itemEssenceContainer = essenceRegistry.getContainedEssence(item);
            if (itemEssenceContainer == null) {
                return;
            } else {
                BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
                if (blockItem != null) {
                    compactEssenceOutput.blockFamilies.add(blockItem.blockFamily);
                } else {
                    compactEssenceOutput.itemPrefabs.add(item.getParentPrefab());
                }
                essenceContainer.add(itemEssenceContainer);
            }
        }

        Map<Prefab, Integer> essences = DistillEssenceComponent.createEssenceToPrefabMapping(essenceContainer);
        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, workstation, "INPUT")) {
            if (essences.containsKey(item.getParentPrefab())) {
                inventoryManager.removeItem(workstation, instigator, item, false, essences.get(item.getParentPrefab()));
            }
        }

        processEntity.addComponent(compactEssenceOutput);
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);

        CompactEssenceOutputComponent compactEssenceOutput = processEntity.getComponent(CompactEssenceOutputComponent.class);
        if (compactEssenceOutput != null) {
            for (BlockFamily blockFamily : compactEssenceOutput.blockFamilies) {
                inventoryManager.giveItem(workstation, instigator, blockItemFactory.newInstance(blockFamily), WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"));
            }
            for (Prefab itemPrefab : compactEssenceOutput.itemPrefabs) {
                inventoryManager.giveItem(workstation, instigator, entityManager.create(itemPrefab), WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"));
            }
        }

    }

    @Override
    public String getDescription() {
        return "Compact Essence";
    }

    @Override
    public int getComplexity() {
        return 0;
    }


    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT").contains(slotNo)
                || WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT").contains(slotNo);
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        EssenceRegistry essenceRegistry = CoreRegistry.get(EssenceRegistry.class);

        if (WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT").contains(slotNo)) {
            return DistillEssenceComponent.createEssenceToPrefabMapping(new EssenceContainerComponent(1, 1, 1, 1, 1)).keySet().contains(item.getParentPrefab());
        } else if (WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT").contains(slotNo)) {
            return workstation == instigator;
        }

        return false;
    }
}
