/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefContainer.java,v 1.4 2008/12/15 03:15:30 hburger Exp $
 * Description: RefContainer 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:30 $
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
package org.oasisopen.jmi1;

import java.util.List;

import javax.jmi.reflect.RefBaseObject;

import org.oasisopen.cci2.QualifierType;

/**
 * RefContainer
 */
public interface RefContainer extends RefBaseObject {

    /**
     * Adds an object to the container
     * 
     * @param arguments there is always an odd number of arguments:
     * ((REASSIGNABLE|PERSISTENT) identifier)+ value
     */
    void refAdd(
        Object... arguments 
    );
    
    /**
     * Retrieves an object from the container
     * 
     * @param arguments there is always an even number of arguments:
     * ((REASSIGNABLE|PERSISTENT) identifier)+
     * 
     * @return the object
     */
    Object refGet(
        Object... arguments 
    );

    /**
     * Removes an object from the container
     * 
     * @param arguments there is always an even number of arguments:
     * ((REASSIGNABLE|PERSISTENT) identifier)+
     */
    void refRemove(
        Object... arguments 
    );

    /**
     * Precedes a persistent sub-segment.
     */
    Object PERSISTENT = QualifierType.PERSISTENT;
    
    /**
     * Precedes re-assignable sub-segment.
     */
    Object REASSIGNABLE = QualifierType.REASSIGNABLE;
        
    
    //------------------------------------------------------------------------
    // Bulk Operations
    //------------------------------------------------------------------------

    /**
     * Executes a query 
     * 
     * @param query
     * 
     * @return the result
     */
    List<?> refGetAll(
        Object query
    );
    
    /**
     * Executes a query 
     * 
     * @param query
     * 
     * @return the result
     */
    long refRemoveAll(
        Object query
    );

}
