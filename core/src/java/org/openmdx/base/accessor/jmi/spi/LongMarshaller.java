/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LongMarshaller.java,v 1.11 2008/09/26 15:27:16 hburger Exp $
 * Description: LongMarshaller class
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/26 15:27:16 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.base.accessor.jmi.spi;

import org.openmdx.base.exception.ServiceException;

/**
 * Number <-> Long marshaller. Marshals objects which are instance of
 * Number to the specific type Long (which is also a Number).
 */
public class LongMarshaller extends NormalizingMarshaller {

    /**
     * Constructor 
     */
    private LongMarshaller(
    ) {
        // Avoid external instantiation
    }

    /**
     * A singleton
     */
    static private final LongMarshaller instance = new LongMarshaller();

    /**
     * @deprecated Use {@link #getInstance()} instead
     */
    public static LongMarshaller getInstance(
        boolean forward
    ) {
        return getInstance();
    }

    /**
     * Provide a marshaller instance
     * 
     * @return an instance
     */
    public static LongMarshaller getInstance(
    ) {
        return instance;
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.NormalizingMarshaller#normalize(java.lang.Object)
     */
    @Override
    protected Object normalize(
        Object source
    ) throws ServiceException{
        if(keep(source)) {
            return source;
        }
        //
        // Lenient
        //
        try {
            return new Long(((Number)source).longValue());
        } catch (Exception exception) {
            throw newServiceException(exception, source);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.NormalizingMarshaller#isLenient()
     */
    @Override
    protected boolean isLenient() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.NormalizingMarshaller#targetClass()
     */
    @Override
    protected Class<?> targetClass() {
        return Long.class;
    }
    
}
