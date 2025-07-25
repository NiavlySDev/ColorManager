/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2025 KyoriPowered
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
 */
package net.kyori.adventure.text;

import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.*;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import net.kyori.adventure.util.ARGBLike;
import net.kyori.adventure.util.ForwardingIterator;
import net.kyori.adventure.util.IntFunction2;
import net.kyori.adventure.util.MonkeyBars;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A Component is an immutable object that represents how text
 * is displayed Minecraft clients.
 *
 * <p>Components can be thought of as the combination of:</p>
 *
 * <ul>
 *  <li>The message the Component wants to display; and</li>
 *  <li>The {@link Style} of that message.</li>
 * </ul>
 *
 * <p>The most basic component is the {@link TextComponent},
 * where the message is a simple String. However, other dynamic
 * Components are available, which are linked to from here and you
 * are encouraged to explore to better support your plugin/mod.
 * Factories and builders for all available component types are
 * provided via this interface.</p>
 *
 * <p>Components can be serialized to and deserialized from other
 * formats via the use of {@link ComponentSerializer component
 * serializers}. If used within one of our natively supported platforms,
 * the availability of such serializers may vary, consult the documentation
 * or support for the given platform should this be the case.</p>
 *
 * <p>Further information about Components, along with functional
 * examples of how they can be used,
 * <a href="https://docs.advntr.dev/text.html">can be found on
 * our documentation.</a></p>
 *
 * @see BlockNBTComponent
 * @see EntityNBTComponent
 * @see KeybindComponent
 * @see ScoreComponent
 * @see SelectorComponent
 * @see StorageNBTComponent
 * @see TextComponent
 * @see TranslatableComponent
 * @see LinearComponents
 * @since 4.0.0
 */
@ApiStatus.NonExtendable
public interface Component extends ComponentBuilderApplicable, ComponentLike, Examinable, HoverEventSource<Component>, StyleGetter, StyleSetter<Component> {
  /**
   * A predicate that checks equality of two {@code Component}s using {@link Objects#equals(Object, Object)}.
   *
   * @since 4.8.0
   */
  BiPredicate<? super Component, ? super Component> EQUALS = Objects::equals;
  /**
   * A predicate that checks equality of two {@code Component}s using identity equality.
   *
   * @since 4.8.0
   */
  BiPredicate<? super Component, ? super Component> EQUALS_IDENTITY = (a, b) -> a == b;
  /**
   * A predicate that excludes {@link #empty()}.
   *
   * @since 4.10.0
   */
  Predicate<? super Component> IS_NOT_EMPTY = component -> component != empty();

  /**
   * Gets an empty component.
   *
   * @return an empty component
   * @since 4.0.0
   */
  static @NotNull TextComponent empty() {
    return TextComponentImpl.EMPTY;
  }

  /**
   * Gets a text component with a new line character as the content.
   *
   * @return a text component with a new line character as the content
   * @since 4.0.0
   */
  static @NotNull TextComponent newline() {
    return TextComponentImpl.NEWLINE;
  }

  /**
   * Gets a text immutable component with a single space as the content.
   *
   * @return a text component with a single space as the content
   * @since 4.0.0
   */
  static @NotNull TextComponent space() {
    return TextComponentImpl.SPACE;
  }

  /**
   * Joins {@code components} using {@code separator}.
   *
   * @param separator the separator
   * @param components the components
   * @return a text component
   * @since 4.0.0
   * @deprecated for removal since 4.9.0, use {@link #join(JoinConfiguration, ComponentLike...)} with {@link JoinConfiguration#separator(ComponentLike)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent join(final @NotNull ComponentLike separator, final @NotNull ComponentLike@NotNull... components) {
    return join(separator, Arrays.asList(components));
  }

  /**
   * Joins {@code components} using {@code separator}.
   *
   * @param separator the separator
   * @param components the components
   * @return a text component
   * @since 4.0.0
   * @deprecated for removal since 4.9.0, use {@link #join(JoinConfiguration, Iterable)} with {@link JoinConfiguration#separator(ComponentLike)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent join(final @NotNull ComponentLike separator, final Iterable<? extends ComponentLike> components) {
    final Component component = join(JoinConfiguration.separator(separator), components);

    if (component instanceof TextComponent) return (TextComponent) component;
    return Component.text().append(component).build();
  }

  /**
   * Joins {@code components} using the configuration in {@code configBuilder}.
   *
   * @param configBuilder the join configuration
   * @param components the components
   * @return the resulting component
   * @see JoinConfiguration#noSeparators()
   * @see JoinConfiguration#separator(ComponentLike)
   * @see JoinConfiguration#separators(ComponentLike, ComponentLike)
   * @since 4.14.0
   */
  @Contract(pure = true)
  static @NotNull Component join(final JoinConfiguration.@NotNull Builder configBuilder, final @NotNull ComponentLike@NotNull... components) {
    return join(configBuilder, Arrays.asList(components));
  }

  /**
   * Joins {@code components} using the configuration in {@code configBuilder}.
   *
   * @param configBuilder the join configuration
   * @param components the components
   * @return the resulting component
   * @see JoinConfiguration#noSeparators()
   * @see JoinConfiguration#separator(ComponentLike)
   * @see JoinConfiguration#separators(ComponentLike, ComponentLike)
   * @since 4.14.0
   */
  @Contract(pure = true)
  static @NotNull Component join(final JoinConfiguration.@NotNull Builder configBuilder, final @NotNull Iterable<? extends ComponentLike> components) {
    return JoinConfigurationImpl.join(configBuilder.build(), components);
  }

  /**
   * Joins {@code components} using the configuration in {@code config}.
   *
   * @param config the join configuration
   * @param components the components
   * @return the resulting component
   * @see JoinConfiguration#noSeparators()
   * @see JoinConfiguration#separator(ComponentLike)
   * @see JoinConfiguration#separators(ComponentLike, ComponentLike)
   * @since 4.9.0
   */
  @Contract(pure = true)
  static @NotNull Component join(final @NotNull JoinConfiguration config, final @NotNull ComponentLike@NotNull... components) {
    return join(config, Arrays.asList(components));
  }

  /**
   * Joins {@code components} using the configuration in {@code config}.
   *
   * @param config the join configuration
   * @param components the components
   * @return the resulting component
   * @see JoinConfiguration#noSeparators()
   * @see JoinConfiguration#separator(ComponentLike)
   * @see JoinConfiguration#separators(ComponentLike, ComponentLike)
   * @since 4.9.0
   */
  @Contract(pure = true)
  static @NotNull Component join(final @NotNull JoinConfiguration config, final @NotNull Iterable<? extends ComponentLike> components) {
    return JoinConfigurationImpl.join(config, components);
  }

