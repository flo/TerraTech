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
package org.terasology.terraTech.ironWorks.systems;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.machines.components.ProcessingMachineComponent;
import org.terasology.machines.events.ProcessingMachineChanged;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.terraTech.ironWorks.components.HeatedComponent;
import org.terasology.terraTech.ironWorks.components.HeaterComponent;
import org.terasology.world.BlockEntityRegistry;

/**
 * Created by Josharias on 1/23/14.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HeaterAuthoritySystem implements UpdateSubscriberSystem {
    static final long UPDATE_INTERVAL = 500;

    @In
    BlockEntityRegistry blockEntityRegistry;
    @In
    Time time;
    @In
    EntityManager entityManager;

    long nextUpdateTime;

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (nextUpdateTime <= currentTime) {
            nextUpdateTime = currentTime + UPDATE_INTERVAL;

            for (EntityRef entity : entityManager.getEntitiesWith(HeaterComponent.class, LocationComponent.class)) {
                HeaterComponent heaterComponent = entity.getComponent(HeaterComponent.class);
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);

                // distribute the heat to the defined direction
                EntityRef targetEntity = blockEntityRegistry.getEntityAt(heaterComponent.heatDirection.getAdjacentPos(new Vector3i(locationComponent.getWorldPosition())));
                HeatedComponent heatedComponent = targetEntity.getComponent(HeatedComponent.class);
                if (heatedComponent != null) {
                    heatedComponent.temperature += (heaterComponent.temperature - heatedComponent.temperature) * heatedComponent.temperatureAbsorptionRate;
                    targetEntity.saveComponent(heatedComponent);
                }
            }

            for (EntityRef entity : entityManager.getEntitiesWith(HeatedComponent.class, LocationComponent.class)) {
                HeatedComponent heated = entity.getComponent(HeatedComponent.class);
                if (heated.temperature > 0) {
                    heated.temperature -= heated.temperatureLossPerSecond * (UPDATE_INTERVAL / 1000f);
                    if (heated.temperature < 0) {
                        heated.temperature = 0;
                    }
                    entity.saveComponent(heated);
                }
            }
        }
    }

    @ReceiveEvent(components = {HeatedComponent.class, ProcessingMachineComponent.class})
    public void onHeatedChanged(OnChangedComponent event, EntityRef processingMachine) {
        processingMachine.send(new ProcessingMachineChanged());
    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

}
