/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.terraTech.gameType.systems;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.ButtonState;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.machines.ProcessingManager;
import org.terasology.machines.gui.UIScreenGenericProcessing;
import org.terasology.registry.In;
import org.terasology.terraTech.gameType.events.PlayerProcessingButton;

@RegisterSystem(RegisterMode.CLIENT)
public class ProcessingMachineClientSystem implements ComponentSystem {

    @In
    GUIManager guiManager;
    @In
    EntityManager entityManager;
    @In
    ProcessingManager processingManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onPlayerProcessingButton(PlayerProcessingButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            guiManager.toggleWindow(UIScreenGenericProcessing.UIGENERICPROCESSINGID);
            UIScreenGenericProcessing screen = (UIScreenGenericProcessing) guiManager.getWindowById(UIScreenGenericProcessing.UIGENERICPROCESSINGID);

            if (screen.isVisible()) {
                screen.linkMachine(entity);
            }
            event.consume();
        }
    }
}