  /**
   * Create a collector that will join components without a separator.
   *
   * @return a collector that can join components
   * @since 4.6.0
   */
  static @NotNull Collector<Component, ? extends ComponentBuilder<?, ?>, Component> toComponent() {
    return toComponent(Component.empty());
  }

  /**
   * Create a collector that will join components using the provided separator.
   *
   * @param separator the separator to join with
   * @return a collector that can join components
   * @since 4.6.0
   */
  static @NotNull Collector<Component, ? extends ComponentBuilder<?, ?>, Component> toComponent(final @NotNull Component separator) {
    return Collector.of(
      Component::text,
      (builder, add) -> {
        if (separator != Component.empty() && !builder.children().isEmpty()) {
          builder.append(separator);
        }
        builder.append(add);
      }, (a, b) -> {
        final List<Component> aChildren = a.children();
        final TextComponent.Builder ret = Component.text().append(aChildren);
        if (!aChildren.isEmpty()) {
          ret.append(separator);
        }
        ret.append(b.children());
        return ret;
      },
      TextComponent.Builder::build
    );
  }

  /*
   * ---------------------------
   * ---- BlockNBTComponent ----
   * ---------------------------
   */

  /**
   * Creates a block NBT component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static BlockNBTComponent.@NotNull Builder blockNBT() {
    return new BlockNBTComponentImpl.BuilderImpl();
  }

  /**
   * Creates a block NBT component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return a block NBT component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull BlockNBTComponent blockNBT(final @NotNull Consumer<? super BlockNBTComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(blockNBT(), consumer);
  }

  /**
   * Creates a block NBT component with a position.
   *
   * @param nbtPath the nbt path
   * @param pos the block position
   * @return a block NBT component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull BlockNBTComponent blockNBT(final @NotNull String nbtPath, final BlockNBTComponent.@NotNull Pos pos) {
    return blockNBT(nbtPath, NBTComponentImpl.INTERPRET_DEFAULT, pos);
  }

  /**
   * Creates a block NBT component with a position.
   *
   * @param nbtPath the nbt path
   * @param interpret whether to interpret
   * @param pos the block position
   * @return a block NBT component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull BlockNBTComponent blockNBT(final @NotNull String nbtPath, final boolean interpret, final BlockNBTComponent.@NotNull Pos pos) {
    return blockNBT(nbtPath, interpret, null, pos);
  }

  /**
   * Creates a block NBT component with a position.
   *
   * @param nbtPath the nbt path
   * @param interpret whether to interpret
   * @param separator the separator
   * @param pos the block position
   * @return a block NBT component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull BlockNBTComponent blockNBT(final @NotNull String nbtPath, final boolean interpret, final @Nullable ComponentLike separator, final BlockNBTComponent.@NotNull Pos pos) {
    return BlockNBTComponentImpl.create(Collections.emptyList(), Style.empty(), nbtPath, interpret, separator, pos);
  }

  /*
   * ----------------------------
   * ---- EntityNBTComponent ----
   * ----------------------------
   */

  /**
   * Creates an entity NBT component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static EntityNBTComponent.@NotNull Builder entityNBT() {
    return new EntityNBTComponentImpl.BuilderImpl();
  }

  /**
   * Creates a entity NBT component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return an entity NBT component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull EntityNBTComponent entityNBT(final @NotNull Consumer<? super EntityNBTComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(entityNBT(), consumer);
  }

  /**
   * Creates a entity NBT component with a position.
   *
   * @param nbtPath the nbt path
   * @param selector the selector
   * @return an entity NBT component
   * @since 4.0.0
   */
  @Contract("_, _ -> new")
  static @NotNull EntityNBTComponent entityNBT(final @NotNull String nbtPath, final @NotNull String selector) {
    return entityNBT().nbtPath(nbtPath).selector(selector).build();
  }

  /*
   * --------------------------
   * ---- KeybindComponent ----
   * --------------------------
   */

  /**
   * Creates a keybind component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static KeybindComponent.@NotNull Builder keybind() {
    return new KeybindComponentImpl.BuilderImpl();
  }

  /**
   * Creates a keybind component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull KeybindComponent keybind(final @NotNull Consumer<? super KeybindComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(keybind(), consumer);
  }

  /**
   * Creates a keybind component with a keybind.
   *
   * @param keybind the keybind
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final @NotNull String keybind) {
    return keybind(keybind, Style.empty());
  }

  /**
   * Creates a keybind component with a keybind.
   *
   * @param keybind the keybind
   * @return the keybind component
   * @since 4.9.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final KeybindComponent.@NotNull KeybindLike keybind) {
    return keybind(requireNonNull(keybind, "keybind").asKeybind(), Style.empty());
  }

  /**
   * Creates a keybind component with a keybind and styling.
   *
   * @param keybind the keybind
   * @param style the style
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final @NotNull String keybind, final @NotNull Style style) {
    return KeybindComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), keybind);
  }

  /**
   * Creates a keybind component with a keybind and styling.
   *
   * @param keybind the keybind
   * @param style the style
   * @return the keybind component
   * @since 4.9.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final KeybindComponent.@NotNull KeybindLike keybind, final @NotNull Style style) {
    return KeybindComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), requireNonNull(keybind, "keybind").asKeybind());
  }

  /**
   * Creates a keybind component with a keybind, and optional color.
   *
   * @param keybind the keybind
   * @param color the color
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final @NotNull String keybind, final @Nullable TextColor color) {
    return keybind(keybind, Style.style(color));
  }

  /**
   * Creates a keybind component with a keybind, and optional color.
   *
   * @param keybind the keybind
   * @param color the color
   * @return the keybind component
   * @since 4.9.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final KeybindComponent.@NotNull KeybindLike keybind, final @Nullable TextColor color) {
    return keybind(requireNonNull(keybind, "keybind").asKeybind(), Style.style(color));
  }

  /**
   * Creates a keybind component with a keybind, and optional color and decorations.
   *
   * @param keybind the keybind
   * @param color the color
   * @param decorations the decorations
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final @NotNull String keybind, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return keybind(keybind, Style.style(color, decorations));
  }

  /**
   * Creates a keybind component with a keybind, and optional color and decorations.
   *
   * @param keybind the keybind
   * @param color the color
   * @param decorations the decorations
   * @return the keybind component
   * @since 4.9.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final KeybindComponent.@NotNull KeybindLike keybind, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return keybind(requireNonNull(keybind, "keybind").asKeybind(), Style.style(color, decorations));
  }

  /**
   * Creates a keybind component with a keybind, and optional color and decorations.
   *
   * @param keybind the keybind
   * @param color the color
   * @param decorations the decorations
   * @return the keybind component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final @NotNull String keybind, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return keybind(keybind, Style.style(color, decorations));
  }

  /**
   * Creates a keybind component with a keybind, and optional color and decorations.
   *
   * @param keybind the keybind
   * @param color the color
   * @param decorations the decorations
   * @return the keybind component
   * @since 4.9.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull KeybindComponent keybind(final KeybindComponent.@NotNull KeybindLike keybind, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return keybind(requireNonNull(keybind, "keybind").asKeybind(), Style.style(color, decorations));
  }

  /*
   * ------------------------
   * ---- ScoreComponent ----
   * ------------------------
   */

