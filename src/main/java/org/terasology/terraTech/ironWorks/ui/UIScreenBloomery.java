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
package org.terasology.terraTech.ironWorks.ui;

import org.terasology.machines.gui.UIScreenGenericProcessing;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.terraTech.ironWorks.components.HeatedComponent;

import javax.vecmath.Vector2f;

/**
 * Created by Josharias on 1/23/14.
 */
public class UIScreenBloomery extends UIScreenGenericProcessing {
    public static final String BLOOMERYUI = "BLOOMERYUI";

    final UILabel heatLabel;

    public UIScreenBloomery() {
        setId(BLOOMERYUI);

        heatLabel = new UILabel();
        heatLabel.setVisible(true);
        heatLabel.setPosition(new Vector2f(450, 350));

        addDisplayElement(heatLabel);
    }


    @Override
    public void update() {
        if (isVisible()) {
            HeatedComponent heatedComponent = machineEntity.getComponent(HeatedComponent.class);

            if (heatedComponent != null) {
                heatLabel.setText(((int) (heatedComponent.temperature * 10f) / 10f) + " degrees");
            }
        }

        super.update();
    }
}
