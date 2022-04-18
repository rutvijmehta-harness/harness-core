/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.app.impl;

import io.harness.EntityType;
import io.harness.ModuleType;
import io.harness.account.AccountClient;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.stages.IntegrationStageNode;
import io.harness.pms.yaml.YAMLFieldNameConstants;
import io.harness.steps.StepSpecTypeConstants;
import io.harness.utils.FeatureRestrictionsGetter;
import io.harness.yaml.schema.YamlSchemaGenerator;
import io.harness.yaml.schema.YamlSchemaProvider;
import io.harness.yaml.schema.beans.YamlSchemaRootClass;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OwnedBy(HarnessTeam.STO)
public class STOYamlSchemaServiceImpl extends StageYamlSchemaServiceImpl {
  @Inject
  public STOYamlSchemaServiceImpl(YamlSchemaProvider yamlSchemaProvider, YamlSchemaGenerator yamlSchemaGenerator,
      @Named("yaml-schema-subtypes") Map<Class<?>, Set<Class<?>>> yamlSchemaSubtypes,
      List<YamlSchemaRootClass> yamlSchemaRootClasses, AccountClient accountClient,
      FeatureRestrictionsGetter featureRestrictionsGetter) {
    super(yamlSchemaProvider, yamlSchemaGenerator, yamlSchemaSubtypes, yamlSchemaRootClasses, accountClient,
        featureRestrictionsGetter);
    this.moduleType = ModuleType.STO;
    this.stageEntityType = EntityType.SECURITY_STAGE;
    this.stepsEntityType = EntityType.SECURITY_STEPS;
    this.stageNamespace = YAMLFieldNameConstants.SECURITY;
    this.stageType = StepSpecTypeConstants.SECURITY_STAGE;
    this.stageNodeClass = IntegrationStageNode.class;
  }
}