  /**
   * Creates a score component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static ScoreComponent.@NotNull Builder score() {
    return new ScoreComponentImpl.BuilderImpl();
  }

  /**
   * Creates a score component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return a score component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull ScoreComponent score(final @NotNull Consumer<? super ScoreComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(score(), consumer);
  }

  /**
   * Creates a score component with a name and objective.
   *
   * @param name the score name
   * @param objective the score objective
   * @return a score component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull ScoreComponent score(final @NotNull String name, final @NotNull String objective) {
    return score(name, objective, null);
  }

  /**
   * Creates a score component with a name, objective, and optional value.
   *
   * @param name the score name
   * @param objective the score objective
   * @param value the value
   * @return a score component
   * @since 4.0.0
   * @deprecated since 4.7.0, not for removal, with no replacement. The {@code value} field is no longer supported in 1.16.5.
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  @Deprecated
  static @NotNull ScoreComponent score(final @NotNull String name, final @NotNull String objective, final @Nullable String value) {
    return ScoreComponentImpl.create(Collections.emptyList(), Style.empty(), name, objective, value);
  }

  /*
   * ---------------------------
   * ---- SelectorComponent ----
   * ---------------------------
   */

  /**
   * Creates a selector component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static SelectorComponent.@NotNull Builder selector() {
    return new SelectorComponentImpl.BuilderImpl();
  }

  /**
   * Creates a selector component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return a selector component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull SelectorComponent selector(final @NotNull Consumer<? super SelectorComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(selector(), consumer);
  }

  /**
   * Creates a selector component with a pattern.
   *
   * @param pattern the selector pattern
   * @return a selector component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull SelectorComponent selector(final @NotNull String pattern) {
    return selector(pattern, null);
  }

  /**
   * Creates a selector component with a pattern.
   *
   * @param pattern the selector pattern
   * @param separator the separator
   * @return a selector component
   * @since 4.8.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull SelectorComponent selector(final @NotNull String pattern, final @Nullable ComponentLike separator) {
    return SelectorComponentImpl.create(Collections.emptyList(), Style.empty(), pattern, separator);
  }

  /*
   * -----------------------------
   * ---- StorageNBTComponent ----
   * -----------------------------
   */

  /**
   * Creates an storage NBT component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static StorageNBTComponent.@NotNull Builder storageNBT() {
    return new StorageNBTComponentImpl.BuilderImpl();
  }

  /**
   * Creates a storage NBT component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return a storage NBT component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull StorageNBTComponent storageNBT(final @NotNull Consumer<? super StorageNBTComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(storageNBT(), consumer);
  }

  /**
   * Creates a storage NBT component with a path and an storage ID.
   *
   * @param nbtPath the nbt path
   * @param storage the identifier of the storage
   * @return a storage NBT component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull StorageNBTComponent storageNBT(final @NotNull String nbtPath, final @NotNull Key storage) {
    return storageNBT(nbtPath, NBTComponentImpl.INTERPRET_DEFAULT, storage);
  }

  /**
   * Creates a storage NBT component with a path and an storage ID.
   *
   * @param nbtPath the nbt path
   * @param interpret whether to interpret
   * @param storage the identifier of the storage
   * @return a storage NBT component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull StorageNBTComponent storageNBT(final @NotNull String nbtPath, final boolean interpret, final @NotNull Key storage) {
    return storageNBT(nbtPath, interpret, null, storage);
  }

  /**
   * Creates a storage NBT component with a path and an storage ID.
   *
   * @param nbtPath the nbt path
   * @param interpret whether to interpret
   * @param separator the separator
   * @param storage the identifier of the storage
   * @return a storage NBT component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull StorageNBTComponent storageNBT(final @NotNull String nbtPath, final boolean interpret, final @Nullable ComponentLike separator, final @NotNull Key storage) {
    return StorageNBTComponentImpl.create(Collections.emptyList(), Style.empty(), nbtPath, interpret, separator, storage);
  }

  /*
   * -----------------------
   * ---- TextComponent ----
   * -----------------------
   */

  /**
   * Creates a text component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static TextComponent.@NotNull Builder text() {
    return new TextComponentImpl.BuilderImpl();
  }

  /**
   * Creates a text component with {@code components} as the children.
   *
   * @param components the children
   * @return a text component
   * @since 4.10.0
   */
  static @NotNull TextComponent textOfChildren(final @NotNull ComponentLike@NotNull... components) {
    if (components.length == 0) return empty();
    return TextComponentImpl.create(Arrays.asList(components), Style.empty(), "");
  }

