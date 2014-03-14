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

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.Network;
import org.terasology.blockNetwork.NetworkNode;
import org.terasology.blockNetwork.NetworkTopologyListener;
import org.terasology.blockNetwork.SidedLocationNetworkNode;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Direction;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerBlockNetworkComponent;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerConductorComponent;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerConsumerComponent;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BeforeDeactivateBlocks;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.OnActivatedBlocks;

import java.util.Map;
import java.util.Set;

/**
 * the block network is controlled by the MechanicalPowerBlockNetworkComponent.  Its removal removes both the node and
 * leaf from the network.
 * <p/>
 * Producers and Consumers add themselves as a leaf.
 */
@RegisterSystem
@Share(MechanicalPowerBlockNetwork.class)
public class MechanicalPowerBlockNetworkImpl extends BaseComponentSystem implements NetworkTopologyListener, MechanicalPowerBlockNetwork {

    @In
    BlockEntityRegistry blockEntityRegistry;

    private static final Logger logger = LoggerFactory.getLogger(MechanicalPowerBlockNetworkImpl.class);

    private BlockNetwork blockNetwork;
    private Map<Vector3i, SidedLocationNetworkNode> networkNodes = Maps.newHashMap();
    private Map<Network, MechanicalPowerNetworkDetails> networks = Maps.newHashMap();

    @Override
    public void initialise() {
        blockNetwork = new BlockNetwork();
        blockNetwork.addTopologyListener(this);
        logger.info("Initialized Mechanical Power System");
    }

    @Override
    public void shutdown() {
        blockNetwork = null;
    }


    @Override
    public Network getNetwork(Vector3i position) {
        return blockNetwork.getNetwork(networkNodes.get(position));
    }

    @Override
    public Iterable<SidedLocationNetworkNode> getNetworkNodes(Network network) {
        Iterable<NetworkNode> nodes = blockNetwork.getNetworkNodes(network);

        return Iterables.filter(nodes, SidedLocationNetworkNode.class);
    }

    @Override
    public Iterable<Network> getNetworks() {
        return blockNetwork.getNetworks();
    }

    @Override
    public MechanicalPowerNetworkDetails getMechanicalPowerNetwork(Network network) {
        return networks.get(network);
    }

    @Override
    public void addTopologyListener(NetworkTopologyListener networkTopologyListener) {
        blockNetwork.addTopologyListener(networkTopologyListener);
    }


    private void addNetworkNode(EntityRef entity) {
        SidedLocationNetworkNode networkNode = null;

        MechanicalPowerBlockNetworkComponent networkItem = entity.getComponent(MechanicalPowerBlockNetworkComponent.class);

        BlockComponent block = entity.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition();
        byte connectionSides = calculateConnectionSides(networkItem, block);

        if(entity.hasComponent(MechanicalPowerProducerComponent.class)) {
            networkNode = new ProducerNode(position, connectionSides);
        }else if(entity.hasComponent(MechanicalPowerConsumerComponent.class)) {
            networkNode = new ConsumerNode(position, connectionSides);
        }else {
            networkNode = new SidedLocationNetworkNode(position, connectionSides);
        }

        networkNodes.put(position, networkNode);
        blockNetwork.addNetworkingBlock(networkNode);
    }

    private byte calculateConnectionSides(MechanicalPowerBlockNetworkComponent networkItem, BlockComponent block) {
        Side blockDirection = block.getBlock().getDirection();
        byte connectionSides = 0;
        if(networkItem.directions.size() == 0) {
            connectionSides = (byte)63;
        }else {
            // convert these directions to sides relative to the facing of the block
            Set<Side> sides = Sets.newHashSet();
            for(String directionString : networkItem.directions) {
                Direction direction = Direction.valueOf(directionString);
                sides.add(blockDirection.getRelativeSide(direction));
            }

            connectionSides = SideBitFlag.getSides(sides);
        }
        return connectionSides;
    }

    private void removeNetworkNode(Vector3i position) {
        SidedLocationNetworkNode networkNode = networkNodes.get(position);
        blockNetwork.removeNetworkingBlock(networkNode);
        networkNodes.remove(position);
    }

    private void updateNetworkNode(Vector3i position, byte connectionSides) {
        SidedLocationNetworkNode oldNetworkNode = networkNodes.get(position);
        SidedLocationNetworkNode networkNode = new SidedLocationNetworkNode(position, connectionSides);
        blockNetwork.updateNetworkingBlock(oldNetworkNode, networkNode);
        networkNodes.put(position, networkNode);
    }

