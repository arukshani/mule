/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.heisenberg.extension.model;


import org.mule.runtime.extension.api.annotation.dsl.xml.XmlHints;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.List;
import java.util.Map;

@XmlHints(allowTopLevelDefinition = true)
public class RecursivePojo {

  @Parameter
  @Optional
  private RecursivePojo next;

  @Parameter
  @Optional
  private List<RecursivePojo> childs;

  @Parameter
  @Optional
  private Map<String, RecursivePojo> mappedChilds;

  public RecursivePojo getNext() {
    return next;
  }

  public List<RecursivePojo> getChilds() {
    return childs;
  }

  public Map<String, RecursivePojo> getMappedChilds() {
    return mappedChilds;
  }

}
