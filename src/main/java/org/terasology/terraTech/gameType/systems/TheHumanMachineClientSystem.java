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
package org.terasology.terraTech.gameType.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.terraTech.gameType.components.TheHumanMachineComponent;
import org.terasology.terraTech.gameType.events.PlayerProcessingButton;

@RegisterSystem(RegisterMode.CLIENT)
public class TheHumanMachineClientSystem extends BaseComponentSystem {
    @In
    NUIManager nuiManager;

    @In
    LocalPlayer localPlayer;

    @ReceiveEvent
    public void onPlayerProcessingButton(PlayerProcessingButton event, EntityRef player, CharacterComponent characterComponent) {
        if (event.getState() == ButtonState.DOWN) {
            TheHumanMachineComponent theHumanMachine = player.getComponent(TheHumanMachineComponent.class);
            localPlayer.activateOwnedEntityAsClient(theHumanMachine.machineEntity);
            event.consume();
        }
    }
}
