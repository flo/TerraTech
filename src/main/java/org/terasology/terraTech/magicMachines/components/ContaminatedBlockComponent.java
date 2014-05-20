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
import org.terasology.world.block.ForceBlockActive;
import org.terasology.world.block.family.BlockFamily;

@ForceBlockActive
public class ContaminatedBlockComponent implements Component {
    public BlockFamily blockFamily;

    public ContaminatedBlockComponent(BlockFamily blockFamily) {
        this.blockFamily = blockFamily;
    }
}
