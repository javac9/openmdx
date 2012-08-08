/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: ConnectionFactory.java,v 1.2 2010/08/03 14:27:24 hburger Exp $
 * Description: RADIUS Connection Factory 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/03 14:27:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2010, OMEX AG, Switzerland
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
package org.openmdx.resource.radius.client;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.openmdx.resource.spi.AbstractConnectionFactory;
import org.openmdx.resource.spi.AbstractManagedConnectionFactory;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusConnection;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * RADIUS Connection Factory
 */
public class ConnectionFactory
	extends AbstractConnectionFactory<RadiusConnection>
    implements org.openmdx.resource.cci.ConnectionFactory<RadiusConnection,RadiusException>
{

	/**
     * Constructor
     * 
     * @param managedConnectionFactory 
     * @param connectionManager
     */
    public ConnectionFactory(
        AbstractManagedConnectionFactory managedConnectionFactory, 
        ConnectionManager connectionManager
    ) {
    	super(
    		managedConnectionFactory,
    		connectionManager
    	);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3994838300645160656L;

    
    //------------------------------------------------------------------------
    // Implements ConnectionFactory
    //------------------------------------------------------------------------
    
	/* (non-Javadoc)
     * @see org.openmdx.resource.cci.ConnectionFactory#getConnection()
     */
//  @Override
    public RadiusConnection getConnection(
    ) throws RadiusException {
        try {
            return newConnection(
                null // connectionRequestInfo
            );
        } catch (ResourceException exception) {
        	throw new RadiusException(
                exception,
                "RADIUS client could not be acquired"
            );
        }
    }

}
