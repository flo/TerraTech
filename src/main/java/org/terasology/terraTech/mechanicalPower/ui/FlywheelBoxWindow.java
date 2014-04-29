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
package org.terasology.terraTech.mechanicalPower.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.machines.ui.DefaultMachineWindow;
import org.terasology.machines.ui.VerticalProgressBar;
import org.terasology.mechanicalPower.components.MechanicalPowerConsumerComponent;

public class FlywheelBoxWindow extends DefaultMachineWindow {
    private VerticalProgressBar powerMeter;

    @Override
    public void initializeWorkstation(EntityRef entity) {
        super.initializeWorkstation(entity);

        powerMeter = find("powerMeter", VerticalProgressBar.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (station.exists()) {
            MechanicalPowerConsumerComponent consumer = station.getComponent(MechanicalPowerConsumerComponent.class);
            if (consumer != null) {
                float value = consumer.currentStoredPower / consumer.maximumStoredPower;
                powerMeter.setValue(value);
            }
        }
    }
}
