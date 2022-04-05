package io.harness.mongo.index.migrator;

import lombok.Data;

@Data
public class ContainerIdAndNamespace {
  private String containerId;
  private String namespace;
}
