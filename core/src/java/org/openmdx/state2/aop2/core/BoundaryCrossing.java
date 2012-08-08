/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BoundaryCrossing.java,v 1.1 2008/11/14 10:06:19 hburger Exp $
 * Description: Boundary Crossing
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 10:06:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.state2.aop2.core;

/**
 * Boundary Crossing
 */
enum BoundaryCrossing {

    NONE (false,false),
    STARTS_EARLIER (true,false),
    ENDS_LATER (false, true),
    STARTS_EARLIER_AND_ENDS_LATER (true, true);

    /**
     * <code>true</code> if a boundary crossing takes place at the beginning
     */
    final boolean startsEarlier;
    
    /**
     * <code>true</code> if a boundary crossing takes place at the end
     */
    final boolean endsLater;
    
    /**
     * Constructor 
     *
     * @param startsEarlier
     * @param endsLater
     */
    private BoundaryCrossing(
        boolean startsEarlier,
        boolean endsLater
    ){
        this.startsEarlier = startsEarlier;
        this.endsLater = endsLater;
    }
    
    /**
     * Retrieve the appropriate instance
     * 
     * @param startsEarlier
     * @param endsLater
     * 
     * @return the appropriate instance
     */
    static BoundaryCrossing valueOf(
        boolean startsEarlier,
        boolean endsLater
    ){
        return startsEarlier ? (
            endsLater ? STARTS_EARLIER_AND_ENDS_LATER : STARTS_EARLIER
        ) : (
            endsLater ? ENDS_LATER : NONE
        );
    }

}