  /**
   * Creates a text component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return the text component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull TextComponent text(final @NotNull Consumer<? super TextComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(text(), consumer);
  }

  /**
   * Creates a text component with content.
   *
   * @param content the plain text content
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final @NotNull String content) {
    if (content.isEmpty()) return empty();
    return text(content, Style.empty());
  }

  /**
   * Creates a text component with content and styling.
   *
   * @param content the plain text content
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final @NotNull String content, final @NotNull Style style) {
    return TextComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), content);
  }

  /**
   * Creates a text component with content, and optional color.
   *
   * @param content the plain text content
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final @NotNull String content, final @Nullable TextColor color) {
    return text(content, Style.style(color));
  }

  /**
   * Creates a text component with content, and optional color and decorations.
   *
   * @param content the plain text content
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final @NotNull String content, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(content, Style.style(color, decorations));
  }

  /**
   * Creates a text component with content, and optional color and decorations.
   *
   * @param content the plain text content
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final @NotNull String content, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(content, Style.style(color, decorations));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(boolean)}.
   *
   * @param value the boolean value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final boolean value) {
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(boolean)} and styling.
   *
   * @param value the boolean value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final boolean value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(boolean)}, and optional color.
   *
   * @param value the boolean value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final boolean value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(boolean)}, and optional color and decorations.
   *
   * @param value the boolean value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final boolean value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(boolean)}, and optional color and decorations.
   *
   * @param value the boolean value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final boolean value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(char)}.
   *
   * @param value the char value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(pure = true)
  static @NotNull TextComponent text(final char value) {
    if (value == '\n') return newline();
    if (value == ' ') return space();
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(char)} and styling.
   *
   * @param value the char value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final char value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(char)}, and optional color.
   *
   * @param value the char value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final char value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(char)}, and optional color and decorations.
   *
   * @param value the char value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final char value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(char)}, and optional color and decorations.
   *
   * @param value the char value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final char value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(double)}.
   *
   * @param value the double value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final double value) {
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(double)} and styling.
   *
   * @param value the double value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final double value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(double)}, and optional color.
   *
   * @param value the double value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final double value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(double)}, and optional color and decorations.
   *
   * @param value the double value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final double value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(double)}, and optional color and decorations.
   *
   * @param value the double value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final double value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(float)}.
   *
   * @param value the float value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final float value) {
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(float)} and styling.
   *
   * @param value the float value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final float value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(float)}, and optional color.
   *
   * @param value the float value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final float value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(float)}, and optional color and decorations.
   *
   * @param value the float value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final float value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(float)}, and optional color and decorations.
   *
   * @param value the float value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final float value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(int)}.
   *
   * @param value the int value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final int value) {
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(int)} and styling.
   *
   * @param value the int value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final int value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(int)}, and optional color.
   *
   * @param value the int value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final int value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(int)}, and optional color and decorations.
   *
   * @param value the int value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final int value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(int)}, and optional color and decorations.
   *
   * @param value the int value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final int value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(long)}.
   *
   * @param value the long value
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TextComponent text(final long value) {
    return text(String.valueOf(value));
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(long)} and styling.
   *
   * @param value the long value
   * @param style the style
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final long value, final @NotNull Style style) {
    return text(String.valueOf(value), style);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(long)}, and optional color.
   *
   * @param value the long value
   * @param color the color
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TextComponent text(final long value, final @Nullable TextColor color) {
    return text(String.valueOf(value), color);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(long)}, and optional color and decorations.
   *
   * @param value the long value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final long value, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /**
   * Creates a text component with the content of {@link String#valueOf(long)}, and optional color and decorations.
   *
   * @param value the long value
   * @param color the color
   * @param decorations the decorations
   * @return a text component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TextComponent text(final long value, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return text(String.valueOf(value), color, decorations);
  }

  /*
   * --------------------------
   * ---- VirtualComponent ----
   * --------------------------
   */

  /**
   * Creates a virtual component.
   *
   * @param <C> the context type
   * @param contextType the context type
   * @param renderer the renderer
   * @return a virtual component
   * @since 4.18.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static <C> @NotNull VirtualComponent virtual(final @NotNull Class<C> contextType, final @NotNull VirtualComponentRenderer<C> renderer) {
    requireNonNull(contextType, "context type");
    requireNonNull(renderer, "renderer");
    return VirtualComponentImpl.createVirtual(contextType, renderer);
  }

  /**
   * Creates a virtual component with a value.
   *
   * @param <C> the context type
   * @param contextType the context type
   * @param renderer the renderer
   * @param style the style
   * @return a virtual component
   * @since 4.18.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static <C> @NotNull VirtualComponent virtual(final @NotNull Class<C> contextType, final @NotNull VirtualComponentRenderer<C> renderer, final @NotNull Style style) {
    requireNonNull(contextType, "context type");
    requireNonNull(renderer, "renderer");
    return VirtualComponentImpl.createVirtual(contextType, renderer, Collections.emptyList(), style);
  }

  /**
   * Creates a virtual component with a value.
   *
   * @param <C> the context type
   * @param contextType the context type
   * @param renderer the renderer
   * @param style the style elements
   * @return a virtual component
   * @since 4.18.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static <C> @NotNull VirtualComponent virtual(final @NotNull Class<C> contextType, final @NotNull VirtualComponentRenderer<C> renderer, final @NotNull StyleBuilderApplicable... style) {
    requireNonNull(contextType, "context type");
    requireNonNull(renderer, "renderer");
    return VirtualComponentImpl.createVirtual(contextType, renderer, Collections.emptyList(), Style.style(style));
  }

  /**
   * Creates a virtual component with a value.
   *
   * @param <C> the context type
   * @param contextType the context type
   * @param renderer the renderer
   * @param style the style elements
   * @return a virtual component
   * @since 4.18.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static <C> @NotNull VirtualComponent virtual(final @NotNull Class<C> contextType, final @NotNull VirtualComponentRenderer<C> renderer, final @NotNull Iterable<StyleBuilderApplicable> style) {
    requireNonNull(contextType, "context type");
    requireNonNull(renderer, "renderer");
    return VirtualComponentImpl.createVirtual(contextType, renderer, Collections.emptyList(), Style.style(style));
  }

  /*
   * -------------------------------
   * ---- TranslatableComponent ----
   * -------------------------------
   */

  /**
   * Creates a translatable component builder.
   *
   * @return a builder
   * @since 4.0.0
   */
  @Contract(pure = true)
  static TranslatableComponent.@NotNull Builder translatable() {
    return new TranslatableComponentImpl.BuilderImpl();
  }

