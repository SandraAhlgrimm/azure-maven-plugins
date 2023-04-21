/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.cosmos.cassandra;

import com.azure.core.util.paging.ContinuablePage;
import com.azure.resourcemanager.cosmos.fluent.CassandraResourcesClient;
import com.azure.resourcemanager.cosmos.fluent.models.CassandraTableGetResultsInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

public class CassandraTableModule extends AbstractAzResourceModule<CassandraTable, CassandraKeyspace, CassandraTableGetResultsInner> {
    private static final String NAME = "tables";

    public CassandraTableModule(@Nonnull CassandraKeyspace parent) {
        super(NAME, parent);
    }

    @Nonnull
    @Override
    protected CassandraTable newResource(@Nonnull CassandraTableGetResultsInner remote) {
        return new CassandraTable(remote, this);
    }

    @Nonnull
    @Override
    protected CassandraTable newResource(@Nonnull String name, @Nullable String resourceGroupName) {
        return new CassandraTable(name, Objects.requireNonNull(resourceGroupName), this);
    }

    @Nonnull
    @Override
    protected Iterator<? extends ContinuablePage<String, CassandraTableGetResultsInner>> loadResourcePagesFromAzure() {
        return Optional.ofNullable(getClient()).map(client -> {
            try {
                return client.listCassandraTables(parent.getResourceGroupName(), parent.getParent().getName(), parent.getName()).iterableByPage(getPageSize()).iterator();
            } catch (final RuntimeException e) {
                return null;
            }
        }).orElse(Collections.emptyIterator());
    }

    @Nullable
    @Override
    protected CassandraTableGetResultsInner loadResourceFromAzure(@Nonnull String name, @Nullable String resourceGroup) {
        return Optional.ofNullable(getClient()).map(client -> {
            try {
                return client.getCassandraTable(parent.getResourceGroupName(), parent.getParent().getName(), parent.getName(), name);
            } catch (final RuntimeException e) {
                return null;
            }
        }).orElse(null);
    }

    @Override
    @AzureOperation(name = "azure/cosmos.delete_cassandra_table.table", params = {"nameFromResourceId(resourceId)"})
    protected void deleteResourceFromAzure(@Nonnull String resourceId) {
        final ResourceId id = ResourceId.fromString(resourceId);
        Optional.ofNullable(getClient()).ifPresent(client -> client.deleteCassandraTable(id.resourceGroupName(), id.parent().parent().name(), id.parent().name(), id.name()));
    }

    @Nonnull
    @Override
    protected AzResource.Draft<CassandraTable, CassandraTableGetResultsInner> newDraftForCreate(@Nonnull String name, @Nullable String rgName) {
        return new CassandraTableDraft(name, Objects.requireNonNull(rgName), this);
    }

    @Nonnull
    @Override
    protected AzResource.Draft<CassandraTable, CassandraTableGetResultsInner> newDraftForUpdate(@Nonnull CassandraTable cassandraTable) {
        throw new AzureToolkitRuntimeException("not supported");
    }

    @Override
    @Nullable
    protected CassandraResourcesClient getClient() {
        return ((CassandraKeyspaceModule) this.getParent().getModule()).getClient();
    }
}
