/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistentContainer_1.java,v 1.45 2008/12/15 03:15:30 hburger Exp $
 * Description: A persistent container implementation
 * Revision:    $Revision: 1.45 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.ObjectComparator_1;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.Reconstructable;
import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.exception.InvalidCardinalityException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.AbstractListIterator;
import org.openmdx.compatibility.base.collection.FilteringList;
import org.openmdx.compatibility.base.collection.MergingList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_3;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.AbstractFilter;
import org.openmdx.compatibility.base.query.Selector;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * A persistent container implementation
 */
@SuppressWarnings("unchecked")
class PersistentContainer_1
    extends AbstractContainer<Object_1_0>
    implements Evictable 
{

    /**
     * Constructor
     * 
     * @param referenceFilter
     * @param attributeFilter
     * @param manager
     * @param provider
     * @param providerManagedReferenceCollection
     * @param sequenceProvider
     */
    private PersistentContainer_1(
        Path referenceFilter,
        Selector attributeFilter,
        Connection_1_3 manager,
        Provider_1_0 provider,
        List providerManagedReferenceCollection,
        Object_1 sequenceProvider
    ){
        super(attributeFilter);
        if(provider == null) throw new IllegalArgumentException(
            "A persistent container's provider must not be null"
        );
        if(manager == null) throw new IllegalArgumentException(
            "A persistent container's manager must not be null"
        );      
        this.referenceFilter = StateCapables.toContainerId(referenceFilter);
        this.provided = providerManagedReferenceCollection;
        this.manager = manager;
        this.provider = provider;
        this.sequenceProvider = sequenceProvider;
    }

    /**
     * Constructor 
     * 
     * @param referenceFilter
     * @param manager
     * @param provider
     * @param providerManagedObjects
     * @param sequenceProvider
     */
    PersistentContainer_1(
        Path referenceFilter,
        Connection_1_3 manager,
        Provider_1_0 provider,
        List providerManagedObjects, 
        Object_1 sequenceProvider
    ){
        this(
            referenceFilter, 
            null, // attributeFilter
            manager, 
            provider,
            providerManagedObjects,
            sequenceProvider
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3978147629899526454L;

    /**
     *
     */
    Path getReferenceFilter(
    ){
        return this.referenceFilter;
    }

    /**
     *
     */
    Provider_1_0 getProvider(
    ){
        return this.provider;
    }

    /**
     *
     */
    Connection_1_3 getManager(
    ){
        return this.manager;
    }


    //--------------------------------------------------------------------------
    // Extends AbstractContainer
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.AbstractContainer#initialQualifier()
     */
    protected long initialQualifier(
    ) throws ServiceException{
        return this.sequenceProvider.getSequence(
            this.referenceFilter.getBase()
        );
    }


    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /**
     * Returns the number of elements in this collection. If the collection 
     * contains more than Integer.MAX_VALUE elements or the number of elements
     * is unknown, returns Integer.MAX_VALUE.
     *
     * @eturn   the number of elements in this collection
     */
    public int size(
    ) {
        return getDelegate().size();
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     * 
     * @return    an iterator over the elements contained in this collection.
     */
    public Iterator iterator(
    ){
        return getDelegate().iterator();
    }

    /**
     * Returns true if this collection contains the specified element. 
     *
     * @param   element
     *          element whose presence in this collection is to be tested.
     *
     * @return  true if this collection contains the specified element
     */
    public boolean contains(
        Object element
    ){
        if(element instanceof Object_1) {
            Object_1 object = (Object_1)element;
            return 
                object.manager == this.manager &&
                object.objIsPersistent() && !object.objIsDeleted() &&
                object.objGetPath().getParent().equals(this.referenceFilter) &&
                (getSelector() == null || getSelector().accept(object));
        } else {
            return false;
        }
    }

    /**
     * Removes a single instance of the specified element from this
     * container, if it is present
     *
     * @param   element
     *          element to be removed from this container, if present.
     *
     * @return  true if this collection changed as a result of the call.
     *
     * @exception   UnsupportedOperationException
     *              remove is not supported by this collection.
     */
    public boolean remove(
        Object element
    ){
        try {
            boolean modify=contains(element);
            if(modify)((Object_1)element).objRemove();
            return modify;
        } catch (ServiceException exception) {
            throw new BadParameterException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.AbstractContainer#getModel()
     */
    @Override
    protected Model_1_0 getModel() {
        return this.manager instanceof Connection_1_5 ? 
            (( Connection_1_5)this.manager).getModel() :
            null;
    }

    /**
     * Selects objects matching the filter.
     * <p>
     * The semantics of the collection returned by this method become
     * undefined if the backing collection (i.e., this list) is structurally
     * modified in any way other than via the returned collection. (Structural
     * modifications are those that change the size of this list, or otherwise
     * perturb it in such a fashion that iterations in progress may yield
     * incorrect results.) 
     * <p>
     * This method returns a Collection as opposed to a Set because it 
     * behaves as set in respect to object id equality, not element equality.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    A subset of this container containing the objects
     *            matching the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     */
    @SuppressWarnings("deprecation")
    public org.openmdx.compatibility.base.collection.Container subSet(
        Object filter
    ){
        Selector selector = super.combineWith(filter);
        return selector == null ? this : new PersistentContainer_1(
            this.referenceFilter,
            selector,
            this.manager,
            this.provider,
            this.provided,
            this.sequenceProvider
        );
    }

    /**
     * Select an object matching the filter.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    the object matching the filter;
     *            or null if no object matches the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     * @exception InvalidCardinalityException
     *            if more than one object matches the filter. 
     */
    public Object_1_0 get(
        Object filter
    ){
        try {
            Object_1_0 candidate = this.manager.getObject(
                this.referenceFilter.getChild((String)filter)
            );
            return getSelector() == null || getSelector().accept(candidate) ? candidate : null; 
        } catch (Exception source) {
            BadParameterException target = new BadParameterException(source);
            if(target.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            } else {
                throw target;
            }
        }
    }

    /**
     * Applies given criteria to the elements of the container and returns the
     * result as list.
     * <p>
     * The acceptable criteria classes must be specified by the container 
     * implementation.
     *
     * @param     criteria
     *            The criteria to be applied to objects of this container;
     *            or <code>null</code> for all the container's elements in
     *            their default order.
     *
     * @return    a list based on the container's elements and the given
     *            criteria.
     * 
     * @exception ClassCastException
     *            if the class of the specified criteria prevents them from
     *            being applied to this container's elements.
     * @exception IllegalArgumentException
     *            if some aspect of the criteria prevents them from being
     *            applied to this container's elements. 
     */
    public List toList(
        Object criteria
    ) {
        try {
            //
            // Reconstruct
            //
            if(criteria instanceof InputStream){
                InputStream iterator = (InputStream)criteria;
                ObjectInputStream buffer=new ObjectInputStream(iterator);
                Selector attributeFilter = (Selector)buffer.readObject();
                if(attributeFilter instanceof ModelHolder_1_0) {
                    ((ModelHolder_1_0)attributeFilter).setModel(
                        ((ModelHolder_1_0)this.manager).getModel()
                    );
                }
                ObjectComparator_1 comparator = (ObjectComparator_1)buffer.readObject();
                if(buffer.readBoolean()){
                    Object[] include = (
                            (UnitOfWork_1)manager.getUnitOfWork()
                    ).include(
                        this.referenceFilter,
                        attributeFilter
                    ).toArray();
                    Arrays.sort(include, comparator);
                    return new ReconstructableList(
                        attributeFilter,
                        Arrays.asList(include),
                        new MarshallingSequentialList(
                            this.manager,
                            this.provider.reconstruct(
                                this.referenceFilter,
                                this.manager,
                                iterator
                            )
                        ),
                        comparator
                    );
                } else {
                    return new PersistentContainer_1(
                        this.referenceFilter,
                        attributeFilter,
                        this.manager,
                        this.provider,
                        this.provided,
                        this.sequenceProvider
                    ).toList(
                        comparator.getDelegate()
                    );
                }
            } else {       
                //
                // Construct
                //
                ObjectComparator_1 comparator = new ObjectComparator_1((AttributeSpecifier[])criteria);
                Object[] buffer = getInclude().toArray();
                Arrays.sort(buffer, comparator);
                return new ReconstructableList(
                    getSelector(),
                    Arrays.asList(buffer),
                    new MarshallingSequentialList(
                        this.manager,
                        this.provider.find(
                            this.referenceFilter,
                            getSelector() == null ? null : ((AbstractFilter)getSelector()).getDelegate(),
                                comparator.getDelegate(),
                                this.manager
                        )
                    ),
                    comparator
                );
            }
        } catch (Exception exception) {
            throw new BadParameterException(exception);
        }
    }

    private boolean isNew() {
        return this.sequenceProvider.objIsNew();
    }

    public List getDelegate(
    ){
        if(this.collection == null) try {
            if(isNew()) { // TODO
                return getInclude();
            } else {
                this.collection = new CountingCollection (
                    new List[]{
                        getInclude(),
                        new ProviderCollection(
                            getSelector() == null ? this.provided : new CountingList(
                                this.manager
                            ) {

                                private static final long serialVersionUID = -2302646566011181142L;

                                @Override
                                protected List newDelegate(
                                ) throws ServiceException {
                                    return provider.find(
                                        referenceFilter,
                                        ((AbstractFilter)getSelector()).getDelegate(),
                                        null,
                                        manager
                                    );
                                }
                               
                            }, 
                            ((UnitOfWork_1)manager.getUnitOfWork()).exclude(
                                referenceFilter,
                                getSelector()
                            )
                        )
                    }
                );
            }
        } catch (ServiceException exception) {
            throw new BadParameterException(exception);
        }
        return this.collection;
    }

    private List getInclude(
    ){
        if(this.include == null) try {
            this.include = (
                    (UnitOfWork_1)manager.getUnitOfWork()
            ).include(
                this.referenceFilter,
                getSelector()
            );
        } catch (ServiceException serviceException) {
            throw new IllegalStateException(serviceException);
        }
        return this.include;
    }

    
    //------------------------------------------------------------------------
    // Implements RefBaseObject
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    public String refMofId() {
        return this.referenceFilter.toResourceIdentifier();
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    public String toString(
    ){
        StringBuilder text = new StringBuilder(
            getClass().getName()
        );
        try {
            text.append(
                ": ("
            ).append(
                this.referenceFilter.toXri() 
            ).append(
                ", "
            ).append(
                getSelector() == null ? 0 : ((AbstractFilter)getSelector()).size()
            ).append(
                " filter properties)"
            ).toString();
        } catch (Exception exception) {
            text.append(
                "// "
            ).append(
                exception
            );
        }
        return text.toString();
    }


    //------------------------------------------------------------------------
    // Implements Evictable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public void evict() {
        if(this.provided instanceof Evictable) {
            ((Evictable)this.provided).evict();      
        }
        if(this.collection instanceof Evictable) {
            ((Evictable)this.collection).evict();
        }
    }


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * @serial
     */
    protected Path referenceFilter; 

    /**
     * @serial
     */
    protected Connection_1_3 manager;

    /**
     * @serial
     */
    protected Provider_1_0 provider;

    /**
     * Reconstructable collection
     */
    protected transient List collection; 

    /**
     * Reconstructable collection
     */
    protected transient List include; 

    /**
     * @serial
     */
    protected final List provided;

    /**
     * The collection's parent
     */
    protected final Object_1 sequenceProvider;

    /**
     * 
     */
    protected static final int LOCAL_EVALUATION_LIMIT = 1000;


    //------------------------------------------------------------------------
    // Class ReconstructableList
    //------------------------------------------------------------------------

    /**
     * 
     */
    static class ReconstructableList 
    extends MergingList 
    implements Reconstructable
    {

        /**
         * @param x
         * @param y
         * @param comparator
         */
        ReconstructableList(
            Selector filter,
            List x, 
            List y, 
            Comparator comparator
        ) {
            super(x, y, comparator);
            this.filter = filter;
        }

        /* (non-Javadoc)
         */
        public void write(OutputStream stream) throws ServiceException {
            boolean propagate = !lists[1].isEmpty() && lists[1] instanceof Reconstructable;
            try {
                ObjectOutputStream buffer=new ObjectOutputStream(stream);
                buffer.writeObject(filter);
                buffer.writeObject(comparator);
                buffer.writeBoolean(propagate);
                buffer.flush();
                if(propagate)((Reconstructable)lists[1]).write(stream);
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator listIterator(int index) {
            return new ObjectIterator(super.listIterator(index));
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int collectionSize = super.size();
            if(collectionSize != Integer.MAX_VALUE) return collectionSize;
            int iterationCount = 0;
            for (
                    Iterator i = this.iterator();
                    i.hasNext();
            ){
                i.next();
                if(iterationCount++ % LOCAL_EVALUATION_LIMIT == 0) {
                    collectionSize = super.size();
                    if(collectionSize != Integer.MAX_VALUE) return collectionSize;
                }
            }
            return iterationCount;
        }

        /**
         * 
         */     
        private Selector filter;

    }


    //------------------------------------------------------------------------
    // Class ObjectIterator
    //------------------------------------------------------------------------

    /**
     * 
     */
    static class ObjectIterator extends AbstractListIterator {

        /**
         * 
         */
        public ObjectIterator(
            ListIterator iterator
        ){
            this.iterator = iterator;
        }

        /* (non-Javadoc)
         */
        protected ListIterator getDelegate() {
            return this.iterator;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(current == null) throw new IllegalStateException(
                "the is no current object"
            );
            try {
                ((Object_1)super.current).objRemove();
            } catch (ServiceException exception) {
                throw new UnsupportedOperationException(exception.getMessage());
            }
        }

        /**
         * 
         */ 
        private final ListIterator iterator;

    }


    //------------------------------------------------------------------------
    // Class CountingCollection
    //------------------------------------------------------------------------

    static class CountingCollection 
    extends ChainingList 
    implements Serializable
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3257562923424559925L;

        /**
         * @param lists
         */
        CountingCollection(List[] lists) {
            super(lists);
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int collectionSize = super.size();
            if(collectionSize != Integer.MAX_VALUE) return collectionSize;
            int iterationCount = 0;
            for (
                    Iterator i = this.iterator();
                    i.hasNext();
            ){
                i.next();
                if(iterationCount++ % LOCAL_EVALUATION_LIMIT == 0) {
                    collectionSize = super.size();
                    if(collectionSize != Integer.MAX_VALUE) return collectionSize;
                }
            }
            return iterationCount;
        }

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator listIterator(int index) {
            return new ObjectIterator(super.listIterator(index));
        }

    }

    //------------------------------------------------------------------------
    // Class ReconstructableList
    //------------------------------------------------------------------------

    static abstract class CountingList 
        extends MarshallingSequentialList 
        implements Marshaller, Serializable, Evictable
    {

        /**
         * @param marshaller
         * @param list
         */
        CountingList(
            Marshaller marshaller
        ) {
            super(marshaller, null);
        }

        /* (non-Javadoc)
         */
        public void write(OutputStream stream) throws ServiceException {
            boolean propagate = super.list instanceof Reconstructable; 
            try {
                ObjectOutputStream buffer=new ObjectOutputStream(stream);
                buffer.writeBoolean(propagate);
                if(propagate){
                    buffer.flush();
                    ((Reconstructable)super.list).write(stream);
                } else {
                    buffer.flush();
                }
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int collectionSize = super.size();
            if(collectionSize == Integer.MAX_VALUE) {
                int iteratorCount = 0;
                for (
                        Iterator i = this.iterator();
                        i.hasNext();
                ){
                    i.next();
                    if(iteratorCount++ % LOCAL_EVALUATION_LIMIT == 0) {
                        collectionSize = super.size();
                        if(collectionSize != Integer.MAX_VALUE) {
                            return collectionSize;
                        }
                    }
                }
                return iteratorCount;
            } else {
                return collectionSize;
            }

        }

        /* (non-Javadoc)
         */
        public Object marshal(Object source) throws ServiceException {
            return source;
        }

        /* (non-Javadoc)
         */
        public Object unmarshal(Object source) throws ServiceException {
            return source;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            super.list = null;
        }

        /**
         * To be able to evict
         * 
         * @return the find result
         */
        protected abstract List newDelegate() throws ServiceException;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.collection.MarshallingSequentialList#getDelegate()
         */
        @Override
        protected List getDelegate() {
            if(super.list == null) try {
                super.list = newDelegate();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return super.list;
        }

    }

    //------------------------------------------------------------------------
    // Class ProviderCollection
    //------------------------------------------------------------------------

    /**
     * 
     */
    static class ProviderCollection 
        extends FilteringList 
        implements Selector, Evictable
    {

        /**
         * Constructor
         * 
         * @param persistentMembers
         * @param excludedMembers
         * @throws ServiceException
         */
        ProviderCollection(
            List providerManagedObjects,
            List exclude
        ) throws ServiceException {
            super(providerManagedObjects);
            this.exclude = exclude;
        }

        /* (non-Javadoc)
         */
        public boolean accept(Object candidate) {
            return !exclude.contains(candidate);
        }

        /**
         * 
         */
        protected boolean acceptAll (
        ){
            return exclude.isEmpty();
        }

        /* (non-Javadoc)
         */
        protected void removeInternal(
            Object object, 
            ListIterator iterator
        ) throws ServiceException {
            if(object instanceof Object_1){
                ((Object_1)object).objRemove();
            } else {
                super.removeInternal(object, iterator);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int providerCount = super.list.size();
            return providerCount == Integer.MAX_VALUE ?
                Integer.MAX_VALUE : // Size is unknown 
                    providerCount - exclude.size(); 
        }

        /**
         * 
         */
        private final List exclude;

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            if(super.list instanceof Evictable) {
                ((Evictable)super.list).evict();
            }
        }

    }

}
