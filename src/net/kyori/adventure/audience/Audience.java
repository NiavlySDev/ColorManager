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
package net.kyori.adventure.audience;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackRequestLike;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * A receiver of Minecraft media.
 *
 * <p><code>Audience</code> is designed to be a universal interface for any player,
 * command sender, console, or otherwise who can receive text, titles,
 * boss bars, and other Minecraft media. It is also designed for a group of
 * receivers such as a team, server, world, or permission.</p>
 *
 * <p>In the past, Minecraft platforms have typically reserved methods such as
 * <code>showTitle</code> for a <code>Player</code> interface. While this is good
 * textbook object-oriented design, it presents two key drawbacks: 1) there
 * is no abstraction for groups of players, such as a <code>Server</code> or a
 * <code>Team</code> and 2) it add boilerplate for handling special cases like
 * console or command senders.</p>
 *
 * <p>Consider the use-case of sending a message and title to every player on a
 * server, and also sending a message to console. Without an <code>Audience</code>,
 * the code might look like this:</p>
 * <pre>
 *   Server server;
 *   for (Player player : server.getPlayers()) {
 *     player.sendMessage(...);
 *     player.showTitle(...);
 *   }
 *   server.getConsole().sendMessage(...);</pre>
 *
 * <p>Now, if <code>Server</code> implemented <code>Audience</code>, its unified interface
 * would allow users to easily send media without if-guarding console or
 * iterating through the list of players:</p>
 * <pre>
 *   Server server;
 *   server.sendMessage(...); // Sends a message to players and console
 *   server.showTitle(...); // Shows a title to players, silently ignored by console</pre>
 *
 * <p>When an <code>Audience</code> is unable to perform an operation, such as sending
 * a boss bar to console, it will silently fail, without logging. This
 * requirement allows users to easily send media to a group of
 * <code>Audience</code>s without checking each for compatibility.</p>
 *
 * <p>While the scope of <code>Audience</code> may be expanded in the future to support
 * new Minecraft media such as the player list, its interface will remain stateless
 * and any new methods will be stubbed by default.</p>
 *
 * @see ForwardingAudience
 * @see BossBarViewer
 * @since 4.0.0
 */
public interface Audience extends Pointered {
  /**
   * Gets an audience that does nothing.
   *
   * @return a do-nothing audience
   * @since 4.0.0
   */
  static @NotNull Audience empty() {
    return EmptyAudience.INSTANCE;
  }

  /**
   * Creates an audience that forwards to many other audiences.
   *
   * @param audiences an array of audiences, can be empty
   * @return an audience
   * @see ForwardingAudience
   * @since 4.0.0
   */
  static @NotNull Audience audience(final @NotNull Audience@NotNull... audiences) {
    final int length = audiences.length;
    if (length == 0) {
      return empty();
    } else if (length == 1) {
      return audiences[0];
    }
    return audience(Arrays.asList(audiences));
  }

  /**
   * Creates an audience that forwards to many other audiences.
   *
   * <p>The underlying <code>Iterable</code> is not copied, therefore any changes
   * made will be reflected in <code>Audience</code>.</p>
   *
   * @param audiences an iterable of audiences, can be empty
   * @return an audience
   * @see ForwardingAudience
   * @since 4.0.0
   */
  static @NotNull ForwardingAudience audience(final @NotNull Iterable<? extends Audience> audiences) {
    return () -> audiences;
  }

  /**
   * Provides a collector to create a forwarding audience from a stream of audiences.
   *
   * <p>The audience produced is immutable and can be reused as desired.</p>
   *
   * @return a collector to create a forwarding audience
   * @since 4.0.0
   */
  static @NotNull Collector<? super Audience, ?, ForwardingAudience> toAudience() {
    return Audiences.COLLECTOR;
  }

  /**
   * Filters this audience.
   *
   * <p>The returned {@code Audience} may be the same, or a completely different one.</p>
   *
   * <p>Container audiences such as {@link ForwardingAudience} may or may not have their own identity.
   * If they do, they <em>may</em> test themselves against the provided {@code filter} first, and if the test fails return an empty audience skipping any contained children.
   * If they do not, they <em>must not</em> test themselves against the filter, only testing their children.</p>
   *
   * @param filter a filter that determines if an audience should be included
   * @return an audience providing a snapshot of all audiences that match the predicate when this method is invoked
   * @since 4.9.0
   */
  default @NotNull Audience filterAudience(final @NotNull Predicate<? super Audience> filter) {
    return filter.test(this)
      ? this
      : empty();
  }

