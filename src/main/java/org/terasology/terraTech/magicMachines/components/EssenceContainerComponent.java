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
package org.terasology.terraTech.magicMachines.components;

import org.terasology.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public class EssenceContainerComponent implements Component {
    public int life;
    public int earth;
    public int fire;
    public int water;
    public int air;

    public EssenceContainerComponent() {
    }

    public EssenceContainerComponent(int life, int earth, int fire, int water, int air) {
        this.life = life;
        this.earth = earth;
        this.fire = fire;
        this.water = water;
        this.air = air;
    }

    public void add(EssenceContainerComponent essenceContainer) {
        this.life += essenceContainer.life;
        this.earth += essenceContainer.earth;
        this.fire += essenceContainer.fire;
        this.water += essenceContainer.water;
        this.air += essenceContainer.air;
    }
}
