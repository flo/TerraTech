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

import com.google.common.collect.Maps;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.terraTech.magicMachines.components.EssenceContainerComponent;
import org.terasology.terraTech.magicMachines.systems.EssenceRegistry;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.Map;

public class DistillEssenceComponent implements Component, ProcessPart, DescribeProcess, ValidateInventoryItem {

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        EssenceRegistry essenceRegistry = CoreRegistry.get(EssenceRegistry.class);

        boolean canBeValid = false;

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            canBeValid = true;
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (essenceRegistry.getContainedEssence(item) == null) {
                return false;
            }
        }

        return canBeValid;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 1000 * getComplexity();
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        EssenceRegistry essenceRegistry = CoreRegistry.get(EssenceRegistry.class);

        EssenceContainerComponent totalEssence = new EssenceContainerComponent();
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            inventoryManager.removeItem(workstation, instigator, item, false, 1);
            totalEssence.add(essenceRegistry.getContainedEssence(item));
        }

        processEntity.addComponent(totalEssence);
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        EssenceContainerComponent totalEssence = processEntity.getComponent(EssenceContainerComponent.class);

        for (Map.Entry<Prefab, Integer> item : createEssenceToPrefabMapping(totalEssence).entrySet()) {
            EntityBuilder entityBuilder = entityManager.newBuilder(item.getKey());
            ItemComponent itemComponent = entityBuilder.getComponent(ItemComponent.class);
            itemComponent.stackCount = item.getValue().byteValue();
            EntityRef essence = entityBuilder.build();
            inventoryManager.giveItem(workstation, instigator, essence, WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"));
        }
    }


    public static Map<Prefab, Integer> createEssenceToPrefabMapping(EssenceContainerComponent essenceContainer) {
        Map<Prefab, Integer> map = Maps.newHashMap();
        if (essenceContainer.air > 0) {
            map.put(Assets.getPrefab("TerraTech:AirEssence"), essenceContainer.air);
        }
        if (essenceContainer.earth > 0) {
            map.put(Assets.getPrefab("TerraTech:EarthEssence"), essenceContainer.earth);
        }
        if (essenceContainer.water > 0) {
            map.put(Assets.getPrefab("TerraTech:WaterEssence"), essenceContainer.water);
        }
        if (essenceContainer.fire > 0) {
            map.put(Assets.getPrefab("TerraTech:FireEssence"), essenceContainer.fire);
        }
        if (essenceContainer.life > 0) {
            map.put(Assets.getPrefab("TerraTech:LifeEssence"), essenceContainer.life);
        }

        return map;
    }

    @Override
    public String getDescription() {
        return "Essence";
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
            return essenceRegistry.getContainedEssence(item) != null;
        } else if (WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT").contains(slotNo)) {
            return workstation == instigator;
        }

        return false;
    }
}