  /**
   * Executes an action against all audiences.
   *
   * <p>If you implement {@code Audience} and not {@link ForwardingAudience} in your own code, and your audience forwards to
   * other audiences, then you <b>must</b> override this method and provide each audience to {@code action}.</p>
   *
   * <p>If an implementation of {@code Audience} has its own identity distinct from its contained children, it <em>may</em> test
   * itself against the provided {@code filter} first, and  if the test fails return an empty audience skipping any contained children.
   * If it does not, it <em>must not</em> test itself against the filter, only testing its children.</p>
   *
   * @param action the action
   * @since 4.9.0
   */
  default void forEachAudience(final @NotNull Consumer<? super Audience> action) {
    action.accept(this);
  }

  /* Start: system messages */
  /**
   * Sends a system chat message to this {@link Audience}.
   *
   * @param message a message
   * @see Component
   * @see #sendMessage(Identified, ComponentLike)
   * @see #sendMessage(Identity, ComponentLike)
   * @since 4.1.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull ComponentLike message) {
    this.sendMessage(message.asComponent());
  }

  /**
   * Sends a system chat message to this {@link Audience}.
   *
   * @param message a message
   * @see Component
   * @see #sendMessage(Identified, Component)
   * @see #sendMessage(Identity, Component)
   * @since 4.1.0
   */
  @SuppressWarnings("deprecation")
  default void sendMessage(final @NotNull Component message) {
    this.sendMessage(message, MessageType.SYSTEM);
  }

  /**
   * Sends a system chat message to this {@link Audience} ignoring the provided {@link MessageType}.
   *
   * @param message a message
   * @param type the type
   * @see Component
   * @see #sendMessage(Identified, ComponentLike, MessageType)
   * @see #sendMessage(Identity, ComponentLike, MessageType)
   * @since 4.1.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal, use {@link #sendMessage(ComponentLike)}
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull ComponentLike message, final @NotNull MessageType type) {
    this.sendMessage(message.asComponent(), type);
  }

  /**
   * Sends a system chat message to this {@link Audience} ignoring the provided {@link MessageType}.
   *
   * @param message a message
   * @param type the type
   * @see Component
   * @see #sendMessage(Identified, Component, MessageType)
   * @see #sendMessage(Identity, Component, MessageType)
   * @since 4.1.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal, use {@link #sendMessage(Component)} instead
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Component message, final @NotNull MessageType type) {
    this.sendMessage(Identity.nil(), message, type);
  }
  /* End: system messages */

