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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.journal.StaticJournalChapterHandler;
import org.terasology.journal.part.TextJournalPart;
import org.terasology.journal.part.TitleJournalPart;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.terraTech.gameType.events.PlayerProcessingButton;

import java.util.Arrays;
import java.util.List;

@RegisterSystem
public class JournalIntegration extends BaseComponentSystem {
    @In
    JournalManager journalManager;

    private String chapterId = "TerraTech";
    private Multimap<String, String> dependencyMap = HashMultimap.create();

    @Override
    public void initialise() {
        super.initialise();

        StaticJournalChapterHandler chapterHandler = new StaticJournalChapterHandler();

        List<JournalManager.JournalEntryPart> introduction = Arrays.asList(
                new TitleJournalPart("Terra Tech"),
                new TextJournalPart("This module is still being developed. Recipes work for Flat and Perlin world generators.  There is no support for Through the Ages yet."),
                new TextJournalPart("To begin, press 'U' to begin and open your character's assembly screen. Different things you can assemble are listed on the right.")
        );
        chapterHandler.registerJournalEntry("introduction", introduction);


        List<JournalManager.JournalEntryPart> overview = Arrays.asList(
                new TitleJournalPart("Smelting"),
                new TextJournalPart("Using a hearth,  you can heat ore to melting point to get metal nuggets."
                        + "  You need to heat your hearth by placing a fireplace next to it.  This does pose a problem, smoke is generated from burning materials."),
                new TextJournalPart("Burning materials poses a problem: smoke.  Smoke is generated when burning materials and floats upwards."
                        + "  Please dont breathe in the smoke, it burns.  Use a stack of chimneys to pipe the smoke to outside where it can blow away."),
                new TitleJournalPart("Essence"),
                new TextJournalPart("Using a distiller,  you can break down blocks of matter into their contained essences."
                        + "  Each different kind of block has a unique composition of essence."),
                new TextJournalPart("These essences can then be reformed into blocks using the compactor."
                        + "  Put a sample of the block into the template slot and supply the correct essences."),
                new TextJournalPart("Disobeying the laws of physics comes with a price: contamination."
                        + "  Working with essences causes bits of the world to start turning sour.  Healing this contamination can put the world back in order."),
                new TitleJournalPart("Conveyors"),
                new TextJournalPart("Use these nifty contraptions to get items from point A to point B."
                        + "  Item Extractors can take items out of chests and put them on to a conveyor.  Place them against the surface you want items to go in to."),
                new TitleJournalPart("Mechanical Power"),
                new TextJournalPart("Harness the power of rotational energy with either an engine or a windmill.  Feed an engine burnable material to produce energy."
                        + "  Create a windmill sail and put it into a windmill block to produce energy."),
                new TextJournalPart("Transfer power in a straight line with axles.  Change the direction of an axle with the help of a gearbox.")
        );
        chapterHandler.registerJournalEntry("overview", overview);
        dependencyMap.put("overview", "introduction");

        journalManager.registerJournalChapter(chapterId, Assets.getTexture("TerraTech", "TerraTechIcon"), "Terra Tech", chapterHandler);
    }


    private void discoveredEntry(EntityRef character, String entryId) {
        for (String dependentOn : dependencyMap.get(entryId)) {
            if (!journalManager.hasEntry(character, chapterId, dependentOn)) {
                discoveredEntry(character, dependentOn);
            }
        }
        if (!journalManager.hasEntry(character, chapterId, entryId)) {
            character.send(new DiscoveredNewJournalEntry(chapterId, entryId));
        }
    }


    @ReceiveEvent
    public void playerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(chapterId, "introduction"));
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onPlayerProcessingButton(PlayerProcessingButton event, EntityRef character) {
        discoveredEntry(character, "overview");
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void playerPickedUpItem(InventorySlotChangedEvent event, EntityRef character) {
    }
}

