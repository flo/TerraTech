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
package org.terasology.terraTech.ironWorks.systems;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.itemTransport.events.ConveyorItemStuckEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.machines.ExtendedInventoryManager;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.terraTech.ironWorks.components.RisingSmokeComponent;
import org.terasology.terraTech.ironWorks.components.SmokeComponent;
import org.terasology.terraTech.ironWorks.components.SmokeProducerComponent;
import org.terasology.terraTech.ironWorks.components.VentComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SmokeAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    static final long UPDATE_INTERVAL = 500;

    @In
    BlockEntityRegistry blockEntityRegistry;
    @In
    Time time;
    @In
    EntityManager entityManager;
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;
    @In
    InventoryManager inventoryManager;


    long nextUpdateTime;
    Map<Long, Long> cachedTimes = Maps.newHashMap();
    List<Side> prioritizedSides = Lists.newLinkedList();
    Random random = new FastRandom();

    public SmokeAuthoritySystem() {
        prioritizedSides.add(Side.TOP);
        prioritizedSides.add(Side.BACK);
        prioritizedSides.add(Side.FRONT);
        prioritizedSides.add(Side.LEFT);
        prioritizedSides.add(Side.RIGHT);
    }

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (nextUpdateTime <= currentTime) {
            nextUpdateTime = currentTime + UPDATE_INTERVAL;

            Set<Vector3i> relativePositions = Sets.newLinkedHashSet();
            relativePositions.add(new Vector3i(0, 1, 0));
            relativePositions.add(new Vector3i(1, 0, 0));
            relativePositions.add(new Vector3i(0, 0, 1));
            relativePositions.add(new Vector3i(-1, 0, 0));
            relativePositions.add(new Vector3i(0, 0, -1));
            relativePositions.add(new Vector3i(1, 0, 1));
            relativePositions.add(new Vector3i(-1, 0, -1));
            relativePositions.add(new Vector3i(1, 0, -1));
            relativePositions.add(new Vector3i(-1, 0, 1));


            for (EntityRef entity : entityManager.getEntitiesWith(SmokeComponent.class, RisingSmokeComponent.class, BlockComponent.class)) {
                BlockComponent block = entity.getComponent(BlockComponent.class);

                boolean movedBlock = false;

                for (Vector3i nextRelativePosition : relativePositions) {
                    Vector3i nextTarget = new Vector3i(block.getPosition());
                    if (nextRelativePosition.y == 0) {
                        nextTarget.add(0, 1, 0);
                    }
                    nextTarget.add(nextRelativePosition);

                    // skip to the edge of a smokey region
                    while (blockEntityRegistry.getBlockEntityAt(nextTarget).hasComponent(SmokeComponent.class)) {
                        nextTarget.add(nextRelativePosition);
                    }

                    if (worldProvider.getBlock(nextTarget) == BlockManager.getAir() || blockEntityRegistry.getBlockEntityAt(nextTarget).hasComponent(VentComponent.class)) {
                        moveToPosition(block.getPosition(), nextTarget);
                        movedBlock = true;
                        break;
                    }
                }

                if (!movedBlock) {
                    entity.removeComponent(RisingSmokeComponent.class);
                }
            }

            for (EntityRef entity : entityManager.getEntitiesWith(SmokeProducerComponent.class, BlockComponent.class)) {
                BlockComponent block = entity.getComponent(BlockComponent.class);
                SmokeProducerComponent smokeProducer = entity.getComponent(SmokeProducerComponent.class);

                if (!cachedTimes.containsKey(smokeProducer.timeBetweenSmoke)
                        || cachedTimes.get(smokeProducer.timeBetweenSmoke) < currentTime) {

                    // choose a random side
                    Vector3i targetPosition = prioritizedSides.get(random.nextInt(prioritizedSides.size())).getAdjacentPos(block.getPosition());

                    // check to see if there is a vent attached
                    for (Side side : prioritizedSides) {
                        Vector3i potentialPosition = side.getAdjacentPos(block.getPosition());

                        if (blockEntityRegistry.getBlockEntityAt(potentialPosition).hasComponent(VentComponent.class)) {
                            // prefer to output to a vent
                            targetPosition = potentialPosition;
                            break;
                        }
                    }

                    if (!giveSmoke(targetPosition, blockManager.getBlock("TerraTech:Smoke")) && !entity.hasComponent(SmokeComponent.class)) {
                        // add smoke damage to this entity
                        entity.addComponent(new SmokeComponent());
                    }

                    cachedTimes.put(smokeProducer.timeBetweenSmoke, currentTime + smokeProducer.timeBetweenSmoke);
                }
            }
        }
    }

    private boolean giveSmoke(Vector3i targetPosition, Block smokeBlock) {
        EntityRef targetEntity = blockEntityRegistry.getBlockEntityAt(targetPosition);
        VentComponent vent = targetEntity.getComponent(VentComponent.class);
        if (vent == null) {
            if (worldProvider.getBlock(targetPosition) == BlockManager.getAir()) {
                // create a smoke block if it should not dissipate
                Vector3i testPosition = new Vector3i(targetPosition);
                for (int i = 0; i < 15; i++) {
                    testPosition.add(0, 1, 0);
                    if (worldProvider.getBlock(testPosition) != BlockManager.getAir()) {
                        // this target position does not have 15 blocks of air above it
                        worldProvider.setBlock(targetPosition, smokeBlock);
                        return true;
                    }
                }

                // this block should dissipate
                worldProvider.setBlock(targetPosition, BlockManager.getAir());
                // create particles
                EntityRef particleEntity = entityManager.create("TerraTech:SmokeParticle");
                LocationComponent location = new LocationComponent();
                location.setWorldPosition(targetPosition.toVector3f());
                particleEntity.addComponent(location);
                return true;
            } else {
                return false;
            }
        } else {
            // create a pickup and put into the vent's inventory
            BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
            EntityRef item = blockItemFactory.newInstance(smokeBlock.getBlockFamily(), 1);
            inventoryManager.giveItem(targetEntity, EntityRef.NULL, item);
            return true;
        }
    }

    private void moveToPosition(Vector3i position, Vector3i targetPosition) {
        Block smokeBlock = worldProvider.getBlock(position);
        worldProvider.setBlock(position, BlockManager.getAir());
        giveSmoke(targetPosition, smokeBlock);
    }

    @ReceiveEvent
    public void ventSmokeFromChimney(ConveyorItemStuckEvent event, EntityRef entity, VentComponent vent) {
        for (EntityRef item : ExtendedInventoryManager.iterateItems(inventoryManager, entity)) {
            BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
            giveSmoke(event.getTargetPosition(), blockItem.blockFamily.getArchetypeBlock());

            inventoryManager.removeItem(entity, entity, item, true);
        }
    }
}