  /* Start: unsigned player messages */
  /**
   * Sends an unsigned player chat message from the given {@link Identified} to this {@link Audience} with the {@link ChatType#CHAT system} chat type.
   *
   * @param source the source of the message
   * @param message a message
   * @see Component
   * @since 4.0.0
   * @deprecated since 4.12.0, the client errors on and can reject identified messages without {@link SignedMessage} data, this may be unsupported in the future, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identified source, final @NotNull ComponentLike message) {
    this.sendMessage(source, message.asComponent());
  }

  /**
   * Sends an unsigned player chat message from the entity represented by the given {@link Identity} to this {@link Audience} with the {@link ChatType#CHAT system} chat type.
   *
   * @param source the identity of the source of the message
   * @param message a message
   * @see Component
   * @since 4.0.0
   * @deprecated since 4.12.0, the client errors on and can reject identified messages without {@link SignedMessage} data, this may be unsupported in the future, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identity source, final @NotNull ComponentLike message) {
    this.sendMessage(source, message.asComponent());
  }

  /**
   * Sends an unsigned player chat message from the given {@link Identified} to this {@link Audience} with the {@link ChatType#CHAT system} chat type.
   *
   * @param source the source of the message
   * @param message a message
   * @see Component
   * @since 4.0.0
   * @deprecated since 4.12.0, the client errors on receiving and can reject identified messages without {@link SignedMessage} data, this may be unsupported in the future, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identified source, final @NotNull Component message) {
    this.sendMessage(source, message, MessageType.CHAT);
  }

  /**
   * Sends an unsigned player chat message from the entity represented by the given {@link Identity} to this {@link Audience} with the {@link ChatType#CHAT system} chat type.
   *
   * @param source the identity of the source of the message
   * @param message a message
   * @see Component
   * @since 4.0.0
   * @deprecated since 4.12.0, the client errors on receiving and can reject identified messages without {@link SignedMessage} data, this may be unsupported in the future, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identity source, final @NotNull Component message) {
    this.sendMessage(source, message, MessageType.CHAT);
  }

  /**
   * Sends an unsigned player chat message from the given {@link Identified} to this {@link Audience} with the {@link ChatType} corresponding to the provided {@link MessageType}.
   *
   * @param source the source of the message
   * @param message a message
   * @param type the type
   * @see Component
   * @since 4.0.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal and the client errors on receiving and can reject identified messages without {@link SignedMessage} data, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identified source, final @NotNull ComponentLike message, final @NotNull MessageType type) {
    this.sendMessage(source, message.asComponent(), type);
  }

  /**
   * Sends an unsigned player chat message from the entity represented by the given {@link Identity} to this {@link Audience}.
   *
   * @param source the identity of the source of the message
   * @param message a message
   * @param type the type
   * @see Component
   * @since 4.0.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal and the client errors on receiving and can reject identified messages without {@link SignedMessage} data, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull Identity source, final @NotNull ComponentLike message, final @NotNull MessageType type) {
    this.sendMessage(source, message.asComponent(), type);
  }

  /**
   * Sends an unsigned player chat message from the given {@link Identified} to this {@link Audience} with the {@link ChatType} corresponding to the provided {@link MessageType}.
   *
   * @param source the source of the message
   * @param message a message
   * @param type the type
   * @see Component
   * @since 4.0.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal and the client errors on receiving and can reject identified messages without {@link SignedMessage} data, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  default void sendMessage(final @NotNull Identified source, final @NotNull Component message, final @NotNull MessageType type) {
    this.sendMessage(source.identity(), message, type);
  }

  /**
   * Sends a player chat message from the entity represented by the given {@link Identity} to this {@link Audience} with the {@link ChatType} corresponding to the provided {@link MessageType}.
   *
   * @param source the identity of the source of the message
   * @param message a message
   * @param type the type
   * @see Component
   * @since 4.0.0
   * @deprecated for removal since 4.12.0, {@link MessageType} is deprecated for removal and the client errors on receiving and can reject identified messages without {@link SignedMessage} data, use {@link #sendMessage(SignedMessage, ChatType.Bound)} instead
   */
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  @Deprecated
  default void sendMessage(final @NotNull Identity source, final @NotNull Component message, final @NotNull MessageType type) {
    // implementation required
  }
  /* End: unsigned player messages */

  /* Start: disguised player messages */
  /**
   * Sends a message to this {@link Audience} with the provided {@link ChatType.Bound bound chat type}.
   *
   * @param message the component content
   * @param boundChatType the bound chat type
   * @since 4.12.0
   * @sinceMinecraft 1.19
   */
  @SuppressWarnings("deprecation")
  default void sendMessage(final @NotNull Component message, final ChatType.@NotNull Bound boundChatType) {
    this.sendMessage(message, MessageType.CHAT);
  }

  /**
   * Sends a message to this {@link Audience} with the provided {@link ChatType.Bound bound chat type}.
   *
   * @param message the component content
   * @param boundChatType the bound chat type
   * @since 4.12.0
   * @sinceMinecraft 1.19
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendMessage(final @NotNull ComponentLike message, final ChatType.@NotNull Bound boundChatType) {
    this.sendMessage(message.asComponent(), boundChatType);
  }
  /* End: disguised player messages

  /* Start: signed player messages */
  /**
   * Sends a signed player message to this {@link Audience} with the provided {@link ChatType.Bound bound chat type}.
   *
   * @param signedMessage the signed message data
   * @param boundChatType the bound chat type
   * @since 4.12.0
   * @sinceMinecraft 1.19
   */
  @SuppressWarnings("deprecation")
  default void sendMessage(final @NotNull SignedMessage signedMessage, final ChatType.@NotNull Bound boundChatType) {
    final Component content = signedMessage.unsignedContent() != null ? signedMessage.unsignedContent() : Component.text(signedMessage.message());
    if (signedMessage.isSystem()) {
      this.sendMessage(content);
    } else {
      this.sendMessage(signedMessage.identity(), content, MessageType.CHAT);
    }
  }

