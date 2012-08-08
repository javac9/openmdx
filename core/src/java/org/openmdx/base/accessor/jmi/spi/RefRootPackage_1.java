/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RefRootPackage_1.java,v 1.209 2010/08/09 13:12:05 hburger Exp $
 * Description: RefRootPackage_1 class
 * Revision:    $Revision: 1.209 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/09 13:12:05 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.beans.ExceptionListener;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.spi.AbstractTransaction_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.ConcurrentWeakRegistry;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.Registry;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.spi.AbstractPersistenceManager;
import org.openmdx.base.persistence.spi.MarshallingInstanceLifecycleListener;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Selector;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.w3c.jpa3.AbstractObject;
import org.w3c.spi.StateAccessor;

/**
 * RefRootPackage_1 class. This is at the same time the JMI root package which 
 * acts as a factory for creating application-specific packages by calling 
 * refPackage().
 */
@SuppressWarnings("unchecked")
public class RefRootPackage_1
    extends RefPackage_1
    implements Marshaller, ExceptionListener 
{

    /**
     * Constructor 
     *
     * @param viewManager
     * @param interactionSpec
     * @param delegate
     * @param packageImpls
     * @param userObjects
     * @param persistenceManagerFactory
     */
    private RefRootPackage_1(
        ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager,
        InteractionSpec interactionSpec,
        PersistenceManager delegate,
        Mapping_1_0 implementationMapper,
        Map<String, Object> userObjects,
        PersistenceManagerFactory persistenceManagerFactory
    ) {
        super(
            null, // refMofId
            null, // outermostPackage
            null // immediatePackage
        );
        this.viewManager = viewManager;
        this.interactionSpec = interactionSpec;
        this.delegate = (PersistenceManager_1_0)delegate;
        this.userObjects = userObjects;
        this.implementationMapper = implementationMapper;
        this.persistenceManagerFactory = persistenceManagerFactory;
        MarshallingInstanceLifecycleListener listener = new MarshallingInstanceLifecycleListener(this.registry, this);
        delegate.addInstanceLifecycleListener(listener);
        this.persistenceManager = new PersistenceManager_1(
            persistenceManagerFactory,
            listener                
        );
        if(this.userObjects != null) {
            for(Map.Entry<String,Object> userObject : this.userObjects.entrySet()) {
                this.persistenceManager.putUserObject(
                    userObject.getKey(), 
                    userObject.getValue()
                );
            }
            SharedObjects.propagate(
                this.persistenceManager, // target
                this.delegate // source
            );
        }        
    }
    
    /**
     * Constructor 
     * <p>
     * Called by<ul>
     * <li>LayerManagerFactory_2
     * <li>EntityManagerFactory_1
     * </ul> 
     *
     * @param persistenceManagerFactory
     * @param standardDelegate
     * @param mapping
     * @param userObjects
     * 
     * @see LayerManagerFactory_2
     * @see EntityManagerFactory_1
     */
    public RefRootPackage_1(
        PersistenceManagerFactory persistenceManagerFactory,
        PersistenceManager standardDelegate,
        Mapping_1_0 mapping,
        Map<String,Object> userObjects
    ){
        this(
            null, // viewManager
            null, // viewContext
            standardDelegate,
            mapping,
            userObjects, 
            persistenceManagerFactory
        );
    }

    /**
     * 
     */
    final Mapping_1_0 implementationMapper;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6682037424274032056L;

    /**
     * Contains the standard delegate
     */
    private PersistenceManager_1_0 delegate;

    private final InteractionSpec interactionSpec;
    protected ConcurrentMap<InteractionSpec,RefRootPackage_1> viewManager;
    private final PersistenceManagerFactory persistenceManagerFactory;
    protected final PersistenceManager_1_0 persistenceManager;

    /**
     * Registry containing <qualifiedName, JMI class> and <qualifiedName, JMI structs> 
     * entries, respectively. This map is shared by all JMI packages, i.e. if 
     * any package loads a class it is available for all other packages and 
     * therefore must be loaded only once per classloader. Moreover, these 
     * members do not have to be serialized. 
     */
    private transient Registry<String,Jmi1Class_1_0> classes;
    
    /**
     * The user objects have to be propagated to a <code>PersistenceManager</code>
     */
    private final Map<String,Object> userObjects;

    /**
     * 
     */
    private final ConcurrentMap<String,RefPackage> packages = new ConcurrentHashMap<String, RefPackage>();
    
    /**
     * Maps PersistenceCapable instances to RefObject_1 instances
     */
    private final Registry<PersistenceCapable,RefObject> registry = new ConcurrentWeakRegistry<PersistenceCapable, RefObject>();
    
    /**
     * Alternate package name format indicator
     */
    private static final String JMI1_PACKAGE_NAME_INDICATOR = "." + Names.JMI1_PACKAGE_SUFFIX;
    
    
    //-------------------------------------------------------------------------
    void unregister(
        PersistenceCapable key
    ){
        this.registry.remove(key);
    }
    
    //-------------------------------------------------------------------------
    void register(
        PersistenceCapable key,
        RefObject value
    ){
        this.registry.putUnlessPresent(key, value);
    }
    
    /**
     * Build an InvalidObjectException from a given cause
     * 
     * @param source
     * 
     * @return
     */
    protected InvalidObjectException toInvalidObjectException (
        Exception source
    ){
        return source instanceof InvalidObjectException ?
            (InvalidObjectException) source :
                new InaccessibleObject(source).getException();
    }

    /* (non-Javadoc)
     * @see java.beans.ExceptionListener#exceptionThrown(java.lang.Exception)
     */
//  @Override
    public void exceptionThrown(Exception cause) {
        throw this.toInvalidObjectException(cause);
    }

    /**
     * Determines an object's interaction spec
     * 
     * @param object
     * 
     * @return an object's interaction spec
     */
    private static InteractionSpec getInteractionsSpec(
        Object object
    ) throws ServiceException {
        return 
            object instanceof RefObject ? ((RefRootPackage_1) ((RefObject)object).refOutermostPackage()).refInteractionSpec() :
            object instanceof ObjectView_1_0 ? ((ObjectView_1_0)object).getInteractionSpec() :
            InteractionSpecs.NULL;
    }
    
    //-------------------------------------------------------------------------
//  @Override
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        if(source instanceof RefStruct_1_0) {
            return ((RefStruct_1_0)source).refDelegate();
        } else if(RefRootPackage_1.this.isTerminal()){
            if(source instanceof DelegatingRefObject_1_0) {
                throw new UnsupportedOperationException("Terminal RefPackage can not unmarshal DelegatingObjet_1_0");
            } else if(source instanceof RefObject_1_0) {
                return ((RefObject_1_0)source).refDelegate();
            } else {
                return source;
            }
        } else {
            if(source instanceof DelegatingRefObject_1_0) {
                return ((DelegatingRefObject_1_0)source).openmdxjdoGetDelegate();
            } else {
                return source;
            }
        }
    }
    
    //-------------------------------------------------------------------------  
