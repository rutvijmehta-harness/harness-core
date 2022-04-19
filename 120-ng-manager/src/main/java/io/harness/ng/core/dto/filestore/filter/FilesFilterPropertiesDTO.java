package io.harness.ng.core.dto.filestore.filter;

import static io.harness.annotations.dev.HarnessTeam.DEL;
import static io.harness.filter.FilterConstants.FILE_STORE_FILTER;

import io.harness.annotations.dev.OwnedBy;
import io.harness.filestore.FileUsage;
import io.harness.filter.FilterType;
import io.harness.filter.dto.FilterPropertiesDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName(FILE_STORE_FILTER)
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ApiModel("FilesFilterProperties")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@OwnedBy(DEL)
public class FilesFilterPropertiesDTO extends FilterPropertiesDTO {
  FileUsage fileUsage;
  String createdBy;
  String referencedBy;

  @Override
  public FilterType getFilterType() {
    return FilterType.FILESTORE;
  }
}
