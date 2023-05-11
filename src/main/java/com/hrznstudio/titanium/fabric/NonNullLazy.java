/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.fabric;

import io.github.fabricators_of_create.porting_lib.util.Lazy;
import io.github.fabricators_of_create.porting_lib.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

/**
 * Proxy object for a value that is calculated on first access.
 * Same as {@link Lazy}, but with a nonnull contract.
 * @param <T> The type of the value
 */
public interface NonNullLazy<T> extends NonNullSupplier<T> {
    /**
     * Constructs a lazy-initialized object
     * @param supplier The supplier for the value, to be called the first time the value is needed.
     */
    static <T> NonNullLazy<T> of(@NotNull NonNullSupplier<T> supplier) {
        Lazy<T> lazy = Lazy.of(supplier::get);
        return () -> Objects.requireNonNull(lazy.get());
    }

    /**
     * Constructs a thread-safe lazy-initialized object
     * @param supplier The supplier for the value, to be called the first time the value is needed.
     */
    static <T> NonNullLazy<T> concurrentOf(@NotNull NonNullSupplier<T> supplier) {
        Lazy<T> lazy = Lazy.concurrentOf(supplier::get);
        return () -> Objects.requireNonNull(lazy.get());
    }
}
