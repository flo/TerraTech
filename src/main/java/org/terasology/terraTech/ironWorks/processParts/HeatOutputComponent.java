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

import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.terraTech.ironWorks.components.HeaterComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;

import java.util.Set;

public class HeatOutputComponent implements Component, ProcessPart {
    public int temperature;
    public long burnTime;

    public String getDescription() {
        return temperature + " degree heat";
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation, String parameter) throws InvalidProcessException {
        Set<String> results = Sets.newHashSet();
        results.add("");
        return results;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        return burnTime;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        boolean isNewComponent = workstation.hasComponent(HeaterComponent.class);

        HeaterComponent heaterComponent = new HeaterComponent();
        heaterComponent.temperature = temperature;

        if (isNewComponent) {
            workstation.addComponent(heaterComponent);
        } else {
            workstation.saveComponent(heaterComponent);
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        workstation.removeComponent(HeaterComponent.class);
    }
}
