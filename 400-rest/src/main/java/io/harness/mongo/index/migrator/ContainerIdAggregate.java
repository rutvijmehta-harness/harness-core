package io.harness.mongo.index.migrator;

import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Data
@Entity(noClassnameStored = true)
public class ContainerIdAggregate {
  @Id private ContainerIdAndNamespace _id;
  private Integer count;
}