  /**
   * Requests deletion of a message with the provided {@link SignedMessage}'s signature.
   *
   * @param signedMessage the message to delete
   * @see SignedMessage#canDelete()
   * @since 4.12.0
   * @sinceMinecraft 1.19
   */
  @ForwardingAudienceOverrideNotRequired
  default void deleteMessage(final @NotNull SignedMessage signedMessage) {
    if (signedMessage.canDelete()) {
      this.deleteMessage(Objects.requireNonNull(signedMessage.signature()));
    }
  }

  /**
   * Requests deletion of a message with the provided {@link SignedMessage.Signature}.
   *
   * @param signature the signature
   * @since 4.12.0
   * @sinceMinecraft 1.19
   */
  default void deleteMessage(final SignedMessage.@NotNull Signature signature) {
  }
  /* End: signed player messages */

  /**
   * Sends a message on the action bar.
   *
   * @param message a message
   * @see Component
   * @since 4.0.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendActionBar(final @NotNull ComponentLike message) {
    this.sendActionBar(message.asComponent());
  }

  /**
   * Sends a message on the action bar.
   *
   * @param message a message
   * @see Component
   * @since 4.0.0
   */
  default void sendActionBar(final @NotNull Component message) {
  }

  /**
   * Sends the player list header.
   *
   * <p>Depending on the implementation of this {@code Audience}, an existing footer may be displayed. If you wish
   * to set both the header and the footer, please use {@link #sendPlayerListHeaderAndFooter(ComponentLike, ComponentLike)}.</p>
   *
   * @param header the header
   * @since 4.3.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendPlayerListHeader(final @NotNull ComponentLike header) {
    this.sendPlayerListHeader(header.asComponent());
  }

  /**
   * Sends the player list header.
   *
   * <p>Depending on the implementation of this {@code Audience}, an existing footer may be displayed. If you wish
   * to set both the header and the footer, please use {@link #sendPlayerListHeaderAndFooter(Component, Component)}.</p>
   *
   * @param header the header
   * @since 4.3.0
   */
  default void sendPlayerListHeader(final @NotNull Component header) {
    this.sendPlayerListHeaderAndFooter(header, Component.empty());
  }

  /**
   * Sends the player list footer.
   *
   * <p>Depending on the implementation of this {@code Audience}, an existing footer may be displayed. If you wish
   * to set both the header and the footer, please use {@link #sendPlayerListHeaderAndFooter(ComponentLike, ComponentLike)}.</p>
   *
   * @param footer the footer
   * @since 4.3.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendPlayerListFooter(final @NotNull ComponentLike footer) {
    this.sendPlayerListFooter(footer.asComponent());
  }

  /**
   * Sends the player list footer.
   *
   * <p>Depending on the implementation of this {@code Audience}, an existing footer may be displayed. If you wish
   * to set both the header and the footer, please use {@link #sendPlayerListHeaderAndFooter(Component, Component)}.</p>
   *
   * @param footer the footer
   * @since 4.3.0
   */
  default void sendPlayerListFooter(final @NotNull Component footer) {
    this.sendPlayerListHeaderAndFooter(Component.empty(), footer);
  }

  /**
   * Sends the player list header and footer.
   *
   * @param header the header
   * @param footer the footer
   * @since 4.3.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendPlayerListHeaderAndFooter(final @NotNull ComponentLike header, final @NotNull ComponentLike footer) {
    this.sendPlayerListHeaderAndFooter(header.asComponent(), footer.asComponent());
  }

  /**
   * Sends the player list header and footer.
   *
   * @param header the header
   * @param footer the footer
   * @since 4.3.0
   */
  default void sendPlayerListHeaderAndFooter(final @NotNull Component header, final @NotNull Component footer) {
  }

  /**
   * Shows a title.
   *
   * @param title a title
   * @see Title
   * @since 4.0.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void showTitle(final @NotNull Title title) {
    final Title.Times times = title.times();
    if (times != null) this.sendTitlePart(TitlePart.TIMES, times);

    this.sendTitlePart(TitlePart.SUBTITLE, title.subtitle());
    this.sendTitlePart(TitlePart.TITLE, title.title());
  }

  /**
   * Shows a part of a title.
   *
   * @param part the part
   * @param value the value
   * @param <T> the type of the value of the part
   * @throws IllegalArgumentException if a title part that is not in {@link TitlePart} is used
   * @since 4.9.0
   */
  default <T> void sendTitlePart(final @NotNull TitlePart<T> part, final @NotNull T value) {
  }

