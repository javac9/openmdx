/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: NonRepairingNamespaceContext.java,v 1.5 2010/06/02 10:50:06 hburger Exp $
 * Description: Non-Repairing Namespace Context
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 10:50:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.xml.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;


/**
 * Non-Repairing Namespace Context
 */
class NonRepairingNamespaceContext implements NamespaceContext {

    /**
     * Constructor 
     *
     * @param next
     * @param namespaceURI the element's namespace URI
     * @param prefix the element's prefix
     * @param localName the elements local name
     */
    NonRepairingNamespaceContext(
        NamespaceContext next,
        String namespaceURI,
        String prefix,
        String localName
    ){
        this.next = next;
        this.namespaceURI = namespaceURI;
        this.prefix = prefix;
        this.localName = localName;
    }

    /**
     * 
     */
    private final NamespaceContext next;

    /**
     * 
     */
    private final Map<String,String> mapping = new HashMap<String,String>();
    
    /**
     * The element's namespace URI, may be <code>""</code>
     */
    private final String namespaceURI;
    
    /**
     * The element's prefix, may be <code>""</code>
     */
    private final String prefix;

    /**
     * Local name of the element opening this scope
     */
    private final String localName;
    
    /**
     * Set a namespace prefix
     * 
     * @param prefix the namespace prefix, may be <code>XMLConstants.DEFAULT_NS_PREFIX</code>
     * @param uri
     */
    void put(
        String prefix,
        String uri
    ){
        this.mapping.put(prefix, uri);
    }
    
    /**
     * In order to leave the current scope
     * 
     * @return the next namespace context
     */
    NonRepairingNamespaceContext getNext(){
        return (NonRepairingNamespaceContext) this.next;
    }
    
    /**
     * Retrieve the namespace URI of the currrent element
     * 
     * @return the namespace URI of the currrent element
     */
    String getCurrentNamespaceURI(){
        return 
            this.namespaceURI != null && !"".equals(this.namespaceURI) ? this.namespaceURI :
            this.getNamespaceURI(this.prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : this.prefix);
    }

    
    /**
     * Retrieve the local name of the currrent element
     * @return the local name of the currrent element
     */
    String getCurrentElementName(){
        return this.localName;
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
//  @Override
    public String getNamespaceURI(
        String prefix
    ) {
        if(prefix == null) throw new IllegalArgumentException();
        String uri = this.mapping.get(prefix);
        return 
            uri != null ? uri :
            XMLConstants.XML_NS_PREFIX.equals(prefix) ? XMLConstants.XML_NS_URI : 
            XMLConstants.XMLNS_ATTRIBUTE.equals(prefix) ? XMLConstants.XMLNS_ATTRIBUTE_NS_URI :
            this.next != null ? this.next.getNamespaceURI(prefix) : 
            XMLConstants.NULL_NS_URI;
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
//  @Override
    public String getPrefix(String namespaceURI) {
        if(namespaceURI == null) throw new IllegalArgumentException();
        for(Map.Entry<String, String> e : mapping.entrySet()) {
            if(namespaceURI.equals(e.getValue())) {
                return e.getKey();
            }
        }
        return
            XMLConstants.XML_NS_URI.equals(namespaceURI) ? XMLConstants.XML_NS_PREFIX :
            XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI) ? XMLConstants.XMLNS_ATTRIBUTE :
            this.next != null ? this.next.getPrefix(namespaceURI) :
            null;
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
//  @Override
    public Iterator<?> getPrefixes(
        final String namespaceURI
    ) {
        if(namespaceURI == null) throw new IllegalArgumentException();
        return
            XMLConstants.XML_NS_URI.equals(namespaceURI) ? Collections.singleton(XMLConstants.XML_NS_PREFIX).iterator() :
            XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI) ? Collections.singleton(XMLConstants.XMLNS_ATTRIBUTE).iterator() :
            new Prefixes(namespaceURI, this.mapping, this.next);
    }

    
    //------------------------------------------------------------------------
    // Class Prefixes
    //------------------------------------------------------------------------
    
    /**
     * Prefixes
     */
    private class Prefixes implements Iterator<Object> {

        /**
         * Constructor 
         *
         * @param namespaceURI
         * @param primary 
         * @param NamespaceContext 
         */
        Prefixes(
            String namespaceURI, 
            Map<String, String> primary, 
            NamespaceContext secondary
        ){
            this.primary = new ArrayList<String>();
            this.delegate = secondary.getPrefixes(namespaceURI);
            for(Map.Entry<String, String> e : primary.entrySet()) {
                if(namespaceURI.equals(e.getValue())) {
                    this.primary.add(e.getValue());
                }
            }
        }
        
        private final List<String> primary;
        private final Iterator<?> delegate;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
    //  @Override
        public boolean hasNext() {
            return !this.primary.isEmpty() || this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
    //  @Override
        public Object next() {
            return this.primary.isEmpty() ? this.delegate.next() : this.primary.get(0);
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
    //  @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
