/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.airlift.http.client;

import com.google.common.annotations.Beta;
import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import io.airlift.configuration.ConfigDefaults;
import io.airlift.http.client.jetty.SocketConfigurator;

import java.lang.annotation.Annotation;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.multibindings.Multibinder.newSetBinder;

@Beta
public class HttpClientBinder
{
    private final Binder binder;

    private HttpClientBinder(Binder binder)
    {
        this.binder = checkNotNull(binder, "binder is null").skipSources(getClass());
    }

    public static HttpClientBinder httpClientBinder(Binder binder)
    {
        return new HttpClientBinder(binder);
    }

    public HttpClientBindingBuilder bindHttpClient(String name, Class<? extends Annotation> annotation)
    {
        checkNotNull(name, "name is null");
        checkNotNull(annotation, "annotation is null");
        return createBindingBuilder(new HttpClientModule(name, annotation));
    }

    private HttpClientBindingBuilder createBindingBuilder(HttpClientModule module)
    {
        binder.install(module);
        return new HttpClientBindingBuilder(module,
                newSetBinder(binder, HttpRequestFilter.class, module.getFilterQualifier()),
                newSetBinder(binder, SocketConfigurator.class, module.getFilterQualifier()));
    }

    public static class HttpClientBindingBuilder
    {
        private final HttpClientModule module;
        private final Multibinder<HttpRequestFilter> filterBinder;
        private final Multibinder<SocketConfigurator> socketConfiguratorBinder;

        public HttpClientBindingBuilder(HttpClientModule module, Multibinder<HttpRequestFilter> filterBinder, Multibinder<SocketConfigurator> socketConfiguratorBinder)
        {
            this.module = module;
            this.filterBinder = filterBinder;
            this.socketConfiguratorBinder = socketConfiguratorBinder;
        }

        public HttpClientBindingBuilder withAlias(Class<? extends Annotation> alias)
        {
            module.addAlias(alias);
            return this;
        }

        public HttpClientBindingBuilder withAliases(Collection<Class<? extends Annotation>> aliases)
        {
            for (Class<? extends Annotation> annotation : aliases) {
                module.addAlias(annotation);
            }
            return this;
        }

        public HttpClientBindingBuilder withConfigDefaults(ConfigDefaults<HttpClientConfig> configDefaults)
        {
            module.withConfigDefaults(configDefaults);
            return this;
        }

        public LinkedBindingBuilder<HttpRequestFilter> addFilterBinding()
        {
            return filterBinder.addBinding();
        }

        public HttpClientBindingBuilder withFilter(Class<? extends HttpRequestFilter> filterClass)
        {
            filterBinder.addBinding().to(filterClass);
            return this;
        }

        public HttpClientBindingBuilder withSocketConfigurator(SocketConfigurator socketConfigurator)
        {
            socketConfiguratorBinder.addBinding().toInstance(socketConfigurator);
            return this;
        }

        public HttpClientBindingBuilder withTracing()
        {
            return withFilter(TraceTokenRequestFilter.class);
        }

        public HttpClientBindingBuilder withPrivateIoThreadPool()
        {
            module.withPrivateIoThreadPool();
            return this;
        }
    }
}
