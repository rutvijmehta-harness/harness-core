/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

/*
 * Harness feature flag service
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: ff@harness.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package io.harness.cf.openapi.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FeatureEnvProperties
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-05-11T09:07:44.775-07:00[America/Los_Angeles]")
public class FeatureEnvProperties {
  public static final String SERIALIZED_NAME_ENVIRONMENT = "environment";
  @SerializedName(SERIALIZED_NAME_ENVIRONMENT) private String environment;

  public static final String SERIALIZED_NAME_VARIATION_MAP = "variationMap";
  @SerializedName(SERIALIZED_NAME_VARIATION_MAP) private List<VariationMap> variationMap = null;

  public static final String SERIALIZED_NAME_RULES = "rules";
  @SerializedName(SERIALIZED_NAME_RULES) private List<ServingRule> rules = null;

  public static final String SERIALIZED_NAME_STATE = "state";
  @SerializedName(SERIALIZED_NAME_STATE) private FeatureState state;

  public static final String SERIALIZED_NAME_DEFAULT_SERVE = "defaultServe";
  @SerializedName(SERIALIZED_NAME_DEFAULT_SERVE) private Serve defaultServe;

  public static final String SERIALIZED_NAME_OFF_VARIATION = "offVariation";
  @SerializedName(SERIALIZED_NAME_OFF_VARIATION) private String offVariation;

  public static final String SERIALIZED_NAME_MODIFIED_AT = "modifiedAt";
  @SerializedName(SERIALIZED_NAME_MODIFIED_AT) private Long modifiedAt;

  public static final String SERIALIZED_NAME_VERSION = "version";
  @SerializedName(SERIALIZED_NAME_VERSION) private Long version;

  public FeatureEnvProperties environment(String environment) {
    this.environment = environment;
    return this;
  }

  /**
   * Get environment
   * @return environment
   **/
  @ApiModelProperty(required = true, value = "")

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public FeatureEnvProperties variationMap(List<VariationMap> variationMap) {
    this.variationMap = variationMap;
    return this;
  }

  public FeatureEnvProperties addVariationMapItem(VariationMap variationMapItem) {
    if (this.variationMap == null) {
      this.variationMap = new ArrayList<>();
    }
    this.variationMap.add(variationMapItem);
    return this;
  }

  /**
   * Get variationMap
   * @return variationMap
   **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<VariationMap> getVariationMap() {
    return variationMap;
  }

  public void setVariationMap(List<VariationMap> variationMap) {
    this.variationMap = variationMap;
  }

  public FeatureEnvProperties rules(List<ServingRule> rules) {
    this.rules = rules;
    return this;
  }

  public FeatureEnvProperties addRulesItem(ServingRule rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

  /**
   * Get rules
   * @return rules
   **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public List<ServingRule> getRules() {
    return rules;
  }

  public void setRules(List<ServingRule> rules) {
    this.rules = rules;
  }

  public FeatureEnvProperties state(FeatureState state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
   **/
  @ApiModelProperty(required = true, value = "")

  public FeatureState getState() {
    return state;
  }

  public void setState(FeatureState state) {
    this.state = state;
  }

  public FeatureEnvProperties defaultServe(Serve defaultServe) {
    this.defaultServe = defaultServe;
    return this;
  }

  /**
   * Get defaultServe
   * @return defaultServe
   **/
  @ApiModelProperty(required = true, value = "")

  public Serve getDefaultServe() {
    return defaultServe;
  }

  public void setDefaultServe(Serve defaultServe) {
    this.defaultServe = defaultServe;
  }

  public FeatureEnvProperties offVariation(String offVariation) {
    this.offVariation = offVariation;
    return this;
  }

  /**
   * Get offVariation
   * @return offVariation
   **/
  @ApiModelProperty(required = true, value = "")

  public String getOffVariation() {
    return offVariation;
  }

  public void setOffVariation(String offVariation) {
    this.offVariation = offVariation;
  }

  public FeatureEnvProperties modifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  /**
   * Get modifiedAt
   * @return modifiedAt
   **/
  @ApiModelProperty(required = true, value = "")

  public Long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  public FeatureEnvProperties version(Long version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   * @return version
   **/
  @javax.annotation.Nullable
  @ApiModelProperty(value = "")

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FeatureEnvProperties featureEnvProperties = (FeatureEnvProperties) o;
    return Objects.equals(this.environment, featureEnvProperties.environment)
        && Objects.equals(this.variationMap, featureEnvProperties.variationMap)
        && Objects.equals(this.rules, featureEnvProperties.rules)
        && Objects.equals(this.state, featureEnvProperties.state)
        && Objects.equals(this.defaultServe, featureEnvProperties.defaultServe)
        && Objects.equals(this.offVariation, featureEnvProperties.offVariation)
        && Objects.equals(this.modifiedAt, featureEnvProperties.modifiedAt)
        && Objects.equals(this.version, featureEnvProperties.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environment, variationMap, rules, state, defaultServe, offVariation, modifiedAt, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FeatureEnvProperties {\n");
    sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
    sb.append("    variationMap: ").append(toIndentedString(variationMap)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    defaultServe: ").append(toIndentedString(defaultServe)).append("\n");
    sb.append("    offVariation: ").append(toIndentedString(offVariation)).append("\n");
    sb.append("    modifiedAt: ").append(toIndentedString(modifiedAt)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
