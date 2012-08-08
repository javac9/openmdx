/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: IsGreaterOrEqualCondition.java,v 1.10 2010/06/01 09:00:06 hburger Exp $
 * Description: Is-Greater-Than-Or-Equal-To Condition
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/01 09:00:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package org.openmdx.base.query;


/**
 * Typed condition for<ul>
 * <li>ConditionType.IS_GREATER_OR_EQUAL
 * <li>ConditionType.IS_LESS
 * </ul>
 */
public class IsGreaterOrEqualCondition extends Condition {
    /**
     * Constructor 
     */
    public IsGreaterOrEqualCondition(
    ) {
        this.fulfils = false;
    }

    /**
     * Constructor 
     *
     * @param quantifier
     * @param feature
     * @param fulfil
     * @param values
     */
    public IsGreaterOrEqualCondition(
        Quantifier quantifier,
        String feature,
        boolean fulfil,
        Object expression
    ) {
        super(
            quantifier,
            feature,
            expression
        );
        this.fulfils = fulfil;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3258134660948504628L;

    /**
     * Defines whether the condition shall be <code>true</code> of <code>false</code>
     */
    private boolean fulfils;
        
    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public IsGreaterOrEqualCondition clone(
    ) throws CloneNotSupportedException {
        return new IsGreaterOrEqualCondition(
            this.getQuantifier(), 
            this.getFeature(), 
            this.isFulfil(),
            this.getExpression()
        );
    }

    /**
     * Tells whether the condition shall be <code>true</code> or <code>false</code>
     * 
     * @return <code>true</code> if the condition shall be fulfilled
     */
    public boolean isFulfil() {
        return this.fulfils;
    }

    /**
     * Defines whether the condition shall be <code>true</code> or <code>false</code>
     * 
     * @param fulful <code>true</code> if the condition shall be fulfilled
     */
    public void setFulfil(
        boolean fulfil
    ) {
        this.fulfils = fulfil;
    }

    @Override
    public ConditionType getType(
    ) {
        return this.isFulfil() ? ConditionType.IS_GREATER_OR_EQUAL : ConditionType.IS_LESS;
    }

    /**
     * Retrieve the expression to used in the comparison
     * 
     * @return the expression to used in the comparison
     */
    public Object getExpression(){
        return super.getValue(0);
    }

}
