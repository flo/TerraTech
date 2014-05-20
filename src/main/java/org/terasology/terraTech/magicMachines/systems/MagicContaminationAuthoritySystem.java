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
package org.terasology.terraTech.magicMachines.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.terraTech.magicMachines.components.ContaminatedBlockComponent;
import org.terasology.terraTech.magicMachines.events.EmitContaminationEvent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(MagicContaminationSystem.class)
public class MagicContaminationAuthoritySystem extends BaseComponentSystem implements MagicContaminationSystem {
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;
    @In
    BlockEntityRegistry blockEntityRegistry;

    @ReceiveEvent
    public void onEmitContamination(EmitContaminationEvent event, EntityRef entity, LocationComponent location) {
        event.setResult(emitContamination(new Vector3i(location.getWorldPosition()), event.radius));

    }

    @Override
    public boolean emitContamination(Vector3i position, int radius) {

        int retries = 0;
        while (retries < 10) {
            Vector3i target = nextTarget(position, radius);
            if (contaminate(target)) {
                return true;
            }
            retries++;
        }

        return false;
    }

    @ReceiveEvent
    public void onHealContamination(EmitContaminationEvent event, EntityRef entity, LocationComponent location) {
        event.setResult(healContamination(new Vector3i(location.getWorldPosition()), event.radius));
    }


    @Override
    public boolean healContamination(Vector3i position, int radius) {
        int retries = 0;
        while (retries < 10) {
            Vector3i target = nextTarget(position, radius);
            if (decontaminate(target)) {
                return true;
            }
            retries++;
        }

        return false;
    }

    @Override
    public boolean contaminate(Vector3i position) {
        Block currentBlock = worldProvider.getBlock(position);
        if (currentBlock == BlockManager.getAir()) {
            return false;
        }

        // turn the current block into a storable form
        ContaminatedBlockComponent contaminated = new ContaminatedBlockComponent(currentBlock.getBlockFamily());
        Block newBlock = blockManager.getBlock("TerraTech:ContaminatedBlock");
        EntityRef newBlockEntity = newBlock.getEntity();

        // store this in a new block
        newBlockEntity.addComponent(contaminated);

        // place the contaminated block back in the world
        worldProvider.setBlock(position, newBlock);

        return true;
    }


    private Vector3i nextTarget(Vector3i position, int radius) {
        // select something within 20 blocks diameter
        FastRandom random = new FastRandom();
        int x = random.nextInt(2 * radius) - radius;
        int y = random.nextInt(2 * radius) - radius;
        int z = random.nextInt(2 * radius) - radius;

        return new Vector3i(position.x + x, position.y + y, position.z + z);
    }

    @Override
    public boolean decontaminate(Vector3i position) {
        EntityRef entityRef = blockEntityRegistry.getBlockEntityAt(position);
        ContaminatedBlockComponent contaminatedBlock = entityRef.getComponent(ContaminatedBlockComponent.class);
        if (contaminatedBlock == null) {
            return false;
        }
        Block originalBlock = blockManager.getBlock(contaminatedBlock.blockFamily.getURI());
        worldProvider.setBlock(position, originalBlock);
        return true;
    }

    @ReceiveEvent
    public void contaminatedBlockDestroyed(DoDestroyEvent event, EntityRef entity, ContaminatedBlockComponent contaminatedBlock, LocationComponent location) {
        decontaminate(new Vector3i(location.getWorldPosition()));
    }
}