//  @Override
    public Object marshal(
        Object source
    ) throws ServiceException {
        if(JDOHelper.getPersistenceManager(source) == this.persistenceManager) {
            return source;
        } else if(source instanceof RefStruct_1_0) {
            return this.refCreateStruct(((RefStruct_1_0)source).refDelegate());
        } else if (source instanceof Record) {
            return this.refCreateStruct((Record)source);
        } else {
            RefRootPackage_1 refPackage = this.refPackage(getInteractionsSpec(source));
            if(refPackage == this){
                if(source instanceof PersistenceCapable) {
                    PersistenceCapable pc = (PersistenceCapable) source;
                    RefObject target = this.registry.get(pc);
                    if(target == null) {
                        String className;
                        if(RefRootPackage_1.this.isTerminal()) {
                            className = ((DataObject_1_0)source).objGetClass();
                            if(className == null){
                                if(this.refInteractionSpec() == null) throw new ServiceException(
                                  BasicException.Code.DEFAULT_DOMAIN,
                                  BasicException.Code.NOT_FOUND,
                                  "Object class can not be determined",
                                  new BasicException.Parameter(
                                      "objectId", 
                                      pc.jdoIsPersistent() ? ((Path)pc.jdoGetObjectId()).toXRI() : null
                                  ),
                                  new BasicException.Parameter(
                                      "transientObjectId", 
                                      pc.jdoGetTransactionalObjectId()
                                  ),
                                  new BasicException.Parameter(
                                      "interactionSpec",
                                      refPackage.interactionSpec
                                  )
                                );
                                return null;
                            }
                        } else {
                            className = this.implementationMapper.getModelClassName(source.getClass().getInterfaces()[0]);
                        }
                        return this.registry.putUnlessPresent(
                            pc,
                            this.refClass(className).refCreateInstance(
                                Collections.singletonList(source)
                            )
                        );
                    } else {
                        return target;
                    }
                } else {
                    return source;
                }
            } else {
                return refPackage.marshal(source);
            }
        }
    }

    @Override
    public RefObject refObject(
        Path resourceIdentifier
    ) {
        try {
            if(this.refModel().containsSharedAssociation(resourceIdentifier)){
                String qualifier = resourceIdentifier.getBase();
                boolean persistent = qualifier.startsWith("!");
                return this.refContainer(
                    resourceIdentifier.getParent(), 
                    null
                ).refGet(
                    QualifierType.valueOf(persistent),
                    persistent ? qualifier.substring(1) : qualifier
                );
            } else {
                return (RefObject)this.marshal(
                    this.refDelegate().getObjectById(resourceIdentifier, false)
                );
            }
        } catch(ServiceException e) {
            throw new JmiServiceException(e);
        } catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } catch(JDOException e) {
            throw new JmiServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#refContainer(org.openmdx.compatibility.base.naming.Path)
     */
    @Override
    public <C extends RefContainer<?>> C refContainer(
        Path resourceIdentifier,
        Class<C> containerClass
    ) {
        try {
            if(this.refModel().containsSharedAssociation(resourceIdentifier)) {
                return (C) this.refObject(
                    resourceIdentifier.getParent()
                ).refGetValue(
                    resourceIdentifier.getBase()
                );
            } else if(this.isTerminal()) {
                DataObject_1_0 parent = (DataObject_1_0) this.delegate.getObjectById(
                    resourceIdentifier.getParent(),
                    false
                );
                RefContainer  container = new RefContainer_1(
                    this,
                    parent.objGetContainer(
                        resourceIdentifier.getBase()
                    )
                );
                return Classes.<C>newProxyInstance(
                    new Jmi1ContainerInvocationHandler((Marshaller)null, container),
                    containerClass == null ? RefContainer.class : containerClass, 
                    PersistenceCapableCollection.class,
                    Serializable.class
                );
            } else {
                RefObject authority = (RefObject) this.delegate.getObjectById(
                    resourceIdentifier.getPrefix(1),
                    false
                );
                RefPackage_1_0 delegate = (RefPackage_1_0) authority.refOutermostPackage();
                return Classes.<C>newProxyInstance(
                    new Jmi1ContainerInvocationHandler(
                        new Jmi1ObjectInvocationHandler.StandardMarshaller(this),
                        delegate.refContainer(
                            resourceIdentifier, 
                            containerClass
                        )
                    ),
                    containerClass == null ? RefContainer.class : containerClass, 
                    PersistenceCapableCollection.class,
                    Serializable.class
                );
            }
        } catch(RuntimeServiceException e) {
            throw new JmiServiceException(e);
        } catch(JDOException e) {
            throw new JmiServiceException(e);
        }  catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    @Override
    public RefPackage refImmediatePackage(
    ) {
        return this;
    }

    //-------------------------------------------------------------------------
    @Override
    public RefRootPackage_1 refOutermostPackage(
    ) {
        return this;
    }

    //-------------------------------------------------------------------------
    /**
     * The root package does not have a meta object.
     * 
     * @return RefObject null, the root package does not have a meta object.
     */
    @Override
    public RefObject refMetaObject(
    ) {
        return null;
    }

    //-------------------------------------------------------------------------
    @Override
    public RefPackage refPackage(
        String nestedPackageName
    ) {
        RefPackage refPackage = this.packages.get(nestedPackageName);
        if(refPackage == null) {
            try {
                int i = nestedPackageName.indexOf(JMI1_PACKAGE_NAME_INDICATOR);
                return Maps.putUnlessPresent(
                    this.packages,
                    nestedPackageName.intern(),
                    i < 0 ? this.refMapping().newPackage(this, nestedPackageName) : this.refPackage(nestedPackageName.substring(0, i).replace('.', ':'))
                );
            } catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            } 
        } else { 
            return refPackage;
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public Jmi1Class_1_0 refClass(
        String qualifiedClassName
    ) {
        if(this.classes == null) {
            this.classes = new ConcurrentWeakRegistry<String,Jmi1Class_1_0>();
        }
        Jmi1Class_1_0 refClass = this.classes.get(qualifiedClassName);
        if(refClass == null) {
            String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
            Jmi1Class_1_0 concurrent = this.classes.put(
                qualifiedClassName,
                refClass = (Jmi1Class_1_0) this.refPackage(packageName).refClass(qualifiedClassName)
            );
            return concurrent == null ? refClass : concurrent;
        } else {
            return refClass;
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public String refMofId(
    ) {
        return null;
    }
    
    /**
     * Retrieve a context specific RefPackage
     * 
     * @param viewContext
     * 
     * @return a context specific RefPackage
     */
    @Override
    public RefRootPackage_1 refPackage(
        InteractionSpec interactionSpec
    ){
        if(
            interactionSpec == InteractionSpecs.NULL ||
            (this.interactionSpec == null ? interactionSpec == null : this.interactionSpec.equals(interactionSpec))
        ) {
            return this;
        }
        try {
            RefRootPackage_1 refPackage;
            if(this.viewManager == null) {
                this.viewManager = new ConcurrentHashMap<InteractionSpec,RefRootPackage_1>();
                this.viewManager.put(InteractionSpecs.NULL, this);
                refPackage = null;
            } else {
                refPackage = this.viewManager.get(
                    interactionSpec == null ? InteractionSpecs.NULL : interactionSpec
                );
            }
            if(refPackage == null) {
                PersistenceManager_1_0 delegate = this.isTerminal() ?
                    this.delegate :
                    ((Delegating_1_0<RefPackage_1_0>)this.delegate).objGetDelegate().refPersistenceManager();
                refPackage = new RefRootPackage_1(
                    this.viewManager,
                    interactionSpec,
                    delegate.getPersistenceManager(interactionSpec), 
                    this.implementationMapper,
                    this.userObjects,
                    this.persistenceManagerFactory
                );
                RefRootPackage_1 oldPackage = this.viewManager.put(
                    interactionSpec,
                    refPackage
                );
                if(oldPackage != null) {
                    return oldPackage;
                }
            }
            return refPackage; 
        } catch (Exception exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "The given RefPackage is unable to create a view",
                    new BasicException.Parameter(
                        "objectFactory.class", 
                        this.delegate == null ? "<null>" : this.delegate.getClass().getName()
                    )
                )
            );
        }
    }

    /**
     * Retrieve the delegate
     * 
     * @return either the legacy or the standard delegate
     */
    @Override
    public PersistenceManager_1_0 refDelegate(){
        return this.delegate;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#refLegacyDelegate()
     */
    @Override
    public boolean isTerminal(
    ) {
        return this.delegate instanceof DataObjectManager_1_0;
    }

    /**
     * Close the outermost package
     */
    void close(
    ) {
        this.registry.close();
        this.delegate.close();
        this.delegate = null;
    }

    /**
     * Retrieves the JDO Persistence Manager Factory.
     * 
     * @return the JDO Persistence Manager Factory configured according to this package
     */
    @Override
    public final PersistenceManagerFactory refPersistenceManagerFactory (
    ) {
        return this.persistenceManagerFactory; 
    }

    /**
     * Retrieve the RefPackage's view context
     * 
     * @return the RefPackage's view context in case of a view,
     * <code>null</code> otherwise
     */
    @Override
    public InteractionSpec refInteractionSpec(
    ){
        return this.interactionSpec;
    }

    //-------------------------------------------------------------------------
    /**
     * Retrieves the JDO Persistence Manager delegating to this package.
     * 
     * @return the JDO Persistence Manager delegating to this package.
     */
    @Override
    public PersistenceManager_1_0 refPersistenceManager(
    ) {
        return this.persistenceManager;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.Jmi1Package_1_0#refImplementationMapper()
     */
    @Override
    public Mapping_1_0 refMapping() {
        return this.implementationMapper;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return this == that;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#hashCode()
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefPackage_1#toString()
     */
    @Override
    public String toString() {
        return "RefPackage <root>";
    }

    
    //-------------------------------------------------------------------------
    // Class InaccessibleObject
    //-------------------------------------------------------------------------

    /**
     * Inaccessible Object
     */
    class InaccessibleObject implements RefObject {

        /**
         * Constructor 
         *
         * @param e
         */
        InaccessibleObject(
            Exception e
        ) {
            this.cause = new InvalidObjectException(
                this,
                e.getMessage()
            );
            BasicException basic = BasicException.toExceptionStack(e);
            this.cause.initCause(basic);
            String path = basic.getCause(null).getParameter("request-path");
            Path objectId;
            if(path == null) {
                objectId = null;
            } else try {
                objectId = new Path(path);
            } catch (RuntimeException exception) {
                objectId = null;
            }
            this.objectId = objectId;
        }

        private final Path objectId;

        private final InvalidObjectException cause;

        InvalidObjectException getException(
        ){
            return this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refClass()
         */
    //  @Override
        public RefClass refClass(
        ) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refDelete()
         */
    //  @Override
        public void refDelete() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refImmediateComposite()
         */
    //  @Override
        public RefFeatured refImmediateComposite() {
            if(this.objectId == null) {
                throw this.cause;
            } else {
                int s = this.objectId.size() - 2;
                return s > 0 ? 
                    (RefFeatured)RefRootPackage_1.this.refPersistenceManager().getObjectById(this.objectId.getPrefix(s), false) : 
                        null;
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refIsInstanceOf(javax.jmi.reflect.RefObject, boolean)
         */
    //  @Override
        public boolean refIsInstanceOf(
            RefObject objType, 
            boolean considerSubtypes
        ) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefObject#refOutermostComposite()
         */
    //  @Override
        public RefFeatured refOutermostComposite() {
            if(this.objectId == null) {
                throw this.cause;
            } else {
                return this.objectId.size() > 2 ?
                    (RefFeatured)RefRootPackage_1.this.refPersistenceManager().getObjectById(this.objectId.getPrefix(1), false) : 
                        null;
            }
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(javax.jmi.reflect.RefObject)
         */
    //  @Override
        public Object refGetValue(RefObject feature) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refGetValue(java.lang.String)
         */
    //  @Override
        public Object refGetValue(String featureName) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(javax.jmi.reflect.RefObject, java.util.List)
         */
    //  @Override
        public Object refInvokeOperation(
            RefObject requestedOperation, 
            List args
        ) throws RefException {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refInvokeOperation(java.lang.String, java.util.List)
         */
    //  @Override
        public Object refInvokeOperation(
            String requestedOperation, 
            List args
        ) throws RefException {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(javax.jmi.reflect.RefObject, java.lang.Object)
         */
    //  @Override
        public void refSetValue(RefObject feature, Object value) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefFeatured#refSetValue(java.lang.String, java.lang.Object)
         */
    //  @Override
        public void refSetValue(String featureName, Object value) {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
         */
    //  @Override
        public RefPackage refImmediatePackage() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
         */
    //  @Override
        public RefObject refMetaObject() {
            throw this.cause;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
    //  @Override
        public String refMofId() {
            return this.objectId.toXRI();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
    //  @Override
        public RefPackage refOutermostPackage() {
            return RefRootPackage_1.this;
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
    //  @Override
        public Collection refVerifyConstraints(boolean deepVerify) {
            throw this.cause;
        }

    }


    //-------------------------------------------------------------------------
    // Class RefObjectHandler
    //-------------------------------------------------------------------------

    /**
     * RefObject Handler
     */
    static class RefObjectHandler implements Serializable, InvocationHandler  {

        /**
         * Constructor 
         *
         * @param primary
         * @param secondary
         */
        RefObjectHandler(
            RefObject primary,
            RefObject secondary
        ) {
            this.primary = primary;
            this.secondary = secondary;
            this.mapping = null;
        }

        private static final long serialVersionUID = -5495786050475461969L;
        private final RefObject primary;
        private final RefObject secondary;
        private transient Map<Method,Method> mapping;

        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
    //  @Override
        public Object invoke(
            Object proxy, 
            Method method, 
            Object[] args
        ) throws Throwable {
            Method primaryMethod = this.getPrimaryMethod(method);
            try {
                return primaryMethod != null ?
                    primaryMethod.invoke(this.primary, args) :
                        method.invoke(this.secondary, args);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }

        }

        /**
         * Retrieves the primary method
         * 
         * @param method
         * 
         * @return
         */
        private synchronized Method getPrimaryMethod(
            Method secondaryMethod
        ){
            Method primaryMethod;
            boolean tested;
            if(this.mapping == null) {
                this.mapping = new ConcurrentHashMap<Method,Method>();
                primaryMethod = null;
                tested = false;
            } else {
                primaryMethod = this.mapping.get(secondaryMethod);
                tested = primaryMethod != null || this.mapping.containsKey(secondaryMethod); 
            }
            if(!tested) {
                try {
                    primaryMethod = this.primary.getClass().getMethod(
                        secondaryMethod.getName(), 
                        secondaryMethod.getParameterTypes()
                    );                    
                } catch (NoSuchMethodException exception) {
                    // remember it by setting the value to null
                }
                this.mapping.put(secondaryMethod, primaryMethod);
            }
            return primaryMethod;
        }       

    }

    //-------------------------------------------------------------------------
    // Class PersistenceManager_1
    //-------------------------------------------------------------------------

    /**
     * The PersistenceManager associated with the outermost package
     */
    class PersistenceManager_1
        extends AbstractPersistenceManager
        implements PersistenceManager_1_0, Delegating_1_0<RefRootPackage_1>
    {

        /**
         * Constructor 
         *
         * @param factory
         * @param listener
         */
        PersistenceManager_1(
            PersistenceManagerFactory factory, 
            MarshallingInstanceLifecycleListener listener
        ) {
            super(
                factory, 
                listener
            );
            this.transaction = new AbstractTransaction_1(){

                @Override
                protected Transaction getDelegate() {
                    return RefRootPackage_1.this.refDelegate().currentTransaction();
                }

                public PersistenceManager getPersistenceManager() {
                    return PersistenceManager_1.this;
                }
             
            };
        }

        private final Transaction transaction;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#getUserObject(java.lang.Object)
         */
        @Override
        public Object getUserObject(Object key) {
            return key == RefPackage.class ? RefRootPackage_1.this : super.getUserObject(key);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#close()
         */
        @Override
        public void close() {
            if(!this.isClosed()) {
                super.close();
                RefRootPackage_1.this.close();
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#currentTransaction()
         */
    //  @Override
        public Transaction currentTransaction(
        ) {
            return this.transaction;
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
         */
    //  @Override
        public void evict(Object pc) {
            // The hint is ignored at the moment...
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#evictAll(java.util.Collection)
         */
        @Override
        public void evictAll(Collection pcs) {
            if(pcs instanceof PersistenceCapableCollection) {
                ((PersistenceCapableCollection)pcs).openmdxjdoEvict(true, true);
            } else {
                super.evictAll(pcs);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#evictAll()
         */
    //  @Override
        public void evictAll(
        ) {
            RefRootPackage_1.this.refDelegate().evictAll();
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getManagedObjects()
         */
    //  @Override
        public Set getManagedObjects(
        ) {
            return new MarshallingSet(
                RefRootPackage_1.this,
                RefRootPackage_1.this.refDelegate().getManagedObjects()
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
         */
    //  @Override
        public Set getManagedObjects(
            EnumSet<ObjectState> states
        ) {
            return new MarshallingSet(
                RefRootPackage_1.this,
                RefRootPackage_1.this.refDelegate().getManagedObjects(states)
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
         */
    //  @Override
        public Set getManagedObjects(
            final Class... classes
        ) {
            return Sets.subSet(
                this.getManagedObjects(),
                new Selector (){

                    public boolean accept(Object candidate) {
                        for(Class superClass : classes) {
                            if(superClass.isInstance(candidate)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    
                }
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
         */
    //  @Override
        public Set getManagedObjects(
            final EnumSet<ObjectState> states, 
            final Class... classes
        ) {
            return Sets.subSet(
                this.getManagedObjects(states),
                new Selector (){

                    public boolean accept(Object candidate) {
                        for(Class superClass : classes) {
                            if(superClass.isInstance(candidate)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    
                }
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
         */
    //  @Override
        public void refresh(Object pc) {
            Object objectId = JDOHelper.getObjectId(pc);
            if(objectId != null) {
                PersistenceManager_1_0 delegate = RefRootPackage_1.this.refDelegate(); 
                delegate.refresh(
                    delegate.getObjectById(objectId)
                );
            }
        } 

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#refreshAll()
         */
    //  @Override
        public void refreshAll(
        ) {
            RefRootPackage_1.this.refDelegate().refreshAll();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#refreshAll(java.util.Collection)
         */
        @Override
        public void refreshAll(
            Collection pcs
        ) {
            if(pcs instanceof PersistenceCapableCollection) {
                ((PersistenceCapableCollection)pcs).openmdxjdoRefresh();
            } else {
                super.refreshAll(pcs);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery()
         */
    //  @Override
        public Query newQuery(
        ) {
            throw new UnsupportedOperationException(
                "The the Class of the candidate instances must be specified when " +
                "creating a new Query instance."
            );            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
         */
    //  @Override
        public Query newQuery(
            Object compiled
        ) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
         */
    //  @Override
        public Query newQuery(
            String query
        ) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
         */
    //  @Override
        public Query newQuery(
            String language, 
            Object query
        ) {
            if(Queries.QUERY_LANGUAGE.equals(language)) {
                Query_2Facade openmdxQuery;
                try {
                    openmdxQuery = Query_2Facade.newInstance((MappedRecord)query);
                }  catch(Exception e) {
                    throw BasicException.initHolder(
                        new JDOFatalUserException(
                            "Unknown predicate for query",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter(
                                    "expected", 
                                    Map.class.getName()
                                ),
                                new BasicException.Parameter(
                                    "actual",
                                    query == null ? null : query.getClass().getName()
                                )
                            )
                        )
                    );
                }
                String queryString = openmdxQuery.getQuery();
                Path resourceIdentifier = openmdxQuery.getPath();
                RefContainer<?> candidates = null;
                if(resourceIdentifier != null) {
                    candidates = RefRootPackage_1.this.refContainer(
                        resourceIdentifier, 
                        null
                    );
                }                
                Query cciQuery = null;
                try {
                    if((queryString != null) && queryString.startsWith("<?xml")) {
                        org.openmdx.base.query.Filter filter = (org.openmdx.base.query.Filter)JavaBeans.fromXML(queryString);
                        cciQuery = RefRootPackage_1.this.refCreateQuery(
                            openmdxQuery.getQueryType(),
                            true, // subclasses
                            filter
                        );
                        return org.openmdx.base.persistence.spi.Queries.prepareQuery(
                            cciQuery,
                            candidates,
                            null
                        );                    
                    }
                    else {
                        cciQuery = RefRootPackage_1.this.refCreateQuery(
                            openmdxQuery.getQueryType(),
                            true, // subclasses
                            null // filter
                        );
                        return org.openmdx.base.persistence.spi.Queries.prepareQuery(
                            cciQuery,
                            candidates,
                            queryString
                        );
                    }
                } catch (ServiceException exception) {
                    throw new JDOFatalUserException(
                        "Query creation failure",
                        exception
                    );
                }
            } 
            else {
                throw BasicException.initHolder(
                    new JDOFatalUserException(
                        "Unsupported query language",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter(
                                "accepted", 
                                org.openmdx.base.persistence.cci.Queries.QUERY_LANGUAGE
                            ),
                            new BasicException.Parameter(
                                "actual",
                                language
                            )
                        )
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
         */
    //  @Override
        public Query newQuery(
            Class cls
        ) {       
            try {
                return RefRootPackage_1.this.refCreateQuery(
                    RefRootPackage_1.this.implementationMapper.getModelClassName(cls), 
                    true, // subclasses
                    null // filter 
                );
            } catch (ServiceException exception) {
                throw new JDOUserException(
                    "Query creation failure", 
                    exception.getCause()
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
         */
    //  @Override
        public Query newQuery(
            Extent cln
        ) {
            try {
                return RefRootPackage_1.this.refCreateQuery(
                    RefRootPackage_1.this.implementationMapper.getModelClassName(cln.getCandidateClass()), 
                    cln.hasSubclasses(),
                    null // filter 
                );
            } catch (ServiceException exception) {
                throw new JDOUserException(
                    "Query creation failure", 
                    exception.getCause()
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
         */
    //  @Override
        public Query newQuery(Class cls, Collection cln) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
         */
    //  @Override
        public Query newQuery(Class cls, String filter) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
         */
    //  @Override
        public Query newQuery(Class cls, Collection cln, String filter) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
         */
    //  @Override
        public Query newQuery(Extent cln, String filter) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
         */
    //  @Override
        public Query newNamedQuery(Class cls, String queryName) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
         */
    //  @Override
        public <T> Extent<T> getExtent(
            Class<T> persistenceCapableClass,
            boolean subclasses
        ) {
            return new Extent_1<T>(this, persistenceCapableClass, subclasses);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
         */
    //  @Override
        public Object getObjectById(
            Object oid, 
            boolean validate
        ) {
            if(oid instanceof UUID) {
                try {
                    return RefRootPackage_1.this.marshal(
                        RefRootPackage_1.this.refDelegate().getObjectById(oid)
                    );
                } catch(ServiceException e) {
                    throw new JDOUserException(
                        e.getCause().getDescription(),
                        e.getCause()
                    );
                } catch(RuntimeServiceException e) {
                    throw new JDOUserException(
                        e.getCause().getDescription(),
                        e.getCause()
                    );
                }
            } else if(oid instanceof Path) {
                try {
                    Path resourceIdentifier = (Path) oid;
                    return resourceIdentifier.size() % 2 == 1 ?
                        RefRootPackage_1.this.refObject(resourceIdentifier) :
                        RefRootPackage_1.this.refContainer(resourceIdentifier, null);
                } catch (JmiServiceException exception) {
                    throw exception.getExceptionCode() == BasicException.Code.NOT_FOUND ? new JDOObjectNotFoundException(
                        "Object not found",
                        exception.getCause()
                    ) : new JDOUserException(
                        "Unable to get object",
                        exception.getCause()
                    );
                }
            } else if (oid instanceof TransientContainerId) {
                TransientContainerId resourceIdentifier = (TransientContainerId) oid;
                try {
                    RefObject parent = (RefObject) RefRootPackage_1.this.marshal(
                        RefRootPackage_1.this.refDelegate().getObjectById(resourceIdentifier.getParent())
                    );
                    return parent.refGetValue(resourceIdentifier.getFeature());
                } catch (ServiceException exception) {
                    throw new JDOUserException(
                        exception.getCause().getDescription(),
                        exception.getCause()
                    );
                } catch(RuntimeServiceException exception) {
                    throw new JDOUserException(
                        exception.getCause().getDescription(),
                        exception.getCause()
                    );
                }
            }
            throw oid == null ? BasicException.initHolder(
                new JDOFatalUserException(
                    "Null object id",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
            ) : BasicException.initHolder(
                new JDOFatalUserException(
                    "Unsupported object id class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("supported", Path.class.getName(), UUID.class.getName()),
                        new BasicException.Parameter("actual", oid.getClass().getName())
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
         */
    //  @Override
        public Object getObjectById(Object oid) {
            return this.getObjectById(oid, true);
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
         */
    //  @Override
        public Object getObjectId(Object pc) {
            return pc instanceof PersistenceCapable ?
                ((PersistenceCapable)pc).jdoGetObjectId() :
                    null;
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
         */
    //  @Override
        public Object getTransactionalObjectId(Object pc) {
            return pc instanceof PersistenceCapable ?
                ((PersistenceCapable)pc).jdoGetTransactionalObjectId() :
                    null;
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
         */
    //  @Override
        public Object newObjectIdInstance(Class pcClass, Object key) {
            return new Path(key.toString());
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#newInstance(java.lang.Class)
         */
    //  @Override
        public <T> T newInstance(Class<T> pcClass) {
            try {
                return (T) RefRootPackage_1.this.refClass(
                    RefRootPackage_1.this.implementationMapper.getModelClassName(pcClass)
                ).refCreateInstance(
                    null
                );
            } catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            } 
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
         */
    //  @Override
        public <T> T makePersistent(T pc) {
            if(pc instanceof AbstractObject) {
                if(this.getCopyOnAttach()) {
                    AbstractObject source = (AbstractObject) pc;
                    RefObject target;
                    Path xri = (Path) JDOHelper.getTransactionalObjectId(source);
                    if(JDOHelper.isDetached(pc)) {
                        //
                        // Attach Copy
                        //
                        DataObject_1_0 dataObject = RefRootPackage_1.this.refDelegate().makePersistent(
                            new DataObject_1(
                                xri,
                                JDOHelper.getVersion(source)
                            )
                        );
                        target = (RefObject) this.getObjectById(
                            JDOHelper.getTransactionalObjectId(dataObject)
                        );
                        if(JDOHelper.isDirty(pc)) try { 
                            this.copy(
                                target,
                                source,
                                false //  // JPA -> JMI
                            );
                        } catch (ServiceException exception) {
                            throw new JDOFatalUserException(
                                "Unable to copy the values from the detached JPA instance to the JMI instance", 
                                exception,
                                pc
                            );
                        }
                    } else {
                        //
                        // Make Persistent 
                        //
                        try {
                            target = this.newInstance(
                                RefRootPackage_1.this.refMapping().getInstanceInterface(pc.getClass())
                            );
                            copy(
                                target,
                                source,
                                false //  // JPA -> JMI
                            );
                        } catch (ServiceException exception) {
                            throw new JDOFatalUserException(
                                "Unable to copy the values from the transient JPA instance to the JMI instance", 
                                exception,
                                pc
                            );
                        }
                        try {
                            RefContainer jmiContainer = (RefContainer) this.getObjectById(xri.getParent());
                            boolean persistent = xri.getBase().startsWith("!");
                            jmiContainer.refAdd(
                                QualifierType.valueOf(persistent),
                                xri.getBase().substring(persistent ? 1 : 0),
                                target
                            );
                        } catch (JmiException exception) {
                            throw new JDOUserException(
                                "Unable to add the JMI instance to its container", 
                                exception,
                                pc
                            );
                        }
                    }
                    return (T) target;
                } else {
                    throw new JDOUnsupportedOptionException(
                        ConfigurableProperty.CopyOnAttach + " must be true"
                    );
                }
            } else {
                return RefRootPackage_1.this.refDelegate().makePersistent(pc);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
         */
    //  @Override
        public void deletePersistent(
            Object pc
        ) {
            if(pc instanceof PersistenceCapable && pc instanceof RefObject) {
                PersistenceCapable jdoObject = (PersistenceCapable) pc;
                if(this != jdoObject.jdoGetPersistenceManager()) throw new JDOUserException(
                    "The object is managed by a different PersistenceManager",
                    jdoObject
                );
                RefObject jmiObject = (RefObject) pc;
                try {
                    jmiObject.refDelete();
                } catch (JmiServiceException exception) {
                    throw new JDOUserException(
                        "Deletion failure",
                        exception,
                        jdoObject
                    );
                }
            } else throw new JDOUserException(
                "The object to be deleted is not an instance of PersistenceCapable and RefObject"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
         */
    //  @Override
        public void makeTransient(Object pc, boolean useFetchPlan) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
         */
        @Override
        public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
         */
//      @Override
        public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
         */
    //  @Override
        public void makeTransactional(Object pc) {
            Object objectId = JDOHelper.getObjectId(pc);
            if(objectId != null) {
                PersistenceManager_1_0 delegate = RefRootPackage_1.this.refDelegate(); 
                delegate.makeTransactional(
                    delegate.getObjectById(objectId)
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
         */
    //  @Override
        public void makeNontransactional(Object pc) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
         */
    //  @Override
        public void retrieve(Object pc, boolean useFetchPlan) {
            Object objectId = JDOHelper.getObjectId(pc);
            if(objectId != null) {
                PersistenceManager_1_0 delegate = RefRootPackage_1.this.refDelegate(); 
                delegate.retrieve(
                    delegate.getObjectById(objectId),
                    useFetchPlan
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AbstractPersistenceManager#retrieveAll(java.util.Collection, boolean)
         */
        @Override
        public void retrieveAll(
            Collection pcs, 
            boolean useFetchPlan
        ) {
            if(pcs instanceof PersistenceCapableCollection) {
                ((PersistenceCapableCollection)pcs).openmdxjdoRetrieve(
                    useFetchPlan ? PersistenceManager_1.this.getFetchPlan() : null
                );
            } else {
                super.retrieveAll(pcs, useFetchPlan);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
         */
    //  @Override
        public Class<Path> getObjectIdClass(Class cls) {
            return RefObject_1_0.class.isAssignableFrom(cls) ? Path.class : null;
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
         */
    //  @Override
        public <T> T detachCopy(T pc) {
            if(JDOHelper.getPersistenceManager(pc) == this) {
                RefObject_1_0 jmiObject = (RefObject_1_0) pc;
                try {
                    AbstractObject jpaObject = RefRootPackage_1.this.refMapping(
                    ).getClassMapping(
                        jmiObject.refClass().refMofId()
                    ).getInstanceClass(
                    ).newInstance(
                    );
                    copy(
                        jmiObject,
                        jpaObject,
                        true // JMI -> JPA
                    );
                    StateAccessor.getInstance().initializeDetachedObject(
                        jpaObject,
                        jmiObject.refGetPath(),
                        JDOHelper.getVersion(pc)
                    );
                    return (T) jpaObject;
                } catch (InstantiationException exception) {
                    throw new JDOFatalUserException(
                        "Unable to instantiate the corresponding JPA class",
                        exception,
                        pc
                    );
                } catch (IllegalAccessException exception) {
                    throw new JDOFatalUserException(
                        "Unable to instantiate the corresponding JPA class",
                        exception,
                        pc
                    );
                } catch (ServiceException exception)  {
                    throw new JDOUserException(
                        "Unable to propagate the values to the JPA instance",
                        exception,
                        pc
                    );
                }
            } else {
                throw new JDOUserException(
                    "The object is not managed by this persistence manager",
                    pc
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#flush()
         */
    //  @Override
        public void flush() {
            RefRootPackage_1.this.refDelegate().flush();          
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#checkConsistency()
         */
    //  @Override
        public void checkConsistency() {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getFetchPlan()
         */
    //  @Override
        public FetchPlan getFetchPlan() {
            return RefRootPackage_1.this.refDelegate().getFetchPlan();
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
         */
    //  @Override
        public Sequence getSequence(String name) {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");            
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.object.spi.AbstractPersistenceManager#getDataStoreConnection()
         */
    //  @Override
        public JDOConnection getDataStoreConnection() {
            return RefRootPackage_1.this.refDelegate().getDataStoreConnection();
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getMultithreaded()
         */
    //  @Override
        public boolean getMultithreaded(
        ){
            return this.getPersistenceManagerFactory().getMultithreaded();
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
         */
    //  @Override
        public void setMultithreaded(boolean flag) {
            if(flag != getMultithreaded()) throw new javax.jdo.JDOUnsupportedOptionException(
                "The " + ConfigurableProperty.Multithreaded.qualifiedName() + 
                " property can be set at factory level only"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.PersistenceManager#getServerDate()
         */
    //  @Override
        public Date getServerDate() {
            throw new UnsupportedOperationException("This JDO operation is not yet supported");
        }

        /**
         * Copy the objects' values
         * 
         * @param jmiObject
         * @param jpaObject
         * @param detach <code>true</code> in case of detach, <code>false</code> in case of attach
         * 
         * @throws ServiceException 
         */
        private void copy(
            RefObject jmiObject,
            AbstractObject jpaObject,
            boolean detach
        ) throws ServiceException{
            Model_1_0 model = refModel();
            StateAccessor stateAccessor = StateAccessor.getInstance();
            String modelClass = jmiObject.refClass().refMofId(); 
            ModelElement_1_0 objectClass = model.getElement(modelClass);
            Map<String, ModelElement_1_0> modelAttributes = model.getAttributeDefs(
                objectClass, 
                false, // includeSubtypes
                detach // includeDerived
            );
            for(Map.Entry<String, ModelElement_1_0> entry : modelAttributes.entrySet()) {
                ModelElement_1_0 attributeDef = entry.getValue();
                boolean reference = model.isReferenceType(attributeDef);
                String jmiFeature = entry.getKey();
                String jpaFeature = reference ? jmiFeature + "_Id" : jmiFeature;
                String multiplicity = ModelUtils.getMultiplicity(attributeDef);
                boolean multivalued = 
                    !Multiplicities.SINGLE_VALUE.equals(multiplicity) && 
                    !Multiplicities.OPTIONAL_VALUE.equals(multiplicity) &&
                    !Multiplicities.STREAM.equals(multiplicity);
                String typeName = (String) model.getElementType(attributeDef).objGetValue("qualifiedName");
                Class<? extends AbstractObject> jpaClass = jpaObject.getClass();                
                Object source;
                Object target;
                if(detach) {
                    //
                    // Detach
                    //
                    source = reference ? PersistenceHelper.getFeatureReplacingObjectById(jmiObject, jmiFeature) : jmiObject.refGetValue(jmiFeature);
                    if(multivalued) {
                        String get = Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                jpaFeature,
                                true, // forQuery
                                PrimitiveTypes.BOOLEAN.equals(typeName), // forBoolean
                                false // singleValued
                            )
                        );
                        try {
                            target = jpaClass.getMethod(get).invoke(jpaObject);
                        } catch (Exception exception) {
                            throw new ServiceException(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_MEMBER_NAME,
                                "Unable to invoke JPA method",
                                new BasicException.Parameter("model-class", modelClass),
                                new BasicException.Parameter("jmi-feature", jmiFeature),
                                new BasicException.Parameter("jpa-class", jpaClass.getName()),
                                new BasicException.Parameter("jpa-feature", jpaFeature),
                                new BasicException.Parameter("jpa-method", get)
                            );
                        }
                        if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                            SortedMap collection = (SortedMap) target;
                            collection.clear();
                            if(reference) {
                                for(Map.Entry<Integer, Path> e : ((SortedMap<Integer,Path>)source).entrySet()) {
                                    collection.put(e.getKey(), stateAccessor.toObjectId(e.getValue()));
                                }
                            } else {
                                collection.putAll((SortedMap<Integer,?>)source);
                            }
                        } else {
                            Collection collection = (Collection) target;
                            collection.clear();
                            if(reference) {
                                for(Path p : (Collection<Path>) source) {
                                    collection.add(stateAccessor.toObjectId(p));
                                }
                            } else {
                                collection.addAll((Collection<?>)source);
                            }
                        }
                    } else if (ModelUtils.isDerived(attributeDef)){
                        Object value = 
                            PrimitiveTypes.DATE.equals(typeName) ? org.w3c.jpa3.Date.toJDO((XMLGregorianCalendar) source) :
                            PrimitiveTypes.DATETIME.equals(typeName) ? org.w3c.jpa3.DateTime.toJDO((Date) source) :
                            source;
                        try {
                            jpaClass.getField(jpaFeature).set(jpaObject, value);
                        } catch (Exception exception) {
                            throw new ServiceException(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_MEMBER_NAME,
                                "Unable to set JPA field",
                                new BasicException.Parameter("model-class", modelClass),
                                new BasicException.Parameter("jmi-feature", jmiFeature),
                                new BasicException.Parameter("jmi-value", source),
                                new BasicException.Parameter("jpa-class", jpaClass.getName()),
                                new BasicException.Parameter("jpa-feature", jpaFeature),
                                new BasicException.Parameter("jpa-value", value)
                                
                            );
                        }
                    } else {
                        String set = Identifier.OPERATION_NAME.toIdentifier(
                            AbstractNames.openmdx2AccessorName(
                                jpaFeature,
                                false, // forQuery
                                PrimitiveTypes.BOOLEAN.equals(typeName), // forBoolean
                                true // singleValued
                            )
                        );
                        try {
                            for(Method method : jpaClass.getMethods()) {
                                if(
                                    method.getName().equals(set) &&
                                    (method.getParameterTypes().length == 1) &&
                                    (method.getReturnType() == void.class)
                                    // Note: The argument type is ignored for the moment 
                                ){
                                    method.invoke(jpaObject, source);
                                    break;
                                }
                            }
                        } catch (Exception exception) {
                            throw new ServiceException(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_MEMBER_NAME,
                                "Unable to invoke JPA method",
                                new BasicException.Parameter("model-class", modelClass),
                                new BasicException.Parameter("jmi-feature", jmiFeature),
                                new BasicException.Parameter("jpa-class", jpaClass.getName()),
                                new BasicException.Parameter("jpa-feature", jpaFeature),
                                new BasicException.Parameter("jpa-method", set)
                            );
                        }
                    }
                } else {
                    //
                    // Attach
                    //
                    String get = Identifier.OPERATION_NAME.toIdentifier(
                        AbstractNames.openmdx2AccessorName(
                            jpaFeature,
                            true, // forQuery
                            PrimitiveTypes.BOOLEAN.equals(model.getElementType(attributeDef).objGetValue("qualifiedName")), // forBoolean
                            !multivalued // singleValued
                        )
                    );
                    try {
                        source = jpaClass.getMethod(get).invoke(jpaObject);
                    } catch (Exception exception) {
                        throw new ServiceException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_MEMBER_NAME,
                            "Unable to invoke JPA method",
                            new BasicException.Parameter("model-class", modelClass),
                            new BasicException.Parameter("jmi-feature", jmiFeature),
                            new BasicException.Parameter("jpa-class", jpaClass.getName()),
                            new BasicException.Parameter("jpa-feature", jpaFeature),
                            new BasicException.Parameter("jpa-method", get)
                        );
                    }
                    PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(jmiObject);
                    if(multivalued) {
                        target = jmiObject.refGetValue(jmiFeature);
                        if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                            SortedMap collection = (SortedMap) target;
                            collection.clear();
                            if(reference) {
                                for(Map.Entry<Integer, String> e : ((SortedMap<Integer,String>)source).entrySet()) {
                                    collection.put(
                                        e.getKey(), 
                                        persistenceManager.getObjectById(stateAccessor.toTransactionalObjectId(e.getValue()))
                                    );
                                }
                            } else {
                                collection.putAll((SortedMap<Integer,?>)source);
                            }
                        } else {
                            Collection collection = (Collection) target;
                            collection.clear();
                            if(reference) {
                                for(String p : (Collection<String>) source) {
                                    collection.add(persistenceManager.getObjectById(stateAccessor.toTransactionalObjectId(p)));
                                }
                            } else {
                                collection.addAll((Collection<?>)source);
                            }
                        }
                    } else {
                        jmiObject.refSetValue(jmiFeature, source);
                    }
                }
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
         */
    //  @Override
        public RefRootPackage_1 objGetDelegate(
        ) {
            return RefRootPackage_1.this;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getPersistenceManager(javax.resource.cci.InteractionSpec)
         */
    //  @Override
        public PersistenceManager_1_0 getPersistenceManager(
            InteractionSpec interactionSpec
        ) {
            return RefRootPackage_1.this.refPackage(interactionSpec).refPersistenceManager();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
         */
    //  @Override
        public Object getFeatureReplacingObjectById(
            UUID transientObjectId,
            String featureName
        ) {
            return RefRootPackage_1.this.refDelegate().getFeatureReplacingObjectById(transientObjectId, featureName);
        }
        
    }
    
}