  /**
   * Clears the title, if one is being displayed.
   *
   * @see Title
   * @since 4.0.0
   */
  default void clearTitle() {
  }

  /**
   * Resets the title and timings back to their default.
   *
   * @see Title
   * @since 4.0.0
   */
  default void resetTitle() {
  }

  /**
   * Shows a boss bar.
   *
   * @param bar a boss bar
   * @see BossBar
   * @since 4.0.0
   */
  default void showBossBar(final @NotNull BossBar bar) {
  }

  /**
   * Hides a boss bar.
   *
   * @param bar a boss bar
   * @see BossBar
   * @since 4.0.0
   */
  default void hideBossBar(final @NotNull BossBar bar) {
  }

  /**
   * Plays a sound at the location of the recipient of the sound.
   *
   * <p>To play a sound that follows the recipient, use {@link #playSound(Sound, Sound.Emitter)} with {@link Sound.Emitter#self()}.</p>
   *
   * @param sound a sound
   * @see Sound
   * @since 4.0.0
   */
  default void playSound(final @NotNull Sound sound) {
  }

  /**
   * Plays a sound at a location.
   *
   * @param sound a sound
   * @param x x coordinate
   * @param y y coordinate
   * @param z z coordinate
   * @see Sound
   * @since 4.0.0
   */
  default void playSound(final @NotNull Sound sound, final double x, final double y, final double z) {
  }

  /**
   * Plays a sound from an emitter, usually an entity.
   *
   * <p>
   *   Sounds played using this method will follow the emitter unless the sound is a custom sound.
   *   In this case the sound will be played at the location of the emitter and will not follow them.
   * </p>
   *
   * <p>To play a sound that follows the recipient, use {@link Sound.Emitter#self()}.</p>
   *
   * <p><b>Note</b>: Due to <a href="https://bugs.mojang.com/browse/MC-138832">MC-138832</a>, the volume and pitch may be ignored when using this method.</p>
   *
   * @param sound a sound
   * @param emitter an emitter
   * @since 4.8.0
   */
  default void playSound(final @NotNull Sound sound, final Sound.@NotNull Emitter emitter) {
  }

  /**
   * Stops a sound.
   *
   * @param sound the sound
   * @since 4.8.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void stopSound(final @NotNull Sound sound) {
    this.stopSound(Objects.requireNonNull(sound, "sound").asStop());
  }

  /**
   * Stops a sound, or many sounds.
   *
   * @param stop a sound stop
   * @see SoundStop
   * @since 4.0.0
   */
  default void stopSound(final @NotNull SoundStop stop) {
  }

  /**
   * Opens a book.
   *
   * <p>When possible, no item should persist after closing the book.</p>
   *
   * @param book a book
   * @see Book
   * @since 4.0.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void openBook(final Book.@NotNull Builder book) {
    this.openBook(book.build());
  }

  /**
   * Opens a book.
   *
   * <p>When possible, no item should persist after closing the book.</p>
   *
   * @param book a book
   * @see Book
   * @since 4.0.0
   */
  default void openBook(final @NotNull Book book) {
  }

  // ------------------------
  // ---- Resource Packs ----
  // ------------------------

  /**
   * Sends a request to apply resource packs to this audience.
   *
   * <p>Multiple resource packs are only supported since 1.20.3. On older versions, all requests behave as if {@link ResourcePackRequest#replace()} is set to {@code true}.</p>
   *
   * @param first the resource pack info
   * @param others the other pack infos
   * @see ResourcePackRequest#addingRequest(ResourcePackInfoLike, ResourcePackInfoLike...)
   * @since 4.15.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendResourcePacks(final @NotNull ResourcePackInfoLike first, final @NotNull ResourcePackInfoLike... others) {
    this.sendResourcePacks(ResourcePackRequest.addingRequest(first, others));
  }

  /**
   * Sends a request to apply resource packs to this audience.
   *
   * <p>Multiple resource packs are only supported since 1.20.3. On older versions, all requests behave as if {@link ResourcePackRequest#replace()} is set to {@code true}.</p>
   *
   * @param request the resource pack request
   * @see ResourcePackInfo
   * @since 4.15.0
   */
  @ForwardingAudienceOverrideNotRequired
  default void sendResourcePacks(final @NotNull ResourcePackRequestLike request) {
    this.sendResourcePacks(request.asResourcePackRequest());
  }

