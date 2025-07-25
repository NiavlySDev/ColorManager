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
package net.kyori.adventure.key;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * A {@code T} value with an associated {@link Key}.
 *
 * @param <T> the value type
 * @since 4.0.0
 */
public interface KeyedValue<T> extends Keyed {
  /**
   * Creates a link.
   *
   * @param key the key
   * @param value the value
   * @param <T> the value type
   * @return the keyed
   * @since 4.10.0
   */
  static <T> @NotNull KeyedValue<T> keyedValue(final @NotNull Key key, final @NotNull T value) {
    return new KeyedValueImpl<>(key, requireNonNull(value, "value"));
  }

  /**
   * Creates a link.
   *
   * @param key the key
   * @param value the value
   * @param <T> the value type
   * @return the keyed
   * @since 4.0.0
   * @deprecated for removal since 4.10.0, use {@link #keyedValue(Key, Object)} instead.
   */
  @Deprecated
  @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
  static <T> @NotNull KeyedValue<T> of(final @NotNull Key key, final @NotNull T value) {
    return new KeyedValueImpl<>(key, requireNonNull(value, "value"));
  }

  /**
   * Gets the value.
   *
   * @return the value
   * @since 4.0.0
   */
  @NotNull T value();
}
