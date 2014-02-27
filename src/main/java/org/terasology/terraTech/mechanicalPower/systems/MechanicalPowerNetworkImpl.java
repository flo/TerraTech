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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.Network;
import org.terasology.blockNetwork.NetworkNode;
import org.terasology.blockNetwork.NetworkTopologyListener;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.terraTech.mechanicalPower.components.MechanicalPowerBlockNetworkComponent;
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
 *
 * Producers and Consumers add themselves as a leaf.
 *
 */
@RegisterSystem
@Share(MechanicalPowerNetwork.class)
public class MechanicalPowerNetworkImpl extends BaseComponentSystem implements NetworkTopologyListener, MechanicalPowerNetwork {

    @In
    BlockEntityRegistry blockEntityRegistry;

    private static final Logger logger = LoggerFactory.getLogger(MechanicalPowerNetworkImpl.class);

    private BlockNetwork blockNetwork;
    private Map<Vector3i, NetworkNode> networkNodes = Maps.newHashMap();
    private Multimap<Network, NetworkNode> networkLeafNodes = HashMultimap.create();

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

    private void addNetworkNode(Vector3i position, byte connectionSides) {
        NetworkNode networkNode = new NetworkNode(position, connectionSides);
        networkNodes.put(position, networkNode);
        blockNetwork.addNetworkingBlock(networkNode);
    }
    private void removeNetworkNode(Vector3i position) {
        NetworkNode networkNode = networkNodes.get(position);
        blockNetwork.removeNetworkingBlock(networkNode);
        networkNodes.remove(position);
    }
    private void updateNetworkNode(Vector3i position, byte connectionSides) {
        NetworkNode oldNetworkNode = networkNodes.get(position);
        NetworkNode networkNode = new NetworkNode(position, connectionSides);
        blockNetwork.updateNetworkingBlock(oldNetworkNode, networkNode);
        networkNodes.put(position, networkNode);
    }

    @Override
    public Multimap<Network, NetworkNode> getLeafNodes() {
        return networkLeafNodes;
    }

    //region network topology listener

    @Override
    public void networkAdded(Network newNetwork) {
    }

    @Override
    public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes) {
        for(NetworkNode node : networkingNodes) {
            EntityRef entity = blockEntityRegistry.getBlockEntityAt(node.location.toVector3i());
            if( entity.hasComponent(MechanicalPowerProducerComponent.class)
                    || entity.hasComponent(MechanicalPowerConsumerComponent.class)) {
                networkLeafNodes.put(network, node);
            }
        }
    }

    @Override
    public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes) {
        for(NetworkNode node : networkingNodes) {
            networkLeafNodes.remove(network, node);
        }
    }

    @Override
    public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes) {
    }

    @Override
    public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes) {
    }

    @Override
    public void networkRemoved(Network network) {
    }

    //endregion

    //region adding and removing from the block network
    @ReceiveEvent
    public void createNetworkNodesOnWorldLoad(OnActivatedBlocks event, EntityRef blockType, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork) {
        byte connectingOnSides = mechanicalPowerBlockNetwork.getConnectionSides();
        for (Vector3i location : event.getBlockPositions()) {
            addNetworkNode(location, connectingOnSides);
        }
    }

    @ReceiveEvent
    public void removeNetworkNodesOnWorldUnload(BeforeDeactivateBlocks event, EntityRef blockType, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork) {
        for (Vector3i location : event.getBlockPositions()) {
            removeNetworkNode(location);
        }
    }

    @ReceiveEvent
    public void createNetworkNode(OnAddedComponent event, EntityRef entity, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        byte connectingOnSides = mechanicalPowerBlockNetwork.getConnectionSides();
        final Vector3i location = block.getPosition();
        addNetworkNode(location, connectingOnSides);
    }

    @ReceiveEvent
    public void updateNetworkNode(OnChangedComponent event, EntityRef entity, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        if (entity.hasComponent(BlockComponent.class)) {
            byte connectingOnSides = mechanicalPowerBlockNetwork.getConnectionSides();
            final Vector3i location = block.getPosition();
            updateNetworkNode(location, connectingOnSides);
        }
    }

    @ReceiveEvent
    public void removeNetworkNode(BeforeDeactivateComponent event, EntityRef entity, MechanicalPowerBlockNetworkComponent mechanicalPowerBlockNetwork, BlockComponent block) {
        final Vector3i location = block.getPosition();
        removeNetworkNode(location);
    }

    //endregion
}