  /**
   * Sends a request to apply resource packs to this audience.
   *
   * <p>Multiple resource packs are only supported since 1.20.3. On older versions, all requests behave as if {@link ResourcePackRequest#replace()} is set to {@code true}.</p>
   *
   * @param request the resource pack request
   * @see ResourcePackInfo
   * @since 4.15.0
   */
  default void sendResourcePacks(final @NotNull ResourcePackRequest request) {
  }

  /**
   * Clear resource packs with the IDs used in the provided requests if they are present.
   *
   * @param request the request used to originally apply the packs
   * @since 4.15.0
   * @sinceMinecraft 1.20.3
   */
  @ForwardingAudienceOverrideNotRequired
  default void removeResourcePacks(final @NotNull ResourcePackRequestLike request) {
    this.removeResourcePacks(request.asResourcePackRequest());
  }

  /**
   * Clear resource packs with the IDs used in the provided requests if they are present.
   *
   * @param request the request used to originally apply the packs
   * @since 4.15.0
   * @sinceMinecraft 1.20.3
   */
  @ForwardingAudienceOverrideNotRequired
  default void removeResourcePacks(final @NotNull ResourcePackRequest request) {
    final List<ResourcePackInfo> infos = request.packs();
    if (infos.size() == 1) {
      this.removeResourcePacks(infos.get(0).id());
    } else if (infos.isEmpty()) {
      return;
    }

    final UUID[] otherReqs = new UUID[infos.size() - 1];
    for (int i = 0; i < otherReqs.length; i++) {
      otherReqs[i] = infos.get(i + 1).id();
    }
    this.removeResourcePacks(infos.get(0).id(), otherReqs);
  }

  /**
   * Clear resource packs with the IDs used in the provided requests if they are present.
   *
   * @param request the first request used to originally apply the pack
   * @param others requests for other packs that should be removed
   * @since 4.15.0
   * @sinceMinecraft 1.20.3
   */
  @ForwardingAudienceOverrideNotRequired
  default void removeResourcePacks(final @NotNull ResourcePackInfoLike request, final @NotNull ResourcePackInfoLike @NotNull... others) {
    final UUID[] otherReqs = new UUID[others.length];
    for (int i = 0; i < others.length; i++) {
      otherReqs[i] = others[i].asResourcePackInfo().id();
    }
    this.removeResourcePacks(request.asResourcePackInfo().id(), otherReqs);
  }

  /**
   * Clear resource packs with the provided ids if they are present.
   *
   * @param ids the ids of resource packs to remove
   * @since 4.16.0
   * @sinceMinecraft 1.20.3
   */
  default void removeResourcePacks(final @NotNull Iterable<UUID> ids) {
    // break these out to id + arrays
    final Iterator<UUID> it = ids.iterator();
    if (!it.hasNext()) return;

    final UUID id = it.next();
    final UUID[] others;
    if (!it.hasNext()) {
      others = new UUID[0];
    } else if (ids instanceof Collection<?>) {
      others = new UUID[((Collection<UUID>) ids).size() - 1];
      for (int i = 0; i < others.length; i++) {
        others[i] = it.next();
      }
    } else {
      final List<UUID> othersList = new ArrayList<>();
      while (it.hasNext()) {
        othersList.add(it.next());
      }
      others = othersList.toArray(new UUID[0]);
    }

    this.removeResourcePacks(id, others);
  }

  /**
   * Clear resource packs with the provided ids if they are present.
   *
   * @param id the id
   * @param others the ids of any additional resource packs
   * @since 4.15.0
   * @sinceMinecraft 1.20.3
   */
  default void removeResourcePacks(final @NotNull UUID id, final @NotNull UUID@NotNull... others) {
  }

  /**
   * Clear all server-provided resource packs that have been sent to this user.
   *
   * @since 4.15.0
   */
  default void clearResourcePacks() {
  }
}