  /**
   * Creates a translatable component by applying configuration from {@code consumer}.
   *
   * @param consumer the builder configurator
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract("_ -> new")
  static @NotNull TranslatableComponent translatable(final @NotNull Consumer<? super TranslatableComponent.Builder> consumer) {
    return AbstractBuilder.configureAndBuild(translatable(), consumer);
  }

  /**
   * Creates a translatable component with a translation key.
   *
   * @param key the translation key
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key) {
    return translatable(key, Style.empty());
  }

  /**
   * Creates a translatable component with a translation key.
   *
   * @param translatable the translatable object to get the key from
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), Style.empty());
  }

  /**
   * Creates a translatable component with a translation key and an optional fallback string.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback) {
    return translatable(key, fallback, Style.empty());
  }

  /**
   * Creates a translatable component with a translation key and an optional fallback string.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, Style.empty());
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param key the translation key
   * @param style the style
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @NotNull Style style) {
    return TranslatableComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), key, null, Collections.emptyList());
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param style the style
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @NotNull Style style) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), style);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param style the style
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull Style style) {
    return TranslatableComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), key, fallback, Collections.emptyList());
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param style the style
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull Style style) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, style);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param style the style
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull StyleBuilderApplicable... style) {
    return translatable(requireNonNull(key, "key"), fallback, Style.style(style));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param style the style
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull Iterable<StyleBuilderApplicable> style) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, Style.style(style));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull ComponentLike@NotNull... args) {
    return translatable(key, fallback, Style.empty(), args);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, args);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull Style style, final @NotNull ComponentLike@NotNull... args) {
    return TranslatableComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), key, fallback, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.13.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull Style style, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, style, args);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull Style style, final @NotNull List<? extends ComponentLike> args) {
    return TranslatableComponentImpl.create(Collections.emptyList(), style, key, fallback, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull Style style, final @NotNull List<? extends ComponentLike> args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, style, args);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param args the translation arguments
   * @param style the style
   * @return a translatable component
   * @since 4.0.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull List<? extends ComponentLike> args, final @NotNull Iterable<StyleBuilderApplicable> style) {
    return TranslatableComponentImpl.create(Collections.emptyList(), Style.style(style), key, fallback, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param args the translation arguments
   * @param style the style
   * @return a translatable component
   * @since 4.8.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull List<? extends ComponentLike> args, final @NotNull Iterable<StyleBuilderApplicable> style) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, args, style);
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param key the translation key
   * @param fallback the fallback string
   * @param args the translation arguments
   * @param style the style
   * @return a translatable component
   * @since 4.0.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable String fallback, final @NotNull List<? extends ComponentLike> args, final @NotNull StyleBuilderApplicable... style) {
    return TranslatableComponentImpl.create(Collections.emptyList(), Style.style(style), key, fallback, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key, optional fallback string, and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param fallback the fallback string
   * @param args the translation arguments
   * @param style the style
   * @return a translatable component
   * @since 4.8.0
   * @sinceMinecraft 1.19.4
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable String fallback, final @NotNull List<? extends ComponentLike> args, final @NotNull StyleBuilderApplicable... style) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), fallback, args, style);
  }

  /**
   * Creates a translatable component with a translation key, and optional color.
   *
   * @param key the translation key
   * @param color the color
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color) {
    return translatable(key, Style.style(color));
  }

  /**
   * Creates a translatable component with a translation key, and optional color.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color);
  }

  /**
   * Creates a translatable component with a translation key, and optional color and decorations.
   *
   * @param key the translation key
   * @param color the color
   * @param decorations the decorations
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return translatable(key, Style.style(color, decorations));
  }

  /**
   * Creates a translatable component with a translation key, and optional color and decorations.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param decorations the decorations
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final TextDecoration@NotNull... decorations) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, decorations);
  }

  /**
   * Creates a translatable component with a translation key, and optional color and decorations.
   *
   * @param key the translation key
   * @param color the color
   * @param decorations the decorations
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return translatable(key, Style.style(color, decorations));
  }

  /**
   * Creates a translatable component with a translation key, and optional color and decorations.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param decorations the decorations
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, decorations);
  }

  /**
   * Creates a translatable component with a translation key and arguments.
   *
   * @param key the translation key
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @NotNull ComponentLike@NotNull... args) {
    return translatable(key, Style.empty(), args);
  }

  /**
   * Creates a translatable component with a translation key and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), args);
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param key the translation key
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @NotNull Style style, final @NotNull ComponentLike@NotNull... args) {
    return TranslatableComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), key, null, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @NotNull Style style, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), style, args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color.
   *
   * @param key the translation key
   * @param color the color
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final @NotNull ComponentLike@NotNull... args) {
    return translatable(key, Style.style(color), args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color and decorations.
   *
   * @param key the translation key
   * @param color the color
   * @param decorations the decorations
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations, final @NotNull ComponentLike@NotNull... args) {
    return translatable(key, Style.style(color, decorations), args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color and decorations.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param decorations the decorations
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations, final @NotNull ComponentLike@NotNull... args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, decorations, args);
  }

  /**
   * Creates a translatable component with a translation key and arguments.
   *
   * @param key the translation key
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @NotNull List<? extends ComponentLike> args) {
    return TranslatableComponentImpl.create(Collections.emptyList(), Style.empty(), key, null, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key and arguments.
   *
   * @param translatable the translatable object to get the key from
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @NotNull List<? extends ComponentLike> args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), args);
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param key the translation key
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @NotNull Style style, final @NotNull List<? extends ComponentLike> args) {
    return TranslatableComponentImpl.create(Collections.emptyList(), requireNonNull(style, "style"), key, null, requireNonNull(args, "args"));
  }

  /**
   * Creates a translatable component with a translation key and styling.
   *
   * @param translatable the translatable object to get the key from
   * @param style the style
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @NotNull Style style, final @NotNull List<? extends ComponentLike> args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), style, args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color.
   *
   * @param key the translation key
   * @param color the color
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final @NotNull List<? extends ComponentLike> args) {
    return translatable(key, Style.style(color), args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  static TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final @NotNull List<? extends ComponentLike> args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color and decorations.
   *
   * @param key the translation key
   * @param color the color
   * @param decorations the decorations
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.0.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull String key, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations, final @NotNull List<? extends ComponentLike> args) {
    return translatable(key, Style.style(color, decorations), args);
  }

  /**
   * Creates a translatable component with a translation key, arguments, and optional color and decorations.
   *
   * @param translatable the translatable object to get the key from
   * @param color the color
   * @param decorations the decorations
   * @param args the translation arguments
   * @return a translatable component
   * @since 4.8.0
   */
  @Contract(value = "_, _, _, _ -> new", pure = true)
  static @NotNull TranslatableComponent translatable(final @NotNull Translatable translatable, final @Nullable TextColor color, final @NotNull Set<TextDecoration> decorations, final @NotNull List<? extends ComponentLike> args) {
    return translatable(requireNonNull(translatable, "translatable").translationKey(), color, decorations, args);
  }

  /**
   * Gets the unmodifiable list of children.
   *
   * @return the unmodifiable list of children
   * @since 4.0.0
   */
  @Unmodifiable @NotNull List<Component> children();

  /**
   * Sets the list of children.
   *
   * <p>The contents of {@code children} will be copied.</p>
   *
   * @param children the children
   * @return a component with the children set
   * @since 4.0.0
   */
  @Contract(pure = true)
  @NotNull Component children(final @NotNull List<? extends ComponentLike> children);

  /**
   * Checks if this component contains a component.
   *
   * <p>This method uses <b>identity</b> comparison when checking for contains. Use {@link #contains(Component, BiPredicate)} with {@link #EQUALS} if you
   * wish to use full equality comparison.</p>
   *
   * @param that the other component
   * @return {@code true} if this component contains the provided
   *     component, {@code false} otherwise
   * @since 4.0.0
   */
  default boolean contains(final @NotNull Component that) {
    return this.contains(that, EQUALS_IDENTITY);
  }

  /**
   * Checks if this component contains a component.
   *
   * @param that the other component
   * @param equals the equality tester
   * @return {@code true} if this component contains the provided
   *     component, {@code false} otherwise
   * @since 4.8.0
   */
  default boolean contains(final @NotNull Component that, final @NotNull BiPredicate<? super Component, ? super Component> equals) {
    if (equals.test(this, that)) return true;
    for (final Component child : this.children()) {
      if (child.contains(that, equals)) return true;
    }
    final @Nullable HoverEvent<?> hoverEvent = this.hoverEvent();
    if (hoverEvent != null) {
      final Object value = hoverEvent.value();
      Component component = null;
      if (value instanceof Component) {
        component = (Component) hoverEvent.value();
      } else if (value instanceof HoverEvent.ShowEntity) {
        component = ((HoverEvent.ShowEntity) value).name();
      }
      if (component != null) {
        if (equals.test(that, component)) return true;
        for (final Component child : component.children()) {
          if (child.contains(that, equals)) return true;
        }
      }
    }
    return false;
  }

