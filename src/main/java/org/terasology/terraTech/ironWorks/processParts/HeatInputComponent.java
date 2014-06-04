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
import org.terasology.terraTech.ironWorks.components.HeatedComponent;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.ProcessPartDescription;

public class HeatInputComponent implements Component, ProcessPart, DescribeProcess {
    public float temperature;

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        HeatedComponent heatedComponent = workstation.getComponent(HeatedComponent.class);
        if (heatedComponent != null && heatedComponent.temperature >= temperature) {
            return true;
        }
        return false;
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

    }

    @Override
    public ProcessPartDescription getOutputDescription() {
        return null;
    }

    @Override
    public ProcessPartDescription getInputDescription() {
        return new ProcessPartDescription((int) temperature + " degrees");
    }

    @Override
    public int getComplexity() {
        return 0;
    }
}
