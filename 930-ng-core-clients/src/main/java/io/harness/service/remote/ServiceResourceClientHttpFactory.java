/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.service.remote;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.remote.client.AbstractHttpClientFactory;
import io.harness.remote.client.ClientMode;
import io.harness.remote.client.ServiceHttpClientConfig;
import io.harness.security.ServiceTokenGenerator;
import io.harness.serializer.kryo.KryoConverterFactory;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Singleton
@Slf4j
@OwnedBy(PIPELINE)
public class ServiceResourceClientHttpFactory
    extends AbstractHttpClientFactory implements Provider<ServiceResourceClient> {
  public ServiceResourceClientHttpFactory(ServiceHttpClientConfig ngManagerConfig, String serviceSecret,
      ServiceTokenGenerator tokenGenerator, KryoConverterFactory kryoConverterFactory, String clientId) {
    super(ngManagerConfig, serviceSecret, tokenGenerator, kryoConverterFactory, clientId, false, ClientMode.PRIVILEGED);
    log.info("secretManagerConfig {}", ngManagerConfig);
  }

  @Override
  public ServiceResourceClient get() {
    return getRetrofit().create(ServiceResourceClient.class);
  }
}
