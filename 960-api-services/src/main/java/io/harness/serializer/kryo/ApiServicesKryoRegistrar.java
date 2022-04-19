/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.serializer.kryo;

import static io.harness.annotations.dev.HarnessTeam.PL;

import io.harness.annotations.dev.OwnedBy;
import io.harness.azure.response.AcrRegistriesDTO;
import io.harness.azure.response.AcrRegistryDTO;
import io.harness.azure.response.AcrRepositoriesDTO;
import io.harness.azure.response.AcrRepositoryDTO;
import io.harness.azure.response.AzureClusterDTO;
import io.harness.azure.response.AzureClustersDTO;
import io.harness.azure.response.AzureResourceGroupDTO;
import io.harness.azure.response.AzureResourceGroupsDTO;
import io.harness.azure.response.AzureSubscriptionDTO;
import io.harness.azure.response.AzureSubscriptionsDTO;
import io.harness.serializer.KryoRegistrar;

import com.esotericsoftware.kryo.Kryo;

@OwnedBy(PL)
public class ApiServicesKryoRegistrar implements KryoRegistrar {
  @Override
  public void register(Kryo kryo) {
    // keeping ids same
    kryo.register(AzureClusterDTO.class, 600001);
    kryo.register(AzureClustersDTO.class, 600002);
    kryo.register(AcrRegistryDTO.class, 600003);
    kryo.register(AcrRegistriesDTO.class, 600004);
    kryo.register(AcrRepositoryDTO.class, 600005);
    kryo.register(AcrRepositoriesDTO.class, 600006);
    kryo.register(AzureResourceGroupDTO.class, 600007);
    kryo.register(AzureResourceGroupsDTO.class, 600008);
    kryo.register(AzureSubscriptionDTO.class, 600009);
    kryo.register(AzureSubscriptionsDTO.class, 600010);
  }
}
