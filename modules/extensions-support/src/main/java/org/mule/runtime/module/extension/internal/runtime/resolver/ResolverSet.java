/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;

import com.google.common.collect.ImmutableMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link ValueResolver} which is based on associating a set of keys -&gt; {@link ValueResolver} pairs. The result of evaluating
 * this resolver is a {@link ResolverSetResult}.
 * <p>
 * The general purpose of this class is to repeatedly evaluate a set of {@link ValueResolver}s which results are to be used in the
 * construction of an object, so that the structure of such can be described only once (by the set of keys and
 * {@link ValueResolver}s but evaluated many times. With this goal in mind is that the return value of this resolver will always
 * be a {@link ResolverSetResult} which then can be used by a {@link ObjectBuilder} to generate an actual object.
 * <p>
 * Instances of this class are to be considered thread safe and reusable
 *
 * @since 3.7.0
 */
public class ResolverSet implements ValueResolver<ResolverSetResult>, Initialisable {

  private Map<String, ValueResolver> resolvers = new LinkedHashMap<>();
  private boolean dynamic = false;
  private final MuleContext muleContext;

  public ResolverSet(MuleContext muleContext) {
    this.muleContext = muleContext;
  }

  /**
   * Links the given {@link ValueResolver} to the given {@link ParameterModel}. If such {@code parameter} was already added, then
   * the associated {@code resolver} is replaced.
   *
   * @param key      a not {@code null} {@link ParameterModel}
   * @param resolver a not {@code null} {@link ValueResolver}
   * @return this resolver set to allow chaining
   * @throws IllegalArgumentException is either {@code parameter} or {@code resolver} are {@code null}
   */
  public ResolverSet add(String key, ValueResolver resolver) {
    checkArgument(key != null, "key cannot be null");
    checkArgument(resolver != null, "resolver cannot be null");

    if (resolvers.put(key, resolver) != null) {
      throw new IllegalStateException("A value was already given for key " + key);
    }

    if (resolver.isDynamic()) {
      dynamic = true;
    }
    return this;
  }

  /**
   * Whether at least one of the given {@link ValueResolver} are dynamic
   *
   * @return {@code true} if at least one resolver is dynamic. {@code false} otherwise
   */
  @Override
  public boolean isDynamic() {
    return dynamic;
  }

  /**
   * Evaluates all the added {@link ValueResolver}s and returns the results into a {@link ResolverSetResult}
   *
   * @param event a not {@code null} {@link Event}
   * @return a {@link ResolverSetResult}
   * @throws MuleException if an error occurs creating the {@link ResolverSetResult}
   */
  @Override
  public ResolverSetResult resolve(Event event) throws MuleException {
    ResolverSetResult.Builder builder = getResolverSetBuilder();

    for (Map.Entry<String, ValueResolver> entry : resolvers.entrySet()) {
      builder.add(entry.getKey(), resolveValue(entry.getValue(), event));
    }

    return builder.build();
  }

  private Object resolveValue(ValueResolver<?> resolver, Event event)
      throws MuleException {
    Object value = resolver.resolve(event);
    if (value instanceof ValueResolver) {
      return resolveValue((ValueResolver<?>) value, event);
    }

    if (value instanceof CursorProvider) {
      value = ((CursorProvider) value).openCursor();
    }

    return value;
  }

  public Map<String, ValueResolver> getResolvers() {
    return ImmutableMap.copyOf(resolvers);
  }

  public void initialise() {

    try {
      for (ValueResolver valueResolver : resolvers.values()) {
        muleContext.getInjector().inject(valueResolver);
        LifecycleUtils.initialiseIfNeeded(valueResolver);
      }
    } catch (MuleException e) {
      throw new MuleRuntimeException(e);
    }
  }

  ResolverSetResult.Builder getResolverSetBuilder() {
    return ResolverSetResult.newBuilder();
  }
}
