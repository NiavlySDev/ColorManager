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
package net.kyori.adventure.builder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A builder.
 *
 * @param <R> the type to be built
 * @since 4.10.0
 */
@FunctionalInterface
public interface AbstractBuilder<R> {
  /**
   * Configures {@code builder} using {@code consumer} and then builds.
   *
   * @param builder the builder
   * @param consumer the builder consume
   * @param <R> the type to be built
   * @param <B> the builder type
   * @return the built thing
   * @since 4.10.0
   */
  @Contract(mutates = "param1")
  static <R, B extends AbstractBuilder<R>> @NotNull R configureAndBuild(final @NotNull B builder, final @Nullable Consumer<? super B> consumer) {
    if (consumer != null) {
      consumer.accept(builder);
    }
    return builder.build();
  }

  /**
   * Builds.
   *
   * @return the built thing
   * @since 4.10.0
   */
  @Contract(value = "-> new", pure = true)
  @NotNull R build();
}
