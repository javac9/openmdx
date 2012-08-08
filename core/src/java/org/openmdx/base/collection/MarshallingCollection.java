/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingCollection.java,v 1.11 2008/10/02 17:32:13 hburger Exp $
 * Description: Marshalling Collection
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/02 17:32:13 $
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
package org.openmdx.base.collection;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.openmdx.base.persistence.spi.Marshaller;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;

/**
 * Marshalling Collection
 */
@SuppressWarnings("unchecked")
public class MarshallingCollection<E>
  extends AbstractCollection<E> 
  implements Serializable
{
    
    /**
     * Constructor
     * 
     * @param marshaller
     * @param collection
     * @param unmarshalling
     */
    public MarshallingCollection(
        Marshaller marshaller,
        Collection<?> collection, 
        Unmarshalling unmarshalling
    ) {
        this.marshaller = marshaller;
        this.delegate = (Collection<Object>) collection;
        this.unmarshalling = unmarshalling;
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param collection
     */
    public MarshallingCollection(
        Marshaller marshaller,
        Collection<?> collection
    ) {
        this(
            marshaller,
            collection,
            Unmarshalling.EAGER
        );
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param collection
     */
    public MarshallingCollection(
        org.openmdx.compatibility.base.marshalling.Marshaller marshaller,
        Collection<?> collection
    ) {
        this(
            new CollectionMarshallerAdapter(marshaller), 
            collection, 
            marshaller instanceof ReluctantUnmarshalling ? Unmarshalling.RELUCTANT : Unmarshalling.EAGER
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3977579212402079801L;

    /**
     * 
     */
    private Collection<Object> delegate;

    /**
     * 
     */    
    protected final Marshaller marshaller;

    /**
     * The unmarshal preference
     */
    protected final Unmarshalling unmarshalling;
    
    /**
     * Retrieve the delegate
     * <p>
     * This method may be overridden by a sub-class for dynamic delegation.
     * 
     * @return the delegate
     */
    protected Collection getDelegate(){
        return this.delegate;
    }

    /**
     * Replace the delegate for static delegation
     * 
     * @param the new delegate
     */
    protected void setDelegate(
        Collection delegate
    ){
        this.delegate = delegate;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        getDelegate().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E element) {
        return getDelegate().add(
            marshaller.unmarshal(element)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object element) {
        switch(this.unmarshalling) {
            case RELUCTANT: 
                return super.contains(
                    element
                );
            case EAGER: default: 
                return getDelegate().contains(
                    marshaller.unmarshal(element)
                );  
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }
    
    public Iterator<E> iterator(
    ) {
        return new MarshallingIterator(
            getDelegate().iterator()
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object element) {
        switch(this.unmarshalling) {
            case RELUCTANT: 
                return super.remove(
                    element
                );
            case EAGER: default: 
                return getDelegate().remove(
                    marshaller.unmarshal(element)
                 );
        }
    }

    public int size(
    ) {
        return getDelegate().size();
    }

    
    //------------------------------------------------------------------------
    // Class MarshallingIterator
    //------------------------------------------------------------------------
    
    /**
     * Marshalling Iterator
     * @author hburger
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
    protected class MarshallingIterator
        implements Iterator<E> 
    {

        /**
         * 
         * @param iterator
         */
        MarshallingIterator(
            Iterator<Object> iterator
        ) {
            this.iterator = iterator;
        }

        /*
         *  (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */        
        public boolean hasNext(
        ) {
            return this.iterator.hasNext();
        }
        
        /**
         * 
         */
        public E next(
        ) {
            return (E) marshaller.marshal(
                iterator.next()
            );
        }

        /**
         * 
         */        
        public void remove(
        ) {
            this.iterator.remove();
        }

        /**
         * 
         */
        private final Iterator<Object> iterator;
      
    }

}
