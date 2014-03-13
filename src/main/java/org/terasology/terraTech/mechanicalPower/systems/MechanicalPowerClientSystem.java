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
package org.terasology.terraTech.mechanicalPower.systems;

import org.terasology.blockNetwork.Network;
import org.terasology.blockNetwork.NetworkNode;
import org.terasology.blockNetwork.NetworkTopologyListener;
import org.terasology.blockNetwork.SidedLocationNetworkNode;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.itemRendering.components.AnimateRotationComponent;
import org.terasology.itemRendering.components.RenderItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.Yaw;
import org.terasology.registry.In;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerProducerComponent;
import org.terasology.terraTech.mechanicalPower.components.RotatingAxleComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.CLIENT)
public class MechanicalPowerClientSystem extends BaseComponentSystem implements NetworkTopologyListener {
    @In
    BlockEntityRegistry blockEntityRegistry;
    @In
    EntityManager entityManager;
    @In
    MechanicalPowerBlockNetwork mechanicalPowerBlockNetwork;

    @Override
    public void initialise() {
        mechanicalPowerBlockNetwork.addTopologyListener(this);
    }

    @ReceiveEvent
    public void createRenderedAxle(OnAddedComponent event, EntityRef entity, RotatingAxleComponent rotatingAxle, LocationComponent location, BlockComponent block) {
        EntityBuilder renderedEntityBuilder = entityManager.newBuilder("RotatingAxle");
        renderedEntityBuilder.setOwner(entity);
        // set the look of the rendered entity
        BlockItemComponent blockItem = renderedEntityBuilder.getComponent(BlockItemComponent.class);
        blockItem.blockFamily = rotatingAxle.renderedBlockFamily;
        renderedEntityBuilder.saveComponent(blockItem);

        // rotate the rendered entity to match the block
        RenderItemComponent renderItemTransform = renderedEntityBuilder.getComponent(RenderItemComponent.class);
        Side direction = block.getBlock().getDirection();
        Rotation rotation = getRotation(direction);
        renderItemTransform.pitch = rotation.getPitch();
        renderItemTransform.roll = rotation.getRoll();
        renderItemTransform.yaw = rotation.getYaw();
        renderedEntityBuilder.saveComponent(renderItemTransform);

        rotatingAxle.renderedEntity = renderedEntityBuilder.build();
        entity.saveComponent(rotatingAxle);


        MechanicalPowerNetwork network = mechanicalPowerBlockNetwork.getNetwork(block.getPosition());
        updateAxlesInNetwork(network);
    }


    public Rotation getRotation(Side side) {
        Pitch pitch = Pitch.NONE;
        Yaw yaw = Yaw.NONE;

        if (side == Side.BACK) {
            pitch = Pitch.CLOCKWISE_180;
        } else if (side == Side.TOP) {
            pitch = Pitch.CLOCKWISE_90;
        } else if (side == Side.BOTTOM) {
            pitch = pitch.CLOCKWISE_270;
        } else if (side == Side.LEFT) {
            yaw = Yaw.CLOCKWISE_90;
        } else if (side == Side.RIGHT) {
            yaw = Yaw.CLOCKWISE_270;
        }

        return Rotation.rotate(yaw, pitch);
    }


    @ReceiveEvent
    public void updateAxlesInNetwork(OnChangedComponent event, EntityRef entity, MechanicalPowerProducerComponent powerProducer, BlockComponent block) {
        MechanicalPowerNetwork network = mechanicalPowerBlockNetwork.getNetwork(block.getPosition());
        updateAxlesInNetwork(network);
    }

    private void updateAxlesInNetwork(MechanicalPowerNetwork network) {
        if (network != null) {
            float speed = network.totalPower / (network.totalConsumers + 1);
            for (SidedLocationNetworkNode node : mechanicalPowerBlockNetwork.getNetworkNodes(network)) {
                EntityRef nodeEntity = blockEntityRegistry.getBlockEntityAt(node.location);

                RotatingAxleComponent rotatingAxle = nodeEntity.getComponent(RotatingAxleComponent.class);
                if (rotatingAxle != null) {
                    if (network.totalPower > 0) {
                        // ensure all axle rotation is turned on
                        turnAxleOn(rotatingAxle.renderedEntity, speed);
                    } else {
                        // ensure all axle rotation is turned off
                        turnAxleOff(rotatingAxle.renderedEntity);
                    }
                }
            }
        }
    }

    private void turnAxleOn(EntityRef renderedEntity, float speed) {
        AnimateRotationComponent animateRotation = renderedEntity.getComponent(AnimateRotationComponent.class);
        if (animateRotation != null) {
            // update the speed if we have already added this component
            if (animateRotation.speed != speed) {
                animateRotation.speed = speed;
                renderedEntity.saveComponent(animateRotation);
            }
        } else {

            Rotation targetRotation = Rotation.rotate(Roll.CLOCKWISE_90);

            EntityRef ownerEntity = renderedEntity.getOwner();
            BlockComponent block = ownerEntity.getComponent(BlockComponent.class);
            Side direction = block.getBlock().getDirection();

        /*    if( direction == Side.FRONT) {
                targetRotation = Rotation.rotate(Roll.CLOCKWISE_90);
            } else if( direction == Side.BACK) {
                targetRotation = Rotation.rotate(Roll.CLOCKWISE_270);
            } else if( direction == Side.LEFT) {
                targetRotation = Rotation.rotate(Pitch.CLOCKWISE_90);
            } else if( direction == Side.RIGHT) {
                targetRotation = Rotation.rotate(Pitch.CLOCKWISE_270);
            } else if( direction == Side.TOP) {
                targetRotation = Rotation.rotate(Yaw.CLOCKWISE_90);
            } else if( direction == Side.BOTTOM) {
                targetRotation = Rotation.rotate(Yaw.CLOCKWISE_270);
            }   */

            animateRotation = new AnimateRotationComponent();
            animateRotation.isSynchronized = true;
            animateRotation.pitch = targetRotation.getPitch();
            animateRotation.roll = targetRotation.getRoll();
            animateRotation.yaw = targetRotation.getYaw();
            animateRotation.speed = speed;
            renderedEntity.addComponent(animateRotation);
        }
    }

    private void turnAxleOff(EntityRef renderedEntity) {
        renderedEntity.removeComponent(AnimateRotationComponent.class);
    }

    @Override
    public void networkAdded(Network network) {

    }

    @Override
    public void networkingNodeAdded(Network network, NetworkNode networkingNode) {
        updateAxlesInNetwork((MechanicalPowerNetwork) network);
    }

    @Override
    public void networkingNodeRemoved(Network network, NetworkNode networkingNode) {
        updateAxlesInNetwork((MechanicalPowerNetwork) network);
    }

    @Override
    public void networkRemoved(Network network) {

    }
}
