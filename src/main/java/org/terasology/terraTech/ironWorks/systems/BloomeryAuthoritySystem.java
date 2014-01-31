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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.machines.components.ProcessRequirementsProviderComponent;
import org.terasology.machines.components.ProcessingMachineComponent;
import org.terasology.terraTech.ironWorks.components.HeatedComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class BloomeryAuthoritySystem implements ComponentSystem {
    static final int ironMeltingPoint = 1500;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    /// Adds and removes the smelting requirment at specific temperatures
    @ReceiveEvent
    public void onHeatedChanged(OnChangedComponent event, EntityRef entity, HeatedComponent heatedComponent, ProcessingMachineComponent processingMachineComponent) {
        EntityRef inputEntity = processingMachineComponent.inputEntity;
        ProcessRequirementsProviderComponent processRequirementsProviderComponent = inputEntity.getComponent(ProcessRequirementsProviderComponent.class);
        if (processRequirementsProviderComponent != null) {
            if (heatedComponent.temperature >= 100 && !processRequirementsProviderComponent.requirements.contains("Furnace")) {
                // add smelting requirement
                processRequirementsProviderComponent.requirements.add("StandardSmeltingTemperature");
                inputEntity.saveComponent(processRequirementsProviderComponent);
            } else if (heatedComponent.temperature < 100 && processRequirementsProviderComponent.requirements.contains("Furnace")) {
                // remove Furnace
                processRequirementsProviderComponent.requirements.remove("StandardSmeltingTemperature");
                inputEntity.saveComponent(processRequirementsProviderComponent);
            }
        }
    }
}
