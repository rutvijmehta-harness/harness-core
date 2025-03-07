/*
 * Copyright 2020 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package software.wings.service.impl.splunk;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// TODO Compress Frequency pattern in log_analysis_record.proto
@Data
@Builder
@AllArgsConstructor
public class FrequencyPattern {
  int label;
  List<Pattern> patterns;
  String text;

  @Data
  @Builder
  public static class Pattern {
    private List<Integer> sequence;
    private List<Long> timestamps;
  }
}
