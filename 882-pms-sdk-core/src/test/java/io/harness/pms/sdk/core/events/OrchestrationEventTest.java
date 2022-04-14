package io.harness.pms.sdk.core.events;

import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_NESTS;
import static io.harness.rule.OwnerRule.SAHIL;

import static org.assertj.core.api.Assertions.assertThat;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.category.element.UnitTests;
import io.harness.logging.AutoLogContext;
import io.harness.pms.contracts.ambiance.Ambiance;
import io.harness.pms.contracts.execution.events.OrchestrationEventType;
import io.harness.pms.execution.utils.AmbianceUtils;
import io.harness.pms.sdk.core.AmbianceTestUtils;
import io.harness.pms.sdk.core.PmsSdkCoreTestBase;
import io.harness.rule.Owner;

import java.util.Map;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@OwnedBy(HarnessTeam.PIPELINE)
public class OrchestrationEventTest extends PmsSdkCoreTestBase {
  @Test
  @Owner(developers = SAHIL)
  @Category(UnitTests.class)
  public void testAutoLogContext() {
    Ambiance ambiance = AmbianceTestUtils.buildAmbiance();
    OrchestrationEvent orchestrationEvent =
        OrchestrationEvent.builder().ambiance(ambiance).eventType(OrchestrationEventType.ORCHESTRATION_END).build();
    Map<String, String> logContext = AmbianceUtils.logContextMap(ambiance);
    logContext.put(
        OrchestrationEvent.OrchestrationEventKeys.eventType, OrchestrationEventType.ORCHESTRATION_END.name());
    AutoLogContext autoLogContext = new AutoLogContext(logContext, OVERRIDE_NESTS);
    assertThat(orchestrationEvent.autoLogContext()).isEqualTo(autoLogContext);
  }
}