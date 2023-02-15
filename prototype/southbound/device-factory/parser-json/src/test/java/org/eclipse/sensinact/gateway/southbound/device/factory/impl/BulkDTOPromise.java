/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.osgi.util.function.Consumer;
import org.osgi.util.function.Function;
import org.osgi.util.function.Predicate;
import org.osgi.util.promise.Failure;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Success;

public class BulkDTOPromise implements Promise<BulkGenericDto> {

    private final BulkGenericDto dto;

    public BulkDTOPromise(BulkGenericDto value) {
        dto = value;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public BulkGenericDto getValue() throws InvocationTargetException, InterruptedException {
        return dto;
    }

    @Override
    public Throwable getFailure() throws InterruptedException {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> onResolve(Runnable callback) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> onSuccess(Consumer<? super BulkGenericDto> success) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> onFailure(Consumer<? super Throwable> failure) {
        return null;
    }

    @Override
    public <R> Promise<R> then(Success<? super BulkGenericDto, ? extends R> success, Failure failure) {
        return null;
    }

    @Override
    public <R> Promise<R> then(Success<? super BulkGenericDto, ? extends R> success) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> thenAccept(Consumer<? super BulkGenericDto> consumer) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> filter(Predicate<? super BulkGenericDto> predicate) {
        return null;
    }

    @Override
    public <R> Promise<R> map(Function<? super BulkGenericDto, ? extends R> mapper) {
        return null;
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super BulkGenericDto, Promise<? extends R>> mapper) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> recover(Function<Promise<?>, ? extends BulkGenericDto> recovery) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> recoverWith(Function<Promise<?>, Promise<? extends BulkGenericDto>> recovery) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> fallbackTo(Promise<? extends BulkGenericDto> fallback) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> timeout(long milliseconds) {
        return null;
    }

    @Override
    public Promise<BulkGenericDto> delay(long milliseconds) {
        return null;
    }
}
