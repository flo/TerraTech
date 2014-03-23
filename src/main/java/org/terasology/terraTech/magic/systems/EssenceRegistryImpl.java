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
package org.terasology.terraTech.magic.systems;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.terraTech.magic.components.EssenceContainerComponent;
import org.terasology.terraTech.magic.components.EssenceRegistryPageComponent;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.Map;

@RegisterSystem
@Share(EssenceRegistry.class)
public class EssenceRegistryImpl extends BaseComponentSystem implements EssenceRegistry {

    @In
    PrefabManager prefabManager;


    Map<String, EssenceContainerComponent> categories = Maps.newHashMap();

    @Override
    public void initialise() {
        // collect all the prefabs
        for (Prefab registryPagePrefab : prefabManager.listPrefabs(EssenceRegistryPageComponent.class)) {
            EssenceRegistryPageComponent registryPage = registryPagePrefab.getComponent(EssenceRegistryPageComponent.class);

            categories.putAll(registryPage.categories);
        }
    }

    public EssenceContainerComponent getContainedEssence(EntityRef item) {
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            for (String category : blockItem.blockFamily.getCategories()) {
                if (categories.containsKey(category)) {
                    return categories.get(category);
                }
            }
        }

        return null;
    }


}