  /**
   * Prevents a cycle between this component and the provided component.
   *
   * @param that the other component
   * @since 4.0.0
   * @deprecated for removal since 4.7.0, with no replacement - this method is not necessary due to the fact {@code Component}s are immutable
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  default void detectCycle(final @NotNull Component that) {
    if (that.contains(this)) {
      throw new IllegalStateException("Component cycle detected between " + this + " and " + that);
    }
  }

  /**
   * Appends a component to this component.
   *
   * @param component the component to append
   * @return a component with the component added
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component append(final @NotNull Component component) {
    return this.append((ComponentLike) component);
  }

  /**
   * Appends a component to this component.
   *
   * @param like the component to append
   * @return a component with the component added
   * @since 4.0.0
   */
  default @NotNull Component append(final @NotNull ComponentLike like) {
    requireNonNull(like, "like");
    final Component component = like.asComponent();
    requireNonNull(component, "component");
    if (component == Component.empty()) return this;
    final List<Component> oldChildren = this.children();
    return this.children(MonkeyBars.addOne(oldChildren, component));
  }

  /**
   * Appends a component to this component.
   *
   * @param builder the component to append
   * @return a component with the component added
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component append(final @NotNull ComponentBuilder<?, ?> builder) {
    return this.append(builder.build());
  }

  /**
   * Appends a newline to this component.
   *
   * @return a component with the newline added
   * @since 4.12.0
   */
  @Contract(pure = true)
  default @NotNull Component appendNewline() {
    return this.append(newline());
  }

  /**
   * Appends a space to this component.
   *
   * @return a component with the space added
   * @since 4.12.0
   */
  @Contract(pure = true)
  default @NotNull Component appendSpace() {
    return this.append(space());
  }

