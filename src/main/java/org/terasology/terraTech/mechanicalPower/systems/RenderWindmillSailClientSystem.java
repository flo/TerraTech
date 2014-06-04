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
package org.terasology.terraTech.mechanicalPower.systems;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.itemRendering.components.AnimateRotationComponent;
import org.terasology.itemRendering.components.RenderItemComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.math.Direction;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.mechanicalPower.systems.MechanicalPowerClientSystem;
import org.terasology.registry.In;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.terraTech.mechanicalPower.components.RenderWindmillSailComponent;
import org.terasology.world.block.BlockComponent;

@RegisterSystem(RegisterMode.CLIENT)
public class RenderWindmillSailClientSystem extends BaseComponentSystem {

    @In
    InventoryManager inventoryManager;

    @ReceiveEvent
    public void addItemRendering(InventorySlotChangedEvent event,
                                 EntityRef inventoryEntity,
                                 RenderWindmillSailComponent renderWindmillSail,
                                 BlockComponent block) {
        EntityRef oldItem = event.getOldItem();
        if (oldItem.exists() && !oldItem.getOwner().hasComponent(RenderWindmillSailComponent.class)) {
            // ensure that rendered items get reset
            oldItem.removeComponent(RenderItemComponent.class);
        }

        EntityRef newItem = event.getNewItem();
        if (newItem.exists()) {

            MeshComponent mesh = new MeshComponent();
            mesh.material = Assets.getMaterial("TerraTech:WindmillSail");
            mesh.mesh = Assets.getMesh("TerraTech:WindmillSail");

            if (newItem.hasComponent(MeshComponent.class)) {
                newItem.saveComponent(mesh);
            } else {
                newItem.addComponent(mesh);
            }


            Side direction = block.getBlock().getDirection();
            RenderItemComponent renderItem = new RenderItemComponent();
            renderItem.translate = direction.getRelativeSide(Direction.FORWARD).getVector3i().toVector3f();
            renderItem.size = 1;
            Rotation rotation = MechanicalPowerClientSystem.getRotation(direction);
            renderItem.pitch = rotation.getPitch();
            renderItem.roll = rotation.getRoll();
            renderItem.yaw = rotation.getYaw();

            if (newItem.hasComponent(RenderItemComponent.class)) {
                newItem.saveComponent(renderItem);
            } else {
                newItem.addComponent(renderItem);
            }


            AnimateRotationComponent animateRotation = new AnimateRotationComponent();
            animateRotation.roll = Roll.CLOCKWISE_90;
            animateRotation.speed = 0.1f;


            if (newItem.hasComponent(AnimateRotationComponent.class)) {
                newItem.saveComponent(animateRotation);
            } else {
                newItem.addComponent(animateRotation);
            }

        }
    }


}
