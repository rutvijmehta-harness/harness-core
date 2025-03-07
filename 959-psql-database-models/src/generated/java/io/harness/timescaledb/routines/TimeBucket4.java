/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.routines;

import io.harness.timescaledb.Public;

import java.time.LocalDateTime;
import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.types.YearToSecond;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class TimeBucket4 extends AbstractRoutine<LocalDateTime> {
  private static final long serialVersionUID = 1L;

  /**
   * The parameter <code>public.time_bucket.RETURN_VALUE</code>.
   */
  public static final Parameter<LocalDateTime> RETURN_VALUE =
      Internal.createParameter("RETURN_VALUE", SQLDataType.LOCALDATETIME(0), false, false);

  /**
   * The parameter <code>public.time_bucket.bucket_width</code>.
   */
  public static final Parameter<YearToSecond> BUCKET_WIDTH =
      Internal.createParameter("bucket_width", SQLDataType.INTERVAL, false, false);

  /**
   * The parameter <code>public.time_bucket.ts</code>.
   */
  public static final Parameter<LocalDateTime> TS =
      Internal.createParameter("ts", SQLDataType.LOCALDATETIME(0), false, false);

  /**
   * The parameter <code>public.time_bucket.origin</code>.
   */
  public static final Parameter<LocalDateTime> ORIGIN =
      Internal.createParameter("origin", SQLDataType.LOCALDATETIME(0), false, false);

  /**
   * Create a new routine call instance
   */
  public TimeBucket4() {
    super("time_bucket", Public.PUBLIC, SQLDataType.LOCALDATETIME(0));

    setReturnParameter(RETURN_VALUE);
    addInParameter(BUCKET_WIDTH);
    addInParameter(TS);
    addInParameter(ORIGIN);
    setOverloaded(true);
  }

  /**
   * Set the <code>bucket_width</code> parameter IN value to the routine
   */
  public void setBucketWidth(YearToSecond value) {
    setValue(BUCKET_WIDTH, value);
  }

  /**
   * Set the <code>bucket_width</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket4 setBucketWidth(Field<YearToSecond> field) {
    setField(BUCKET_WIDTH, field);
    return this;
  }

  /**
   * Set the <code>ts</code> parameter IN value to the routine
   */
  public void setTs(LocalDateTime value) {
    setValue(TS, value);
  }

  /**
   * Set the <code>ts</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket4 setTs(Field<LocalDateTime> field) {
    setField(TS, field);
    return this;
  }

  /**
   * Set the <code>origin</code> parameter IN value to the routine
   */
  public void setOrigin(LocalDateTime value) {
    setValue(ORIGIN, value);
  }

  /**
   * Set the <code>origin</code> parameter to the function to be used with a {@link org.jooq.Select} statement
   */
  public TimeBucket4 setOrigin(Field<LocalDateTime> field) {
    setField(ORIGIN, field);
    return this;
  }
}