  /**
   * Apply a fallback style for this component and its children.
   *
   * <p>This method can be used to set the "default" style for a component, whilst still allowing children of the component to override the style.</p>
   *
   * @param style style to be used as a fallback
   * @return the styled component
   * @since 4.10.0
   */
  @Contract(pure = true)
  default @NotNull Component applyFallbackStyle(final @NotNull Style style) {
    Objects.requireNonNull(style, "style");
    return this.style(this.style().merge(style, Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
  }

  /**
   * Apply a fallback style for this component and its children.
   *
   * <p>This method can be used to set the "default" style for a component, whilst still allowing children of the component to override the style.</p>
   *
   * @param style style to be used as a fallback
   * @return the styled component
   * @since 4.10.0
   */
  @Contract(pure = true)
  default @NotNull Component applyFallbackStyle(final @NotNull StyleBuilderApplicable@NotNull... style) {
    return this.applyFallbackStyle(Style.style(style));
  }

  /**
   * Gets the style of this component.
   *
   * @return the style of this component
   * @since 4.0.0
   */
  @NotNull Style style();

  /**
   * Sets the style of this component.
   *
   * @param style the style
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @NotNull Component style(final @NotNull Style style);

  /**
   * Sets the style of this component.
   *
   * @param consumer the style consumer
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component style(final @NotNull Consumer<Style.Builder> consumer) {
    return this.style(this.style().edit(consumer));
  }

  /**
   * Sets the style of this component.
   *
   * @param consumer the style consumer
   * @param strategy the merge strategy
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component style(final @NotNull Consumer<Style.Builder> consumer, final Style.Merge.@NotNull Strategy strategy) {
    return this.style(this.style().edit(consumer, strategy));
  }

  /**
   * Sets the style of this component.
   *
   * @param style the style
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component style(final Style.@NotNull Builder style) {
    return this.style(style.build());
  }

  /**
   * Merges from another style into this component's style.
   *
   * @param that the other style
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component mergeStyle(final @NotNull Component that) {
    return this.mergeStyle(that, Style.Merge.all());
  }

  /**
   * Merges from another style into this component's style.
   *
   * @param that the other style
   * @param merges the style parts to merge
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component mergeStyle(final @NotNull Component that, final Style.@NotNull Merge@NotNull... merges) {
    return this.mergeStyle(that, Style.Merge.merges(merges));
  }

  /**
   * Merges from another style into this component's style.
   *
   * @param that the other style
   * @param merges the style parts to merge
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  default @NotNull Component mergeStyle(final @NotNull Component that, final @NotNull Set<Style.Merge> merges) {
    return this.style(this.style().merge(that.style(), merges));
  }

  /**
   * Gets the font.
   *
   * @return the font of this component
   * @since 4.10.0
   */
  @Override
  default @Nullable Key font() {
    return this.style().font();
  }

  /**
   * Sets the font.
   *
   * @param key a font
   * @return a component
   * @since 4.10.0
   */
  @Override
  default @NotNull Component font(final @Nullable Key key) {
    return this.style(this.style().font(key));
  }

  /**
   * Gets the color of this component.
   *
   * @return the color of this component
   * @since 4.0.0
   */
  @Override
  default @Nullable TextColor color() {
    return this.style().color();
  }

  @Override
  default @Nullable ShadowColor shadowColor() {
    return this.style().shadowColor();
  }

  /**
   * Sets the color of this component.
   *
   * @param color the color
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component color(final @Nullable TextColor color) {
    return this.style(this.style().color(color));
  }

  /**
   * Sets the color if there isn't one set already.
   *
   * @param color the color
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component colorIfAbsent(final @Nullable TextColor color) {
    if (this.color() == null) return this.color(color);
    return this;
  }

  /**
   * Sets the shadow color of this component.
   *
   * @param argb the color
   * @return a component
   * @since 4.18.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component shadowColor(final @Nullable ARGBLike argb) {
    return this.style(this.style().shadowColor(argb));
  }

  /**
   * Sets the shadow color if there isn't one set already.
   *
   * @param argb the color
   * @return a component
   * @since 4.18.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component shadowColorIfAbsent(final @Nullable ARGBLike argb) {
    if (this.shadowColor() == null) return this.shadowColor(argb);
    return this;
  }

  /**
   * Tests if this component has a decoration.
   *
   * @param decoration the decoration
   * @return {@code true} if this component has the decoration, {@code false} if this
   *     component does not have the decoration
   * @since 4.0.0
   */
  @Override
  default boolean hasDecoration(final @NotNull TextDecoration decoration) {
    return StyleGetter.super.hasDecoration(decoration);
  }

  /**
   * Sets the state of {@code decoration} to {@link TextDecoration.State#TRUE} on this component.
   *
   * @param decoration the decoration
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component decorate(final @NotNull TextDecoration decoration) {
    return StyleSetter.super.decorate(decoration);
  }

  /**
   * Gets the state of a decoration on this component.
   *
   * @param decoration the decoration
   * @return {@link TextDecoration.State#TRUE} if this component has the decoration,
   *     {@link TextDecoration.State#FALSE} if this component does not have the decoration,
   *     and {@link TextDecoration.State#NOT_SET} if not set
   * @since 4.0.0
   */
  @Override
  default TextDecoration.@NotNull State decoration(final @NotNull TextDecoration decoration) {
    return this.style().decoration(decoration);
  }

  /**
   * Sets the state of a decoration on this component.
   *
   * @param decoration the decoration
   * @param flag {@code true} if this component should have the decoration, {@code false} if
   *     this component should not have the decoration
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component decoration(final @NotNull TextDecoration decoration, final boolean flag) {
    return StyleSetter.super.decoration(decoration, flag);
  }

  /**
   * Sets the value of a decoration on this component.
   *
   * @param decoration the decoration
   * @param state {@link TextDecoration.State#TRUE} if this component should have the
   *     decoration, {@link TextDecoration.State#FALSE} if this component should not
   *     have the decoration, and {@link TextDecoration.State#NOT_SET} if the decoration
   *     should not have a set value
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component decoration(final @NotNull TextDecoration decoration, final TextDecoration.@NotNull State state) {
    return this.style(this.style().decoration(decoration, state));
  }

  /**
   * Sets the state of a decoration on this component to {@code state} if the current state of
   * the decoration is {@link TextDecoration.State#NOT_SET}.
   *
   * @param decoration the decoration
   * @param state the state
   * @return a component
   * @since 4.12.0
   */
  @Override
  default @NotNull Component decorationIfAbsent(final @NotNull TextDecoration decoration, final TextDecoration.@NotNull State state) {
    requireNonNull(state, "state");
    // Not delegating this method prevents object creation if decoration is NOT absent
    final TextDecoration.@NotNull State oldState = this.decoration(decoration);
    if (oldState == TextDecoration.State.NOT_SET) {
      return this.style(this.style().decoration(decoration, state));
    }
    return this;
  }

  /**
   * Gets a set of decorations this component has.
   *
   * @return a set of decorations this component has
   * @since 4.0.0
   */
  @Override
  default @NotNull Map<TextDecoration, TextDecoration.State> decorations() {
    return this.style().decorations();
  }

  /**
   * Sets decorations for this component's style using the specified {@code decorations} map.
   *
   * <p>If a given decoration does not have a value explicitly set, the value of that particular decoration is not changed.</p>
   *
   * @param decorations a set of default values
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component decorations(final @NotNull Map<TextDecoration, TextDecoration.State> decorations) {
    return this.style(this.style().decorations(decorations));
  }

  /**
   * Gets the click event of this component.
   *
   * @return the click event
   * @since 4.0.0
   */
  @Override
  default @Nullable ClickEvent clickEvent() {
    return this.style().clickEvent();
  }

  /**
   * Sets the click event of this component.
   *
   * @param event the click event
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component clickEvent(final @Nullable ClickEvent event) {
    return this.style(this.style().clickEvent(event));
  }

  /**
   * Gets the hover event of this component.
   *
   * @return the hover event
   * @since 4.0.0
   */
  @Override
  default @Nullable HoverEvent<?> hoverEvent() {
    return this.style().hoverEvent();
  }

  /**
   * Sets the hover event of this component.
   *
   * @param source the hover event source
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component hoverEvent(final @Nullable HoverEventSource<?> source) {
    return this.style(this.style().hoverEvent(source));
  }

  /**
   * Gets the string to be inserted when this component is shift-clicked.
   *
   * @return the insertion string
   * @since 4.0.0
   */
  @Override
  default @Nullable String insertion() {
    return this.style().insertion();
  }

  /**
   * Sets the string to be inserted when this component is shift-clicked.
   *
   * @param insertion the insertion string
   * @return a component
   * @since 4.0.0
   */
  @Contract(pure = true)
  @Override
  default @NotNull Component insertion(final @Nullable String insertion) {
    return this.style(this.style().insertion(insertion));
  }

  /**
   * Tests if this component has any styling.
   *
   * @return {@code true} if this component has any styling, {@code false} if this
   *     component does not have any styling
   * @since 4.0.0
   */
  default boolean hasStyling() {
    return !this.style().isEmpty();
  }

  /**
   * Finds and replaces any text with this or child {@link Component}s using the configured options.
   *
   * @param configurer the configurer
   * @return a modified copy of this component
   * @since 4.2.0
   */
  @Contract(pure = true)
  default @NotNull Component replaceText(final @NotNull Consumer<TextReplacementConfig.Builder> configurer) {
    requireNonNull(configurer, "configurer");
    return this.replaceText(AbstractBuilder.configureAndBuild(TextReplacementConfig.builder(), configurer));
  }

  /**
   * Finds and replaces any text with this or child {@link Component}s using the provided options.
   *
   * @param config the replacement config
   * @return a modified copy of this component
   * @since 4.2.0
   */
  @Contract(pure = true)
  default @NotNull Component replaceText(final @NotNull TextReplacementConfig config) {
    requireNonNull(config, "replacement");
    if (!(config instanceof TextReplacementConfigImpl)) {
      throw new IllegalArgumentException("Provided replacement was a custom TextReplacementConfig implementation, which is not supported.");
    }
    return TextReplacementRenderer.INSTANCE.render(this, ((TextReplacementConfigImpl) config).createState());
  }

  /**
   * Create a new component with any redundant style elements or children removed.
   *
   * @return the optimized component
   * @since 4.9.0
   */
  default @NotNull Component compact() {
    return ComponentCompaction.compact(this, null);
  }

  /**
   * Returns an iterable view of this component.
   *
   * @param type the type
   * @param flags the flags
   * @return the iterable
   * @since 4.9.0
   */
  default @NotNull Iterable<Component> iterable(final @NotNull ComponentIteratorType type, final @NotNull ComponentIteratorFlag@Nullable... flags) {
    return this.iterable(type, flags == null ? Collections.emptySet() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
  }

  /**
   * Returns an iterable view of this component.
   *
   * @param type the type
   * @param flags the flags
   * @return the iterable
   * @since 4.9.0
   */
  default @NotNull Iterable<Component> iterable(final @NotNull ComponentIteratorType type, final @NotNull Set<ComponentIteratorFlag> flags) {
    requireNonNull(type, "type");
    requireNonNull(flags, "flags");
    return new ForwardingIterator<>(() -> this.iterator(type, flags), () -> this.spliterator(type, flags));
  }

  /**
   * Returns an iterator for this component.
   *
   * <p>As components are immutable, this iterator does not support removal.</p>
   *
   * @param type the type
   * @param flags the flags
   * @return the iterator
   * @since 4.9.0
   */
  default @NotNull Iterator<Component> iterator(final @NotNull ComponentIteratorType type, final @NotNull ComponentIteratorFlag@Nullable... flags) {
    return this.iterator(type, flags == null ? Collections.emptySet() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
  }

  /**
   * Returns an iterator for this component.
   *
   * <p>As components are immutable, this iterator does not support removal.</p>
   *
   * @param type the type
   * @param flags the flags
   * @return the iterator
   * @since 4.9.0
   */
  default @NotNull Iterator<Component> iterator(final @NotNull ComponentIteratorType type, final @NotNull Set<ComponentIteratorFlag> flags) {
    return new ComponentIterator(this, requireNonNull(type, "type"), requireNonNull(flags, "flags"));
  }

  /**
   * Returns a spliterator for this component.
   *
   * <p>The resulting spliterator has the {@link Spliterator#IMMUTABLE}, {@link Spliterator#NONNULL} and {@link Spliterator#ORDERED} characteristics.</p>
   *
   * @param type the type
   * @param flags the flags
   * @return the spliterator
   * @since 4.9.0
   */
  default @NotNull Spliterator<Component> spliterator(final @NotNull ComponentIteratorType type, final @NotNull ComponentIteratorFlag@Nullable... flags) {
    return this.spliterator(type, flags == null ? Collections.emptySet() : MonkeyBars.enumSet(ComponentIteratorFlag.class, flags));
  }

  /**
   * Returns a spliterator for this component.
   *
   * <p>The resulting spliterator has the {@link Spliterator#IMMUTABLE}, {@link Spliterator#NONNULL} and {@link Spliterator#ORDERED} characteristics.</p>
   *
   * @param type the type
   * @param flags the flags
   * @return the spliterator
   * @since 4.9.0
   */
  default @NotNull Spliterator<Component> spliterator(final @NotNull ComponentIteratorType type, final @NotNull Set<ComponentIteratorFlag> flags) {
    return Spliterators.spliteratorUnknownSize(this.iterator(type, flags), Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED);
  }

  /**
   * Finds and replaces text within any {@link Component}s using a string literal.
   *
   * @param search a string literal
   * @param replacement a {@link ComponentLike} to replace each match
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull String search, final @Nullable ComponentLike replacement) {
    return this.replaceText(b -> b.matchLiteral(search).replacement(replacement));
  }

  /**
   * Finds and replaces text within any {@link TextComponent}s using a regex pattern.
   *
   * @param pattern a regex pattern
   * @param replacement a function to replace each match
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull Pattern pattern, final @NotNull Function<TextComponent.Builder, @Nullable ComponentLike> replacement) {
    return this.replaceText(b -> b.match(pattern).replacement(replacement));
  }

  /**
   * Finds and replaces the first occurrence of text within any {@link Component}s using a string literal.
   *
   * @param search a string literal
   * @param replacement a {@link ComponentLike} to replace the first match
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceFirstText(final @NotNull String search, final @Nullable ComponentLike replacement) {
    return this.replaceText(b -> b.matchLiteral(search).once().replacement(replacement));
  }

  /**
   * Finds and replaces the first occurrence of text within any {@link TextComponent}s using a regex pattern.
   *
   * @param pattern a regex pattern
   * @param replacement a function to replace the first match
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceFirstText(final @NotNull Pattern pattern, final @NotNull Function<TextComponent.Builder, @Nullable ComponentLike> replacement) {
    return this.replaceText(b -> b.match(pattern).once().replacement(replacement));
  }

  /**
   * Finds and replaces {@code n} instances of text within any {@link TextComponent}s using a string literal.
   *
   * @param search a string literal
   * @param replacement a {@link ComponentLike} to replace the first match
   * @param numberOfReplacements the amount of matches that should be replaced
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull String search, final @Nullable ComponentLike replacement, final int numberOfReplacements) {
    return this.replaceText(b -> b.matchLiteral(search).times(numberOfReplacements).replacement(replacement));
  }

  /**
   * Finds and replaces {@code n} instances of text within any {@link TextComponent}s using a regex pattern.
   *
   * @param pattern a regex pattern
   * @param replacement a function to replace each match
   * @param numberOfReplacements the amount of matches that should be replaced
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull Pattern pattern, final @NotNull Function<TextComponent.Builder, @Nullable ComponentLike> replacement, final int numberOfReplacements) {
    return this.replaceText(b -> b.match(pattern).times(numberOfReplacements).replacement(replacement));
  }

  /**
   * Finds and replaces {@code n} instances of text within any {@link TextComponent}s using a string literal.
   *
   * <p>Utilises an {@link IntFunction2} to determine if each instance should be replaced.</p>
   *
   * @param search a string literal
   * @param replacement a {@link ComponentLike} to replace the first match
   * @param fn a function of (index, replaced) used to determine if matches should be replaced, where "replaced" is the number of successful replacements
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull String search, final @Nullable ComponentLike replacement, final @NotNull IntFunction2<PatternReplacementResult> fn) {
    return this.replaceText(b -> b.matchLiteral(search).replacement(replacement).condition(fn));
  }

  /**
   * Finds and replaces text using a regex pattern.
   *
   * <p>Utilises an {@link IntFunction2} to determine if each instance should be replaced.</p>
   *
   * @param pattern a regex pattern
   * @param replacement a function to replace the first match
   * @param fn a function of (index, replaced) used to determine if matches should be replaced, where "replaced" is the number of successful replacements
   * @return a modified copy of this component
   * @since 4.0.0
   * @deprecated for removal since 4.2.0, use {@link #replaceText(Consumer)} or {@link #replaceText(TextReplacementConfig)} instead.
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Contract(pure = true)
  @Deprecated
  default @NotNull Component replaceText(final @NotNull Pattern pattern, final @NotNull Function<TextComponent.Builder, @Nullable ComponentLike> replacement, final @NotNull IntFunction2<PatternReplacementResult> fn) {
    return this.replaceText(b -> b.match(pattern).replacement(replacement).condition(fn));
  }

  @Override
  default void componentBuilderApply(final @NotNull ComponentBuilder<?, ?> component) {
    component.append(this);
  }

  @Override
  default @NotNull Component asComponent() {
    return this;
  }

  @Override
  default @NotNull HoverEvent<Component> asHoverEvent(final @NotNull UnaryOperator<Component> op) {
    return HoverEvent.showText(op.apply(this));
  }

  @Override
  default @NotNull Stream<? extends ExaminableProperty> examinableProperties() {
    return Stream.of(
      ExaminableProperty.of("style", this.style()),
      ExaminableProperty.of(ComponentInternals.CHILDREN_PROPERTY, this.children())
    );
  }
}
