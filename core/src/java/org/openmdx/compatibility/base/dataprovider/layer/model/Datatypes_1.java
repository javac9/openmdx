/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Datatypes_1.java,v 1.1 2008/10/14 00:30:45 hburger Exp $
 * Description: Lenient Model Plug-In
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/14 00:30:45 $
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * Lenient Model Plug-In
 */
public class Datatypes_1
    extends Standard_1
{

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        super.activate(id, configuration, delegation);
        //
        // Assertion
        //
        if(isBypassedByLenientRequests()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "The " + Datatypes_1.class.getSimpleName() + " plugin must not be bypassed by lenient requests",
                new BasicException.Parameter(
                    "class", 
                    getClass().getName()
                ),
                new BasicException.Parameter(
                    SharedConfigurationEntries.BYPASSED_BY_LENIENT_REQUESTS, 
                    Boolean.TRUE
                )
            );
        }
    }

    /**
     * Complete lenient reply
     *  
     * @param reply
     * 
     * @return completed reply 
     * 
     * @throws ServiceException
     */
    protected DataproviderReply lenientReply(
        DataproviderReply reply
    ) throws ServiceException {
        for(DataproviderObject object : reply.getObjects()) {
            super.completeDatatypes(object);
        }
        return reply;
    }

    /**
     * Complete lenient request
     * 
     * @param header
     * @param request
     * 
     * @return completed request
     * 
     * @throws ServiceException
     */
    protected DataproviderRequest lenientRequest(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        switch(request.operation()) {
            case DataproviderOperations.OBJECT_MODIFICATION:
            case DataproviderOperations.OBJECT_REPLACEMENT:
            case DataproviderOperations.OBJECT_REMOVAL:
                super.verifyDigest(header, request);
        }
        return super.prepareDatatypes(request);
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? lenientReply(
            getDelegation().create(
                header,
                lenientRequest(header,request)
            )
        ) : super.create(
            header,
            request
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? lenientReply(
            getDelegation().get(
                header,
                lenientRequest(header,request)
            )
        ) : super.get(
            header,
            request
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? lenientReply(
            getDelegation().find(
                header,
                lenientRequest(header,request)
            )
        ) : super.find(
            header,
            request
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? this.completeReply(
            request,
            getDelegation().modify(
                header,
                lenientRequest(header,request)
            )
        ) : super.modify(
            header,
            request
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? lenientReply(
            getDelegation().remove(
                header,
                lenientRequest(header,request)
            )
        ) : super.remove(
            header,
            request
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.model.Compatibility_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return isLenient(request) ? lenientReply(
            getDelegation().replace(
                header,
                lenientRequest(header,request)
            )
        ) : super.replace(
            header,
            request
        );
    }

}
