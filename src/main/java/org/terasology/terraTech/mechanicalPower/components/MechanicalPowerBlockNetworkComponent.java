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
package org.terasology.terraTech.mechanicalPower.components;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.world.block.ForceBlockActive;

import java.util.Set;

@ForceBlockActive
public class MechanicalPowerBlockNetworkComponent implements Component {
    public Set<Side> sides = Sets.newHashSet();

    public byte getConnectionSides() {
        if(sides.size() == 0) {
            return (byte)63;
        }else {
           /* Block block = entity.getComponent(BlockComponent.class).getBlock();
            Side blockDirection = block.getDirection();

            // convert these directions to sides relative to the facing of the block
            Set<Side> sides = Sets.newHashSet();
            for(Direction direction : directions) {
                sides.add(blockDirection.getRelativeSide(direction));
            }
                  */
            return SideBitFlag.getSides(sides);
        }
    }
}
