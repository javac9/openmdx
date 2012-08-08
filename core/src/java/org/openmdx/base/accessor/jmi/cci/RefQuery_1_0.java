/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefQuery_1_0.java,v 1.4 2010/05/26 13:25:43 hburger Exp $
 * Description: RefFilter_1_0 interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/05/26 13:25:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.cci;

import java.util.Collection;

import javax.jdo.Query;

import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;

/**
 * The RefQuery_1_0 is an extension to JMI and allows to query the Collections
 * returned by JMI methods.
 */
public interface RefQuery_1_0 extends Query {

  /**
   * This operation allows to set a filter value with the semantics
   * &lt;quantifier&gt; &lt;fieldName&gt; &lt;operator&gt; &lt;values&gt;. 
   */
  void refAddValue(
    String featureName,
    Quantifier quantifier,
    ConditionType operator,
    Collection<?> values
  );

  /**
   * This operation allows to set a filter value with the semantics
   * &lt;quantifier&gt; &lt;fieldName&gt; &lt;operator&gt; &lt;values&gt;. 
   * 
   * @param featureDef
   * @param quantifier
   * @param operator
   * @param values
   */
  void refAddValue(
    ModelElement_1_0 featureDef,
    Quantifier quantifier,
    ConditionType operator,
    Collection<?> values
  );

  /**
   * Allows to specify the sort order for a field.
   * 
   * @param featureName
   * @param order
   */
  void refAddValue(
    String featureName,
    SortOrder order
  );

  /**
   * Allows to specify the sort order for a field.
   * 
   * @param featureDef
   * @param order
   */
  void refAddValue(
    ModelElement_1_0 featureDef,
    SortOrder order
  );
  
  /**
   * Returns the collection of added conditions and order instructions.
   * 
   * @return a filter
   */
  Filter refGetFilter(
  );

  /**
   * Retrieve the filter's MOF id
   * 
   * @return the filter's MOF id
   */
  String refMofId();
    
}

//--- End of File -----------------------------------------------------------
