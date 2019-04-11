/*
 * MIT License
 *
 * Copyright (c) 2019 dags
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package impl;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.AbstractAITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.merchant.VillagerRegistry;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipeRegistry;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipeRegistry;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.CatalogTypeAlreadyRegisteredException;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.RegistryModuleAlreadyRegisteredException;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlot;
import org.spongepowered.api.statistic.BlockStatistic;
import org.spongepowered.api.statistic.EntityStatistic;
import org.spongepowered.api.statistic.ItemStatistic;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.selector.SelectorFactory;
import org.spongepowered.api.text.serializer.TextSerializerFactory;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.extent.ExtentBufferFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TestReg implements GameRegistry {
    @Override
    public <T extends CatalogType> Optional<T> getType(Class<T> typeClass, String id) {
        if (typeClass == TextColor.class) {
            TextColor c = TestColor.REGISTRY.get(id);
            if (c != null) {
                return Optional.of(typeClass.cast(c));
            }
        }
        if (typeClass == TextStyle.Base.class) {
            TextStyle.Base s = TestStyle.REGISTRY.get(id);
            if (s != null) {
                return Optional.of(typeClass.cast(s));
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllOf(Class<T> typeClass) {
        if (typeClass == TextColor.class) {
            return TestColor.REGISTRY.values().stream().map(typeClass::cast).collect(Collectors.toList());
        }
        if (typeClass == TextStyle.Base.class) {
            return TestStyle.REGISTRY.values().stream().map(typeClass::cast).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends CatalogType> Collection<T> getAllFor(String pluginId, Class<T> typeClass) {
        return null;
    }

    @Override
    public <T extends CatalogType> GameRegistry registerModule(Class<T> catalogClass, CatalogRegistryModule<T> registryModule) throws IllegalArgumentException, RegistryModuleAlreadyRegisteredException {
        return null;
    }

    @Override
    public GameRegistry registerModule(RegistryModule module) throws RegistryModuleAlreadyRegisteredException {
        return null;
    }

    @Override
    public <T> GameRegistry registerBuilderSupplier(Class<T> builderClass, Supplier<? extends T> supplier) {
        return null;
    }

    @Override
    public <T extends ResettableBuilder<?, ? super T>> T createBuilder(Class<T> builderClass) throws IllegalArgumentException {
        return null;
    }

    @Override
    public <T extends CatalogType> T register(Class<T> type, T obj) throws IllegalArgumentException, CatalogTypeAlreadyRegisteredException {
        return null;
    }

    @Override
    public Collection<String> getDefaultGameRules() {
        return null;
    }

    @Override
    public Optional<EntityStatistic> getEntityStatistic(StatisticType statType, EntityType entityType) {
        return Optional.empty();
    }

    @Override
    public Optional<ItemStatistic> getItemStatistic(StatisticType statType, ItemType itemType) {
        return Optional.empty();
    }

    @Override
    public Optional<BlockStatistic> getBlockStatistic(StatisticType statType, BlockType blockType) {
        return Optional.empty();
    }

    @Override
    public Optional<Rotation> getRotationFromDegree(int degrees) {
        return Optional.empty();
    }

    @Override
    public Favicon loadFavicon(String raw) throws IOException {
        return null;
    }

    @Override
    public Favicon loadFavicon(Path path) throws IOException {
        return null;
    }

    @Override
    public Favicon loadFavicon(URL url) throws IOException {
        return null;
    }

    @Override
    public Favicon loadFavicon(InputStream in) throws IOException {
        return null;
    }

    @Override
    public Favicon loadFavicon(BufferedImage image) throws IOException {
        return null;
    }

    @Override
    public CraftingRecipeRegistry getCraftingRecipeRegistry() {
        return null;
    }

    @Override
    public SmeltingRecipeRegistry getSmeltingRecipeRegistry() {
        return null;
    }

    @Override
    public Optional<ResourcePack> getResourcePackById(String id) {
        return Optional.empty();
    }

    @Override
    public Optional<DisplaySlot> getDisplaySlotForColor(TextColor color) {
        return Optional.empty();
    }

    @Override
    public AITaskType registerAITaskType(Object plugin, String id, String name, Class<? extends AbstractAITask<? extends Agent>> aiClass) {
        return null;
    }

    @Override
    public ExtentBufferFactory getExtentBufferFactory() {
        return null;
    }

    @Override
    public ValueFactory getValueFactory() {
        return null;
    }

    @Override
    public VillagerRegistry getVillagerRegistry() {
        return null;
    }

    @Override
    public TextSerializerFactory getTextSerializerFactory() {
        return null;
    }

    @Override
    public SelectorFactory getSelectorFactory() {
        return null;
    }

    @Override
    public Locale getLocale(String locale) {
        return null;
    }

    @Override
    public Optional<Translation> getTranslationById(String id) {
        return Optional.empty();
    }
}