    //region adding and removing from the block network

    // this event can happen generically because the full entity will be retrieved
    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void createNetworkNodesOnWorldLoad(OnActivatedBlocks event, EntityRef blockType, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork) {
        for (Vector3i location : event.getBlockPositions()) {
            addNetworkNode(blockEntityRegistry.getBlockEntityAt(location));
        }
    }

    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void removeNetworkNodesOnWorldUnload(BeforeDeactivateBlocks event, EntityRef blockType, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork) {
        for (Vector3i location : event.getBlockPositions()) {
            removeNetworkNode(location);
        }
    }

    // this event can happen generically because we are only updating the connection sides
    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void updateNetworkNode(OnChangedComponent event, EntityRef entity, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        byte connectingOnSides = calculateConnectionSides(mechanicalPowerBlockNetwork, block);
        final Vector3i location = block.getPosition();
        updateNetworkNode(location, connectingOnSides);
    }

    // this event can happen generically because it is position based
    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void removeNetworkNode(BeforeDeactivateComponent event, EntityRef entity, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        removeNetworkNode(block.getPosition());
    }

    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void createProducerNetworkNode(OnActivatedComponent event, EntityRef entity, MechanicalPowerProducerComponent type, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        addNetworkNode(entity);
    }

    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void createConsumerNetworkNode(OnActivatedComponent event, EntityRef entity, MechanicalPowerConsumerComponent type, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        addNetworkNode(entity);
    }

    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void createConductorNetworkNode(OnActivatedComponent event, EntityRef entity, MechanicalPowerConductorComponent type, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        addNetworkNode(entity);
    }

    @ReceiveEvent (priority = EventPriority.PRIORITY_CRITICAL)
    public void updateCurrentPowerInNetwork(OnChangedComponent event, EntityRef entity, MechanicalPowerProducerComponent producer, BlockComponent block) {
        Vector3i location = block.getPosition();
        ProducerNode producerNode = getProducerNode(location);
        // save the current power output so we can remove it if the node is removed
        producerNode.power = producer.active ? producer.power : 0;

        MechanicalPowerNetworkDetails network = getMechanicalPowerNetwork(getNetwork(location));
        network.totalPower += producer.active ? producer.power : -producer.power;
    }

    private ProducerNode getProducerNode(Vector3i location) {
        SidedLocationNetworkNode networkNode = networkNodes.get(location);
        if( networkNode instanceof  ProducerNode) {
            return (ProducerNode) networkNode;
        }else {
            // upgrade it
            ProducerNode producerNode = new ProducerNode(networkNode.location, networkNode.connectionSides);
            blockNetwork.updateNetworkingBlock(networkNode, producerNode);
            networkNodes.put(location, producerNode);
            return producerNode;
        }
    }
    private ConsumerNode getConsumerNode(Vector3i location) {
        SidedLocationNetworkNode networkNode = networkNodes.get(location);
        if( networkNode instanceof  ConsumerNode) {
            return (ConsumerNode) networkNode;
        }else {
            // upgrade it
            ConsumerNode consumerNode = new ConsumerNode(networkNode.location, networkNode.connectionSides);
            blockNetwork.updateNetworkingBlock(networkNode, consumerNode);
            networkNodes.put(location, consumerNode);
            return consumerNode;
        }
    }

    @Override
    public void networkAdded(Network network) {
        networks.put(network, new MechanicalPowerNetworkDetails());
    }

    @Override
    public void networkingNodeAdded(Network network, NetworkNode networkingNode) {
        MechanicalPowerNetworkDetails powerNetwork = getMechanicalPowerNetwork(network);

        if (networkingNode instanceof ProducerNode) {
            powerNetwork.totalProducers++;
        } else if (networkingNode instanceof ConsumerNode) {
            powerNetwork.totalConsumers++;
        }
    }

    @Override
    public void networkingNodeRemoved(Network network, NetworkNode networkingNode) {
        MechanicalPowerNetworkDetails powerNetwork = getMechanicalPowerNetwork(network);

        if (networkingNode instanceof ProducerNode) {
            ProducerNode producerNode = (ProducerNode) networkingNode;
            powerNetwork.totalPower -= producerNode.power;
            powerNetwork.totalProducers--;
        } else if (networkingNode instanceof ConsumerNode) {
            powerNetwork.totalConsumers--;
        }
    }

    @Override
    public void networkRemoved(Network network) {
        networks.remove(network);
    }

    //endregion
}
