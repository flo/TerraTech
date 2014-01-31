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
package org.terasology.terraTech.ironWorks.processParts;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.machines.processParts.ProcessDescriptor;
import org.terasology.machines.processParts.ProcessPart;
import org.terasology.terraTech.ironWorks.components.HeaterComponent;

public class HeatOutputComponent implements Component, ProcessPart, ProcessDescriptor {
    public int temperature;

    @Override
    public void resolve(EntityRef outputEntity) {
        boolean isNewComponent = outputEntity.hasComponent(HeaterComponent.class);

        HeaterComponent heaterComponent = new HeaterComponent();
        heaterComponent.temperature = temperature;

        if (isNewComponent) {
            outputEntity.addComponent(heaterComponent);
        } else {
            outputEntity.saveComponent(heaterComponent);
        }
    }

    @Override
    public String getDescription() {
        return temperature + " degree heat";
    }

    @Override
    public boolean validate(EntityRef entity) {
        return true;
    }

    @Override
    public boolean isOutput() {
        return true;
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
