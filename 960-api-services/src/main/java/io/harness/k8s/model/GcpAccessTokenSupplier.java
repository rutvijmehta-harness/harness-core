package io.harness.k8s.model;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;

import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.store.DataStore;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

@OwnedBy(HarnessTeam.CDP)
public class GcpAccessTokenSupplier implements Supplier<String> {
  private final String serviceAccountJsonKey;
  private final DataStore<StoredCredential> cache;
  private final Clock clock;
  private final GoogleCredential googleCredential;

  public GcpAccessTokenSupplier(String serviceAccountJsonKey, Function<String, GoogleCredential> jsonKeyToCredential,
                                DataStore<StoredCredential> cache, Clock clock) {
    this.serviceAccountJsonKey = serviceAccountJsonKey;
    this.googleCredential =
        copyAndAddRefreshListener(jsonKeyToCredential.apply(serviceAccountJsonKey), clock, cache);
    this.cache = cache;
    this.clock = clock;
  }

  @Override
  public String get() {
    try {
      StoredCredential storedCredential = cache.get(googleCredential.getServiceAccountId());
      if (isNullOrExpired(storedCredential)) {
        googleCredential.refreshToken();
      }
      return cache.get(googleCredential.getServiceAccountId()).getAccessToken();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getServiceAccountJsonKey() {
    return serviceAccountJsonKey;
  }

  private boolean isNullOrExpired(StoredCredential storedCredential) {
    if (storedCredential == null) {
      return true;
    }
    Instant expirationTime = Instant.ofEpochMilli(storedCredential.getExpirationTimeMilliseconds());
    return clock.instant().isAfter(expirationTime.minus(Duration.ofMinutes(1L)));
  }

  private GoogleCredential copyAndAddRefreshListener(
      GoogleCredential googleCredential, Clock clock, DataStore<StoredCredential> cache) {

    return new GoogleCredential.Builder()
        .setJsonFactory(googleCredential.getJsonFactory())
        .setTransport(googleCredential.getTransport())
        .setServiceAccountId(googleCredential.getServiceAccountId())
        .setServiceAccountScopes(googleCredential.getServiceAccountScopes())
        .setServiceAccountPrivateKey(googleCredential.getServiceAccountPrivateKey())
        .setServiceAccountPrivateKeyId(googleCredential.getServiceAccountPrivateKeyId())
        .setTokenServerEncodedUrl(googleCredential.getTokenServerEncodedUrl())
        .setServiceAccountProjectId(googleCredential.getServiceAccountProjectId())
        .addRefreshListener(new DataStoreCredentialRefreshListener(googleCredential.getServiceAccountId(), cache))
        .setClock(clock::millis)
        .build();
  }
}
