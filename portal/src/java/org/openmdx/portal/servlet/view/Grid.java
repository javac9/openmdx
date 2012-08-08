/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Grid.java,v 1.92 2010/06/01 10:32:11 wfro Exp $
 * Description: GridControl
 * Revision:    $Revision: 1.92 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/01 10:32:11 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Extension;
import org.openmdx.base.query.IsBetweenCondition;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.query.SoundsLikeCondition;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;
import org.openmdx.portal.servlet.Filter;
import org.openmdx.portal.servlet.Filters;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.FieldDef;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.control.GridControl;
import org.openmdx.portal.servlet.texts.Texts_1_0;

@SuppressWarnings("unchecked")
public abstract class Grid
    extends ControlState
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public Grid(
        GridControl control,
        ObjectView view,
        String lookupType
    ) {
        super(
            control,
            view
        );
        ApplicationContext application = this.view.getApplicationContext();
        Texts_1_0 texts = application.getTexts();
        
        this.columnSortOrders = new HashMap(control.getInitialColumnSortOrders());
        this.lookupType = lookupType;
        this.showRows = this.showGridContentOnInit();
        this.dataBinding = application.getPortalExtension().getDataBinding(
            control.getObjectContainer().getDataBindingName() 
        );
        
        // Filters
        if(control.getObjectContainer().isReferenceIsStoredAsAttribute()) {
            this.filters = new Filter[]{};
        }
        else {
            String containerId = control.getContainerId();
            String baseFilterId = containerId.substring(containerId.indexOf("Ref:") + 4);
            Filters filters = application.getFilters(
                control.getQualifiedReferenceName()
            );
            // Default filter
            String defaultFilterPropertyName = control.getPropertyName(
                control.getContainerId(),
                GridControl.PROPERTY_DEFAULT_FILTER
            );
            // Fallback to old-style property name for default filter
            if(
                (application.getSettings().getProperty(defaultFilterPropertyName) == null) &&
                (baseFilterId.indexOf(":") < 0) 
            ) {
                defaultFilterPropertyName = control.getPropertyName(
                    control.getQualifiedReferenceName(),
                    GridControl.PROPERTY_DEFAULT_FILTER
                );                
            }
            // Override DEFAULT filter, if at least ALL and DEFAULT filter are defined
            // and settings contains a default filter declaration
            Filter defaultFilter = null;
            if(application.getSettings().getProperty(defaultFilterPropertyName) != null) {
                try {
                    defaultFilter = (Filter)JavaBeans.fromXML(
                    	new String(
                    		Base64.decode(application.getSettings().getProperty(defaultFilterPropertyName))
                    	)
                    );
                    defaultFilter.setName(Filters.DEFAULT_FILTER_NAME);
                    defaultFilter.setGroupName("0");
                    defaultFilter.setIconKey(WebKeys.ICON_FILTER_DEFAULT);
                }
                // Ignore if decoding fails
                catch(Exception e) {
                	SysLog.info("can not get default filter from settings", e.getMessage());                    
                }
            }
            this.filters = filters.getPreparedFilters(
                baseFilterId,
                defaultFilter
            );
        }
        
        // Creators and template rows
        Model_1_0 model = application.getModel();
        org.openmdx.ui1.jmi1.ObjectContainer objectContainer = control.getObjectContainer();
        this.isComposite = !objectContainer.isReferenceIsStoredAsAttribute();
        this.isChangeable = objectContainer.isChangeable();

        Map objectCreators = null;
        List templateRows = null;                
        if(!this.isChangeable) {
            objectCreators = null;
            this.addObjectAction = null;
            this.removeObjectAction = null;
        }
        else if(!this.isComposite) {
          Action addObjectAction = null;
          Action removeObjectAction = null;
          objectCreators = null;
          try {
            // objectContainer.getReferenceName() contains the unqualified
            // name of the reference. Lookup the reference in model.
            ModelElement_1_0 reference = model.getFeatureDef(
              model.getElement(control.getContainerClass()), 
              objectContainer.getReferenceName(), 
              false
            );
            addObjectAction = new Action(
                Action.EVENT_ADD_OBJECT,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                    new Action.Parameter(Action.PARAMETER_NAME, "+"),
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
                },
                "add object",
                true
            );
            removeObjectAction = new Action(
                Action.EVENT_ADD_OBJECT,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                    new Action.Parameter(Action.PARAMETER_NAME, "-"),
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_REFERENCE, (String)reference.objGetValue("qualifiedName"))
                },
                "remove object",
                true
            );
          }
          catch(ServiceException e) {
        	  SysLog.warning(e.getMessage(), e.getCause());
          }
          this.addObjectAction = addObjectAction;
          this.removeObjectAction = removeObjectAction;
        }
        else {
            this.addObjectAction = null;
            this.removeObjectAction = null;
            objectCreators = new TreeMap(); // sorted map of creators. key is inspector label
            templateRows = new ArrayList();
            try {
                ModelElement_1_0 referencedType = model.getElement(objectContainer.getReferencedTypeName());
                int ii = 0;
                for(
                    Iterator i = referencedType.objGetList("allSubtype").iterator(); 
                    i.hasNext();
                    ii++
                ) {
                    ModelElement_1_0 subtype = model.getElement(i.next());
                    if(!((Boolean)subtype.objGetValue("isAbstract")).booleanValue()) {
                        String forClass = (String)subtype.objGetValue("qualifiedName");
                        org.openmdx.ui1.jmi1.AssertableInspector assertableInspector = application.getAssertableInspector(forClass);
                        if(assertableInspector.isChangeable()) {
                            
                            // Object creator
                            objectCreators.put(
                                ApplicationContext.getOrderAsString(assertableInspector.getOrder()) + ":" + forClass,
                                new Action(
                                    Action.EVENT_NEW_OBJECT,
                                    new Action.Parameter[]{
                                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                                        new Action.Parameter(Action.PARAMETER_FOR_REFERENCE, objectContainer.getReferenceName()),
                                        new Action.Parameter(Action.PARAMETER_FOR_CLASS, forClass)
                                    },
                                    application.getLabel(forClass),
                                    application.getIconKey(forClass),
                                    true
                                )
                            );
                            
                            // Template row for each creator
                            int nCols = java.lang.Math.min(
                                objectContainer.getMember().size(),
                                Grid.MAX_COLUMNS
                            ); 
                            Map templateRowObject = new HashMap();
                            templateRowObject.put(
                                SystemAttributes.OBJECT_CLASS,
                                forClass
                            );
                            Object[] templateRow = new Object[nCols+1];      
                            // First column contains full information about object
                            AttributeValue objRef = ObjectReferenceValue.createObjectReferenceValue(
                                templateRowObject,
                                new FieldDef(
                                    "identity",
                                    "org:openmdx:base:ExtentCapable:identity",
                                    Multiplicities.SINGLE_VALUE,
                                    false,
                                    true,
                                    application.getIconKey(forClass),
                                    null, null, null,
                                    application.getPortalExtension().getDataBinding(null)
                                ),
                                application
                            );
                            templateRow[0] = objRef;
                            for(int j = 0; j < nCols; j++) {
                                org.openmdx.ui1.jmi1.ValuedField fieldDef = (org.openmdx.ui1.jmi1.ValuedField)objectContainer.getMember().get(j);
                                // Special treatment of identity
                                if(SystemAttributes.OBJECT_IDENTITY.equals(fieldDef.getFeatureName())) {
                                    templateRow[j+1] = ObjectReferenceValue.createObjectReferenceValue(
                                        templateRowObject,
                                        new FieldDef(
                                            "identity",
                                            "org:openmdx:base:ExtentCapable:identity",
                                            Multiplicities.SINGLE_VALUE,
                                            false,
                                            true,
                                            null, null, null, null,
                                            application.getPortalExtension().getDataBinding(null)
                                        ),
                                        application
                                    );
                                }
                                else {
                                    templateRow[j+1] = application.createAttributeValue(
                                        fieldDef,
                                        templateRowObject
                                    );
                                }
                            }
                            templateRows.add(templateRow);
                        }
                    }
                }
            }
            catch(ServiceException e) {
            	SysLog.warning(e.getMessage(), e.getCause());
            }
        }    
        this.objectCreators = objectCreators == null ? 
            null : 
            (Action[])objectCreators.values().toArray(new Action[objectCreators.size()]);     
        this.templateRows = templateRows == null ? 
            null : 
            templateRows.toArray(new Object[templateRows.size()]);        
        
        // return no action in case of non changeable grids
        this.multiDeleteAction = (this.getObjectCreator() == null) || (this.getAddObjectAction() != null) ? 
            null : 
            new Action(
                Action.EVENT_MULTI_DELETE, 
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                                                  
                },
                application.getTexts().getDeleteTitle(), 
                WebKeys.ICON_DELETE,
                true
              );
        this.saveAction = new Action(
            Action.EVENT_SAVE_GRID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())
            },
            texts.getSaveTitle(),
            WebKeys.ICON_SAVE,
            true
        );
        this.setCurrentFilterAsDefaultAction = new Action(
            Action.EVENT_SET_CURRENT_FILTER_AS_DEFAULT,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(control.getPaneIndex())),                      
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())     
            },
            texts.getSetCurrentAsDefaultText(),
            WebKeys.ICON_FILTER_SET_AS_DEFAULT,
            true
        );

        // Show first page
        String showRowsOnInitPropertyName = control.getPropertyName(
            control.getQualifiedReferenceName(),
            PROPERTY_SHOW_ROWS_ON_INIT
        );
        if(application.getSettings().getProperty(showRowsOnInitPropertyName) != null) {
            this.setShowGridContentOnInit(
                Boolean.valueOf(application.getSettings().getProperty(showRowsOnInitPropertyName)).booleanValue()
            );
        }
        // Rows
        this.refresh(false);
    }

  //-------------------------------------------------------------------------
  public GridControl getGridControl(
  ) {
      return (GridControl)this.control;
  }

    //-------------------------------------------------------------------------
    public boolean getShowRows(
    ) {
        return this.showRows;
    }
  
    //-------------------------------------------------------------------------
    public void setShowRows(
        boolean newValue
    ) {
        this.showRows = newValue;
    }
  
    //-----------------------------------------------------------------------
    public Action getAddObjectAction(
    ) {
        return this.addObjectAction;
    }

    //-----------------------------------------------------------------------
    public Action getRemoveObjectAction(
    ) {
        return this.removeObjectAction;
    }

    //-----------------------------------------------------------------------
    public boolean isComposite(
    ) {
        return this.isComposite;
    }

    //-----------------------------------------------------------------------
    public boolean isChangeable(
    ) {
        return this.isChangeable;
    }    
    
    //-----------------------------------------------------------------------
    /**
     * @return Returns the objectCreators.
     */
    public Action[] getObjectCreators(
    ) {
        return objectCreators;
    }

    //-------------------------------------------------------------------------
    public Object[] getTemplateRow(
    ) {
        return this.templateRows;
    }

    //-------------------------------------------------------------------------
    public Action[] getObjectCreator(
    ) {
        return this.objectCreators;
    }
  
    //-------------------------------------------------------------------------
    public Action getMultiDeleteAction(
    ) {
        return this.multiDeleteAction;
    }
  
    //-------------------------------------------------------------------------
    public Action getSaveAction(
    ) {
        return this.saveAction;
    }

    //-------------------------------------------------------------------------
    public Action getPageNextAction(
        boolean isEnabled
    ) {
        return new Action(
            Action.EVENT_PAGE_NEXT,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getGridControl().getPaneIndex())),         
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())       
            },
            "",
            isEnabled ? WebKeys.ICON_NEXT : WebKeys.ICON_NEXT_DISABLED,
            isEnabled
        );
    }

    //-------------------------------------------------------------------------
    public Action getPagePreviousAction(
        boolean isEnabled
    ) {
        return new Action(
            Action.EVENT_PAGE_PREVIOUS,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getRefObject().refMofId()),                        
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getGridControl().getPaneIndex())),
                new Action.Parameter(Action.PARAMETER_REFERENCE, control.getId())       
            },
            "",
            isEnabled ? WebKeys.ICON_PREVIOUS : WebKeys.ICON_PREVIOUS_DISABLED,
            isEnabled
        );
    }
      
    //-------------------------------------------------------------------------
    public Action getPageNextFastAction(
        boolean isEnabled
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        int next10 = java.lang.Math.min(this.getLastPage(), this.getCurrentPage() + 10);
        return new Action(
            Action.EVENT_SET_PAGE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refMofId()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, Integer.toString(next10))
            },
            ">>",
            isEnabled ? WebKeys.ICON_NEXT_FAST : WebKeys.ICON_NEXT_FAST_DISABLED,
            isEnabled
        );
    }
    
    //-------------------------------------------------------------------------
    public Action getPagePreviousFastAction(
        boolean isEnabled
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        int back10 = java.lang.Math.max(0, this.getCurrentPage() - 10);
        return new Action(
            Action.EVENT_SET_PAGE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refMofId()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, Integer.toString(back10))
            },
            "<<",
            isEnabled ? WebKeys.ICON_PREVIOUS_FAST : WebKeys.ICON_PREVIOUS_FAST_DISABLED,
            isEnabled
        );        
    }
      
    //-------------------------------------------------------------------------
    public Action getSetCurrentFilterAsDefaultAction(
    ) {
        return this.setCurrentFilterAsDefaultAction;
    }
      
  //-------------------------------------------------------------------------
  public void refresh(
      boolean refreshData
  ) {
      ApplicationContext application = this.view.getApplicationContext();
      Model_1_0 model = application.getModel();
      GridControl gridControl = this.getGridControl();

      // Show row selectors
      this.showRowSelectors = false;
      if(lookupType != null) {
          this.showRowSelectors = false;
          try {
              this.showRowSelectors = GridControl.getShowRowSelectors(
                  lookupType,
                  model.getElement(gridControl.getObjectContainer().getReferencedTypeName()),
                  model
              );
          } 
          catch(Exception e) {}          
      }
      if(this.currentFilter == null) {
          this.selectFilter(
              Filters.DEFAULT_FILTER_NAME, 
              null 
          );
      }
      else {
    	  String filterName = this.currentFilter.getName();
          this.selectFilter(
              filterName,
              this.filterValues.get(filterName) == null ? 
            	  "" : 
            		  this.filterValues.get(filterName)
          );
      }
  }
  
  //-------------------------------------------------------------------------
  public void setCurrentFilterAsDefault(
  ) throws ServiceException {
      if(this.currentFilter != null) {
          Filter defaultFilter = new Filter(
              Filters.DEFAULT_FILTER_NAME,
              null,
              "0",
              WebKeys.ICON_FILTER_DEFAULT,
              null,
              this.currentFilter.getCondition(),
              this.currentFilter.getOrderSpecifier(),
              null, // Do not store PiggyBackConditions
        	  this.getGridControl().getContainerId(), 
        	  this.view.getApplicationContext().getCurrentUserRole(), 
        	  this.view.getObjectReference().refMofId()
          );
          String filterAsXml = JavaBeans.toXML(defaultFilter);
          Properties settings = this.view.getApplicationContext().getSettings();
          settings.setProperty(
              this.getGridControl().getPropertyName(
                  this.getGridControl().getContainerId(),
                  GridControl.PROPERTY_DEFAULT_FILTER
              ),
              Base64.encode(filterAsXml.getBytes())
          );
          this.setFilter(
              Filters.DEFAULT_FILTER_NAME,
              defaultFilter
          );
      }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Returns true if row selectors (check boxes) should be shown for each grid 
   * row. This allows the user to select a row of the grid. 
   * The method returns true, if
   * <ul>
   *   <li>lookupType != null and the grid contains objects which are instance of lookupType</li>
   * </ul>
   * false otherwise.
   */
  public boolean showRowSelectors(
  ) {
    return this.showRowSelectors;
  }
  
  //-------------------------------------------------------------------------
  public Object[] getRows(
	  PersistenceManager pm
  ) {
      List rows = new ArrayList();      
      if(this.showRows) {
          List filteredObjects =  this.getFilteredObjects(
        	  pm,
        	  this.currentFilter
          );
          int newPageSize = this.setPageRequestNewPageSize;
          int newPage = this.setPageRequestNewPage;
          
          ApplicationContext app = this.view.getApplicationContext();
          GridControl gridControl = this.getGridControl();
          
          int currentPageSize = this.getPageSize();
          newPageSize = (newPageSize <= 0) || (newPageSize > MAX_PAGE_SIZE) ? 
        	  currentPageSize : 
        		  newPageSize;
          if(newPageSize != currentPageSize) {
              this.numberOfPages = Integer.MAX_VALUE;       
              Properties settings = app.getSettings();
              settings.setProperty(
                  this.getGridControl().getPropertyName(
                      this.getGridControl().getQualifiedReferenceName(),
                      GridControl.PROPERTY_PAGE_SIZE
                  ),
                  "" + newPageSize
              );
          }
          
          // Try to keep current row. Otherwise set current row 
          // to first row of selected page
          int firstRow = newPage * currentPageSize;
          if(newPage == this.currentPage) {
              firstRow = this.currentRow >= 0
                  ? this.currentRow - currentPageSize
                  : firstRow;
          }
          else if(newPage == this.currentPage + 1) {
              firstRow = this.currentRow >= 0
                  ? this.currentRow
                  : firstRow;
          }
          else if(newPage == this.currentPage - 1) {
              firstRow = this.currentRow >= 0
                  ? this.currentRow - currentPageSize - newPageSize
                  : firstRow; 
          }
          firstRow = Math.max(0, firstRow);
          
          // Set starting position to firstRow. If firstRow exceeds number
          // of maximum rows then page up until an existing row is found
          Iterator i = null;
          if(filteredObjects != null) {
              newPage = firstRow / newPageSize;
              int lastEmptyPage = newPage + 1;
              while(true) {
                  boolean hasNext = false;
                  try {
                      i = filteredObjects.listIterator(firstRow);
                      hasNext = i.hasNext();
                  }
                  catch(Exception e) {
                      hasNext = false;
                  }
                  if(hasNext || (newPage == 0)) {
                      if(newPage == lastEmptyPage - 1) break;
                      newPage++;
                  }
                  else {
                      lastEmptyPage = newPage;
                      newPage -= 10;                  
                      if(newPage < 0) newPage = 0;
                  }
                  firstRow = newPage * currentPageSize;
              }
          }
          if(i == null) {
              i = new ArrayList().iterator();
          }
          this.currentPage = new Double(Math.ceil((double)firstRow / (double)newPageSize)).intValue();
          this.currentRow = firstRow;          
          this.currentPageSize = newPageSize;
    
          int nCols = Math.min(
              gridControl.getShowMaxMember(),
              Math.min(
                  gridControl.getObjectContainer().getMember().size(),
                  Grid.MAX_COLUMNS
              )
          ) + 1;
          try {
	          while(i.hasNext()) {
	              RefObject_1_0 rowObject = (RefObject_1_0)i.next();
	              Object[] row = new Object[nCols];      
	              // first column contains full information about object
	              AttributeValue objRef = ObjectReferenceValue.createObjectReferenceValue(
	                  rowObject,
	                  new FieldDef(
	                      "identity",
	                      "org:openmdx:base:ExtentCapable:identity",
	                      Multiplicities.SINGLE_VALUE,
	                      false,
	                      true,
	                      null, null, null, null,
	                      app.getPortalExtension().getDataBinding(null)
	                  ),
	                  app
	              );
	              row[0] = objRef;               
	              for(int j = 1; j < nCols; j++) {
	                  org.openmdx.ui1.jmi1.ValuedField fieldDef = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(j-1);
	                  // special treatment of identity
	                  if(SystemAttributes.OBJECT_IDENTITY.equals(fieldDef.getFeatureName())) {
	                      row[j] = ObjectReferenceValue.createObjectReferenceValue(                      
	                          rowObject,
	                          new FieldDef(
	                              "identity",
	                              "org:openmdx:base:ExtentCapable:identity",
	                              Multiplicities.SINGLE_VALUE,
	                              false,
	                              true,
	                              null, null, null, null,
	                              app.getPortalExtension().getDataBinding(null)
	                          ),
	                          app
	                      );
	                  }
	                  else {
	                      try {
	                          row[j] = app.createAttributeValue(
	                              fieldDef, 
	                              rowObject
	                          );
	                      }
	                      catch(Exception e) {
	                          row[j] = null;
	                      }
	                  }
	              }
	              rows.add(row);
	              this.currentRow++;
	              if(rows.size() % newPageSize == 0) break;
	          }
	          if(!i.hasNext()) {
	              this.numberOfPages = newPage+1;
	          }
          } catch(Exception e) {
        	  ServiceException e0 = new ServiceException(e);
        	  e0.log();
          }
      }
      return rows.toArray(new Object[rows.size()]);
  }

  //-------------------------------------------------------------------------
  public void setPage(
      int newPage,
      int newPageSize
  ) {
      this.setPageRequestNewPage = newPage;
      this.setPageRequestNewPageSize = newPageSize;
  }
  
  //-------------------------------------------------------------------------
  public int getCurrentPage(
  ) {
    return this.currentPage;
  }

  //-------------------------------------------------------------------------
  public int getLastPage(
  ) {
    return java.lang.Math.max(0, this.numberOfPages - 1);
  }
  
  //-------------------------------------------------------------------------
  public int getPageSize(
  ) {
      ApplicationContext application = this.view.getApplicationContext();
      GridControl gridControl = this.getGridControl();
      
      if(this.currentPageSize < 0) {
          String pageSizePropertyName = this.getGridControl().getPropertyName(
              this.getGridControl().getQualifiedReferenceName(),
              GridControl.PROPERTY_PAGE_SIZE
          );
          if(application.getSettings().getProperty(pageSizePropertyName) != null) {
             this.currentPageSize = 
                  Short.parseShort(application.getSettings().getProperty(pageSizePropertyName));
          }
          else {
              this.currentPageSize = 
                  application.getPortalExtension().getGridPageSize(
                      gridControl.getObjectContainer().getReferencedTypeName()
                  );
          }
      }
      return this.currentPageSize;
  }
  
    //-------------------------------------------------------------------------
    abstract protected List<RefObject_1_0> getFilteredObjects(
    	PersistenceManager pm,
        Filter filter
    );

    //-------------------------------------------------------------------------
    abstract protected Collection<RefObject_1_0> getAllObjects(
    	PersistenceManager pm
    );
    
    //-------------------------------------------------------------------------
    public void selectFilter(
    	String filterName,
    	String filterValues
    ) {
    	GridControl gridControl = this.getGridControl();  
    	try {
    		// reset column order indicators if new filter is set
    		this.columnSortOrders.clear();
    		this.columnSortOrders.putAll(
    			gridControl.getInitialColumnSortOrders()
    		);
    		this.filterValues.put(
    			filterName,
    			this.currentFilterValue = (filterValues == null ? "" : filterValues)
    		);
    		// can apply filter only to containers
    		Filter filter = this.getFilter(filterName);
    		if(filter == null) {
    			SysLog.info("filter " + filterName + " not found");
    		}
    		else {
    			this.currentFilter = filter;
    			int i = 0;
    			for(Condition condition : this.currentFilter.getCondition()) {
    				for(Object value : condition.getValue()){
    					// Replace ? by filter value entered by user
    					// Replace ${name} by attribute value
    					if(
    						"?".equals(value) ||
    						(value instanceof String && ((String)value).startsWith("${") && ((String)value).endsWith("}")) 
    					) {
    						this.currentFilter = new Filter(
    							this.currentFilter.getName(),
    							this.currentFilter.getLabel(),
    							this.currentFilter.getGroupName(),
    							this.currentFilter.getIconKey(),
    							this.currentFilter.getOrder(),
    							this.currentFilter.getCondition(),
    							this.currentFilter.getOrderSpecifier(),
    							this.currentFilter.getExtension(),
								this.getGridControl().getQualifiedReferenceName(), 
								this.view.getApplicationContext().getCurrentUserRole(), 
								this.view.getObjectReference().refMofId()
    						);
    						Condition newCondition = (Condition)condition.clone();
    						// ?
    						if("?".equals(value)) {
	    						List values = new ArrayList();
	    						StringTokenizer tokenizer = new StringTokenizer(filterValues, ",;");
	    						while(tokenizer.hasMoreTokens()) {
	    							values.add(tokenizer.nextToken());
	    						}
	    						if(values.size() == 0) {
	    							values.add("");
	    						}
	    						newCondition.setValue(
	    							values.toArray(new Object[values.size()])
	    						);
    						}
    						// ${name}
    						else {
    							String feature = (String)value;
    							feature = feature.substring(2);
    							feature = feature.substring(0, feature.length() - 1);
    							newCondition.setValue(
    								new Object[]{
    									this.view.getObjectReference().getObject().refGetValue(feature)
    								}
    							);
    						}
    						this.currentFilter.getCondition(
    						).set(
    							i,
    							newCondition
    						);
    					}
    				}
    				i++;
    			}
    		}
    		SysLog.detail("selected filter ", this.currentFilter);
    		this.numberOfPages = Integer.MAX_VALUE;
    		this.setPage(
    			0, 
    			-1 // do not change page size
    		);
    	}
    	catch(CloneNotSupportedException e) {
    		throw new RuntimeServiceException(e);
    	}
    }

  //-------------------------------------------------------------------------
  private static class ConditionParser {
   
    public ConditionParser(
      String token,
      String feature
    ) {
      if(token.startsWith(">=")) {
        this.condition = 
          new IsGreaterOrEqualCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            true,
            (Object[])null
          );
        offset = 2;                      
      }
      else if(token.startsWith("<=")) {
        this.condition =
          new IsGreaterCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            false,
            (Object[])null
          );
        offset = 2;
      }
      else if(token.startsWith("<>")) {
        this.condition =
          new IsInCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            false,
            (Object[])null
          );
        offset = 2;
      }
      else if(token.startsWith("<")) {
        this.condition =
          new IsGreaterOrEqualCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            false,
            (Object[])null
          );
        offset = 1;
      }
      else if(token.startsWith(">")) {
        this.condition =
          new IsGreaterCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            true,
            (Object[])null
          );
        offset = 1;
      }
      else if(token.startsWith("*")) {
        this.condition =
          new SoundsLikeCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            true,
            (Object[])null
          );
        offset = 1;
      }
      else if(token.startsWith("!*")) {
          this.condition =
            new SoundsLikeCondition(
            	Quantifier.THERE_EXISTS,
              feature,
              false,
              (Object[])null
            );
          offset = 2;
      }
      else if(token.startsWith("%")) {
        this.condition =
          new IsLikeCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            true,
            (Object[])null
          );
        offset = 1;
      }
      else if(token.startsWith("!%")) {
          this.condition =
            new IsLikeCondition(
            	Quantifier.THERE_EXISTS,
              feature,
              false,
              (Object[])null
            );
          offset = 2;
      }
      else if(token.startsWith("=")) {
        this.condition =
          new IsInCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            true,
            (Object[])null
          );
        offset = 1;
      }
      else if(token.startsWith("!=")) {
        this.condition =
          new IsInCondition(
        	  Quantifier.THERE_EXISTS,
            feature,
            false,
            (Object[])null
          );
        offset = 2;
      }
      else {
        condition = null;
        offset = 0;
      }
    }
    
    public int getOffset(
    ) {
      return this.offset;
    }
    
    public Condition getCondition(
    ) {
      return this.condition;
    }
    
    private final int offset;
    private final Condition condition;
  }

  //-------------------------------------------------------------------------
  public void setColumnFilter(
      String filterName,
      String filterValues,
      boolean add,
      int newPageSize
  ) {
      ApplicationContext application = this.view.getApplicationContext();
      GridControl gridControl = this.getGridControl();      
      this.filterValues.put(
    	  filterName, 
    	  this.currentFilterValue = (filterValues == null ? "" : filterValues)
      );    
      // clear column sort indicators if new filter is set
      if(!add) {
          this.columnSortOrders.clear();
          this.columnSortOrders.putAll(
              gridControl.getInitialColumnSortOrders()
          );   
      }    
    // find column with given column title
    for(
        int i = 0; 
        (i < gridControl.getObjectContainer().getMember().size()) && (i < MAX_COLUMNS); 
        i++
    ) {

        org.openmdx.ui1.jmi1.ValuedField column = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(i);
        if(filterName.equals(column.getFeatureName())) {

          Filter baseFilter = add ? this.currentFilter : this.getFilter("All");
          List<Condition> conditions = new ArrayList<Condition>(
        	  baseFilter.getCondition()  
          );
          Extension extension = baseFilter.getExtension();
          if(extension != null) {
        	  extension = extension.clone();
          }
          
          // Number
          if(column instanceof org.openmdx.ui1.jmi1.NumberField) {

            // Code
            if(
                (application.getCodes() != null) &&
                (application.getCodes().getShortText(column.getQualifiedFeatureName(), (short)0, true, true) != null)
            ) {
              SysLog.detail("Code filter values", filterValues);
              Map shortTexts = application.getCodes().getShortText(
                column.getQualifiedFeatureName(),
                application.getCurrentLocaleAsIndex(),
                false,
                true
              );
              StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
              while(andExpr.hasMoreTokens()) {
                Condition condition = 
                  new IsInCondition(
                	  Quantifier.THERE_EXISTS,
                    column.getFeatureName(),
                    true,
                    (Object[])null
                  );
                List values = new ArrayList();
                StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
                while(orExpr.hasMoreTokens()) {
                  String token = orExpr.nextToken().trim();
                  ConditionParser conditionParser = new ConditionParser(
                    token,
                    column.getFeatureName()
                  );
                  if(conditionParser.getCondition() != null) {
                    condition = conditionParser.getCondition();
                  }
                  try {
                    String trimmedToken = token.substring(
                      conditionParser.getOffset()
                    ).trim();
                    Short code = (Short)shortTexts.get(trimmedToken);
                    if(code == null) {
                      try {
                        code = new Short(trimmedToken);
                      } 
                      catch(Exception e) {}
                    }
                    if(code == null) {
                    	SysLog.detail("can not map token " + trimmedToken + " to code");
                    	values.add((short)-1);
                    }
                    else {
                      values.add(code);
                    }
                  }
                  catch(NumberFormatException e) {}
                }
                if(values.size() > 0) {
                    condition.setValue(values.toArray());
                    conditions.add(condition);
                }
              }
            }
            
            // Number
            else {
              SysLog.detail("Number filter values", filterValues);
              StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
              while(andExpr.hasMoreTokens()) {
                Condition condition = 
                  new IsInCondition(
                	  Quantifier.THERE_EXISTS,
                    column.getFeatureName(),
                    true,
                    (Object[])null
                  );
                List values = new ArrayList();
                StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
                while(orExpr.hasMoreTokens()) {
                  String token = orExpr.nextToken().trim();
                  ConditionParser conditionParser = new ConditionParser(
                    token,
                    column.getFeatureName()
                  );
                  if(conditionParser.getCondition() != null) {
                      condition = conditionParser.getCondition();
                  }
                  BigDecimal num = application.parseNumber(
                      token.substring(conditionParser.getOffset()).trim()
                  );
                  if(num != null) {        
                      // Only integers as filter values. BigDecimal
                      // should not be used because serializing of
                      // filters with XMLEncoder does not work
                      values.add(
                          new Long(num.longValue())
                      );
                  }
                }
                if(values.size() > 0) {
                    condition.setValue(
                      values.toArray(new Object[values.size()])
                    );
                    conditions.add(
                      condition
                    );
                }
              }
            }
          }
          
          // Date
          else if(column instanceof org.openmdx.ui1.jmi1.DateField) {
        	SysLog.detail("Date filter values", filterValues);
            SimpleDateFormat dateParser = (SimpleDateFormat)SimpleDateFormat.getDateInstance(
                java.text.DateFormat.SHORT,
                this.getCurrentLocale()
            );            
            StringTokenizer andExpr = new StringTokenizer(filterValues, "&");
            while(andExpr.hasMoreTokens()) {
              Condition condition = 
                new IsBetweenCondition(
                	Quantifier.THERE_EXISTS,
                  column.getFeatureName(),
                  true,
                  null, 
                  null
                );
              List values = new ArrayList();
              StringTokenizer orExpr = new StringTokenizer(andExpr.nextToken().trim(), ";");
              while(orExpr.hasMoreTokens()) {
                String token = orExpr.nextToken().trim();
                ConditionParser conditionParser = new ConditionParser(
                  token,
                  column.getFeatureName()
                );
                if(conditionParser.getCondition() != null) {
                  condition = conditionParser.getCondition();
                }
                try {
                  values.add(
                      dateParser.parse(token.substring(conditionParser.getOffset()).trim())
                  );
                }
                catch(ParseException e) {}
              }
              if(
                  (condition instanceof IsBetweenCondition) &&
                  (values.size() < 2)
              ) {
                  Date day;
                  if(values.isEmpty()) {
                	  day = new Date();
                  } else try {
                      day = (Date)values.get(0);
                  } catch(IllegalArgumentException e) {
                	  day = new Date();
                  }
                  Calendar nextDay = new GregorianCalendar();
                  nextDay.setTime(day);
                  nextDay.add(Calendar.DAY_OF_MONTH, 1);
                  values.clear();
                  values.add(day);
                  values.add(nextDay.getTime());
              }
              if(!values.isEmpty()) {
                  condition.setValue(
                    values.toArray(new Object[values.size()])
                  );
                  conditions.add(
                    condition
                  );
              }
            }
          }

          // Boolean
          else if(column instanceof org.openmdx.ui1.jmi1.CheckBox) {
        	SysLog.detail("Boolean filter values", filterValues);
            List values = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(filterValues, ";");
            while(tokenizer.hasMoreTokens()) {
              String token = tokenizer.nextToken();
              if(application.getTexts().getTrueText().equals(token)) {
                values.add(
                  Boolean.TRUE
                );
              }
              else {
                values.add(
                  Boolean.FALSE
                );
              }
            }
            if(values.size() > 0) {
                conditions.add(
                  new IsInCondition(
                	  Quantifier.THERE_EXISTS,
                    column.getFeatureName(),
                    true,
                    values.toArray(new Object[values.size()])
                  )
                );
            }
          }

          // String
          else {
        	  SysLog.detail("String filter values", filterValues);
              Pattern pattern = Pattern.compile("[\\s\\&]*(?:(?:\"([^\"]*)\")|([^\\s\"\\&]+))");
              Matcher matcher = pattern.matcher(filterValues);
              while(matcher.find()) {
                  for(int j = 1; j <= matcher.groupCount(); j++) {
                      String andExpr = matcher.group(j);
                      if(andExpr != null) {
                    	  // if getQuery() returns a query filter it must be merged with 
                    	  // the base query which might also contain a query filter
                    	  org.openmdx.base.query.Filter query = application.getPortalExtension().getQuery(
                              column,
                              andExpr,
                              extension == null ? 0 : extension.getStringParam().size(),
                              application
                          );
                    	  if(query != null) {
                			  conditions.addAll(query.getCondition());
                			  Extension queryExtension = query.getExtension();
                			  if(queryExtension != null) {
                    			  //
	                    		  // Merge base query with returned query
                    			  //
                				  if(extension == null) {
                					  extension = queryExtension.clone();
                				  } else {
                					  // Merged clause
                					  extension.setClause(
                						  extension.getClause() + " AND " + queryExtension.getClause()
                					  );
                					  // Merged string parameter
                					  extension.getStringParam().addAll(
                						  queryExtension.getStringParam()
                					  );
                				  }
                			  }
                    	  } else {
                              Condition condition = new IsLikeCondition(
                            	  Quantifier.THERE_EXISTS,
                                  column.getFeatureName(),
                                  true,
                                  (Object[])null
                              );
                              List values = new ArrayList();
                              StringTokenizer orExpr = new StringTokenizer(andExpr.trim(), ";");
                              while(orExpr.hasMoreTokens()) {
                                  String token = orExpr.nextToken().trim();
                                  ConditionParser conditionParser = new ConditionParser(
                                      token,
                                      column.getFeatureName()
                                  );
                                  if(conditionParser.getCondition() != null) {
                                      condition = conditionParser.getCondition();
                                  }
                                  String trimmedToken = token.substring(conditionParser.getOffset()).trim();
                                  if(condition instanceof IsLikeCondition) {
                                      trimmedToken = application.getWildcardFilterValue(trimmedToken);
                                  }
                                  values.add(trimmedToken);
                              }
                              if(!values.isEmpty()) {
                                  condition.setValue(values.toArray());
                                  conditions.add(condition);
                              }
                          }
                      }
                  }
              }
          }          
          this.currentFilter = new Filter(
              column.getFeatureName(),
              null,
              "",
              WebKeys.ICON_DEFAULT,
              null,
              conditions,
              null, // order
              extension,
              this.getGridControl().getContainerId(), 
              this.view.getApplicationContext().getCurrentUserRole(), 
              this.view.getObjectReference().refMofId()               
          );
          break;
        }
    }
    SysLog.detail("selected filter", this.currentFilter);
    this.numberOfPages = Integer.MAX_VALUE;
    this.setPage(
        0,
        newPageSize
    );
  }
  
  //-------------------------------------------------------------------------
  public void setOrder(
	  String feature,
	  short order
  ) {
	  GridControl gridControl = this.getGridControl();
	  this.columnSortOrders.put(
		  feature,
		  new Short(order)
	  );
	  // apply filter to container
	  if(this.currentFilter != null) {
		  List orderSpecifier = new ArrayList(this.currentFilter.getOrderSpecifier());
		  // Lookup column to be ordered
		  org.openmdx.ui1.jmi1.ValuedField column = null;        
		  for(
			  int i = 0; 
			  (i < gridControl.getObjectContainer().getMember().size()) && (i < MAX_COLUMNS); 
			  i++
		  ) {
			  column = (org.openmdx.ui1.jmi1.ValuedField)gridControl.getObjectContainer().getMember().get(i);
			  if(feature.equals(column.getFeatureName())) {
				  break;
			  }
		  }
		  if(!(column instanceof org.openmdx.ui1.jmi1.ObjectReferenceField)) {
			  String orderByFeature = feature;

			  // Set/add order specifier
			  boolean found = false;
			  for(
				  int i = 0; 
				  i < orderSpecifier.size(); 
				  i++
			  ) {
				  if(orderByFeature.equals(((OrderSpecifier)orderSpecifier.get(i)).getFeature())) {
					  orderSpecifier.set(
						  i,
						  new OrderSpecifier(
							  orderByFeature,
							  SortOrder.valueOf(order)
						  )
					  );
					  found = true;
					  break;
				  }
			  }
			  if(!found) {
				  orderSpecifier.add(
					  new OrderSpecifier(
						  orderByFeature,
						  SortOrder.valueOf(order)
					  )         
				  );
			  }
			  this.currentFilter = new Filter(
				  this.currentFilter.getName(),
				  this.currentFilter.getLabel(),
				  this.currentFilter.getGroupName(),
				  this.currentFilter.getIconKey(),
				  this.currentFilter.getOrder(),
				  this.currentFilter.getCondition(),
				  orderSpecifier,
				  this.currentFilter.getExtension(),
				  this.getGridControl().getQualifiedReferenceName(), 
				  this.view.getApplicationContext().getCurrentUserRole(), 
				  this.view.getObjectReference().refMofId()                 
			  );
		  }
	  }
	  SysLog.detail("order by filter", this.currentFilter);
	  this.numberOfPages = Integer.MAX_VALUE;
	  this.setPage(
		  0,
		  -1 // do not change page size
	  );
  }
  
    //-------------------------------------------------------------------------
    public short getOrder(
    	String feature
    ) {
	    Short order = (Short)this.columnSortOrders.get(feature);
	    return order == null
	      ? SortOrder.UNSORTED.code()
	      : order.shortValue();
    }

    //-------------------------------------------------------------------------
    public Filter getCurrentFilter(
    ) {
    	return this.currentFilter;
    }
  
    //-------------------------------------------------------------------------
    public String getFilterValue(
	    String filterName
    ) {
	    return this.filterValues.get(filterName) == null ?
	    	"" :
	    		this.filterValues.get(filterName);
    }
  
    //-------------------------------------------------------------------------
    public String getCurrentFilterValue(
    ) {
    	return this.currentFilterValue;
    }
  
    //-------------------------------------------------------------------------
    public Action getFirstPageAction(
    ) {
        GridControl gridControl = this.getGridControl();
        int paneIndex = gridControl.getPaneIndex();
        return new Action(
            Action.EVENT_SET_PAGE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refMofId()),
                new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(paneIndex)), 
                new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                new Action.Parameter(Action.PARAMETER_PAGE, "0")
            },
            "|<&nbsp;",
            WebKeys.ICON_FIRST,
            true
       );
    }
    
    //-------------------------------------------------------------------------
    public Action getSelectFilterAction(
        Filter filter
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        HtmlEncoder_1_0 encoder = application.getHtmlEncoder();
        return
            new Action(
                Action.EVENT_SELECT_FILTER,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, this.view.getRefObject().refMofId()),
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(gridControl.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),
                    new Action.Parameter(Action.PARAMETER_NAME, encoder.encode(filter.getName(), false))
                },
                filter.getLabel(application.getCurrentLocaleAsIndex()),
                filter.getIconKey(),
                true
            );
    }
  
    //-------------------------------------------------------------------------
    public Filter[] getFilters(
    ) {
        return this.filters;
    }
    
    //-------------------------------------------------------------------------
    public Filter getFilter(
        String filterName
    ) {
        Filter[] filters = this.getFilters();
        for(int i = 0; i < filters.length; i++) {
            if((filters[i] != null) && filterName.equals(filters[i].getName())) {
                return filters[i];
            }
        }
        return null;
    }
    
    //-------------------------------------------------------------------------
    public void setFilter(
        String filterName,
        Filter filter
    ) {
        Filter[] filters = this.getFilters();
        for(int i = 0; i < filters.length; i++) {
            if((filters[i] != null) && filterName.equals(filters[i].getName())) {
                filters[i] = filter;
                break;
            }
        }
    }
    
    //-------------------------------------------------------------------------
    public Action getColumnOrderSetAction(
        String forFeature
    ) {
      ApplicationContext application = this.view.getApplicationContext();  
      GridControl gridControl = this.getGridControl();
      short order = this.getOrder(forFeature);
      int paneIndex = gridControl.getPaneIndex();
      
      // toggle ANY -> ASCENDING -> DESCENDING -> ANY
      // show icon of current ordering
      try {
        switch(SortOrder.valueOf(order)) {
          case UNSORTED:
            return new Action(
              Action.EVENT_SET_ORDER_ASC,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getSortAscendingText(),
              application.getTexts().getSortAscendingText(),
              WebKeys.ICON_SORT_ANY,
              true
            );
          case ASCENDING:
            return new Action(
              Action.EVENT_SET_ORDER_DESC,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getSortDescendingText(),
              application.getTexts().getSortDescendingText(),
              WebKeys.ICON_SORT_UP,
              true
            );
          case DESCENDING:
            return new Action(
              Action.EVENT_SET_ORDER_ANY,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getDisableSortText(),
              application.getTexts().getDisableSortText(),
              WebKeys.ICON_SORT_DOWN,
              true
          );
          default: return null; // unreachable statement 
        } 
      } catch (IllegalArgumentException exception) {
          return new Action(
              Action.EVENT_NONE,
              new Action.Parameter[]{},
              application.getTexts().getSortAscendingText(),
              application.getTexts().getSortAscendingText(),
              WebKeys.ICON_SORT_ANY,
              true
          );          
       }
      
    }

    //-------------------------------------------------------------------------
    public Action getColumnOrderAddAction(
        String forFeature
    ) {
      ApplicationContext application = this.view.getApplicationContext();  
      GridControl gridControl = this.getGridControl();
      short order = this.getOrder(forFeature);
      
      // toggle ANY -> ASCENDING -> DESCENDING -> ANY
      // show icon of current ordering
      int paneIndex = gridControl.getPaneIndex();
      try {
        switch(SortOrder.valueOf(order)) {
          case UNSORTED:
            return new Action(
              Action.EVENT_ADD_ORDER_ASC,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getSortAscendingText(),
              WebKeys.ICON_SORT_ANY,
              true
            );
          case ASCENDING:
            return new Action(
              Action.EVENT_ADD_ORDER_DESC,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getSortDescendingText(),
              WebKeys.ICON_SORT_UP,
              true
            );
          case DESCENDING:
            return new Action(
              Action.EVENT_ADD_ORDER_ANY,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                  new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
                  new Action.Parameter(Action.PARAMETER_NAME, forFeature)
              },
              application.getTexts().getDisableSortText(),
              WebKeys.ICON_SORT_DOWN,
              true
          );
          default:  
            return null; // unreachable statement
        } 
      } catch (IllegalArgumentException exception) {
          return new Action(
              Action.EVENT_NONE,
              new Action.Parameter[]{},
              application.getTexts().getSortAscendingText(),
              WebKeys.ICON_SORT_ANY,
              true
          );          
      }
    }
    
    //-------------------------------------------------------------------------
    public void setShowGridContentOnInit(
        boolean newValue
    ) {
        // Update settings. The alignment setting will be reused for
        // all grids of the same type
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        Properties settings = application.getSettings();
        settings.setProperty(
            gridControl.getPropertyName(
                gridControl.getQualifiedReferenceName(),
                PROPERTY_SHOW_ROWS_ON_INIT
            ),
            "" + newValue
        );
    }

    //-------------------------------------------------------------------------
    public short getAlignment(
    ) {
        // Alignment
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        String gridAlignmentPropertyName = gridControl.getPropertyName(
            gridControl.getQualifiedReferenceName(),
            PROPERTY_PAGE_ALIGNMENT
        );
        if(application.getSettings().getProperty(gridAlignmentPropertyName) != null) {
            return Short.parseShort(application.getSettings().getProperty(gridAlignmentPropertyName));
        }
        else {
            return ALIGNMENT_NARROW;
        }
    }
    
    //-------------------------------------------------------------------------
    public void setAlignment(
        short alignment
    ) {
        // update settings. The alignment setting will be reused for
        // all grids of the same type
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        Properties settings = application.getSettings();
        settings.setProperty(
            gridControl.getPropertyName(
                gridControl.getQualifiedReferenceName(),
                PROPERTY_PAGE_ALIGNMENT
            ),
            "" + alignment
        );
    }

    //-------------------------------------------------------------------------
    public boolean showGridContentOnInit(
    ) {
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        return application.getPortalExtension().showGridContentOnInit(
            gridControl, 
            application
        );
      }
    
    //-------------------------------------------------------------------------
    public Action getAlignmentAction(
    ) {
      // toggle grid alignment: WIDE -> NARROW -> WIDE
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
      short alignment = this.getAlignment();
      return new Action(
          alignment == ALIGNMENT_NARROW
              ? Action.EVENT_SET_GRID_ALIGNMENT_WIDE
              : Action.EVENT_SET_GRID_ALIGNMENT_NARROW,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_PANE, "" + gridControl.getPaneIndex()),                      
              new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
          },
          alignment == ALIGNMENT_NARROW
              ? application.getTexts().getWideGridLayoutText()
              : application.getTexts().getNarrowGridLayoutText(),
          alignment == ALIGNMENT_NARROW
              ? WebKeys.ICON_PAGE_WIDE
              : WebKeys.ICON_PAGE_NARROW,
          true
        );
    }

    //-------------------------------------------------------------------------
    public Action getSetShowGridContentOnInitAction(
    ) {      
      // toggle show first page: NO -> YES -> NO
        ApplicationContext application = this.view.getApplicationContext();
        GridControl gridControl = this.getGridControl();
        boolean showRowsOnInit = this.showGridContentOnInit();
        return new Action(
          showRowsOnInit
              ? Action.EVENT_SET_HIDE_ROWS_ON_INIT
              : Action.EVENT_SET_SHOW_ROWS_ON_INIT,
          new Action.Parameter[]{
              new Action.Parameter(Action.PARAMETER_PANE, "" + gridControl.getPaneIndex()),                      
              new Action.Parameter(Action.PARAMETER_REFERENCE, gridControl.getId()),       
          },
          showRowsOnInit
              ? application.getTexts().getShowRowsOnInitTitle()
              : application.getTexts().getHideRowsOnInitTitle(),
          showRowsOnInit
              ? WebKeys.ICON_SHOW_ROWS_ON_INIT
              : WebKeys.ICON_HIDE_ROWS_ON_INIT,
          true
        );
    }

    // -------------------------------------------------------------------------
    public static int getPageSizeParameter(
        Map parameterMap
    ) {
        Object[] pageSizes = (Object[]) parameterMap.get(WebKeys.REQUEST_PARAMETER_PAGE_SIZE);
        String pageSize = pageSizes == null ? null : (pageSizes.length > 0 ? (String) pageSizes[0] : null);
        return pageSize == null ? -1 : Integer.parseInt(pageSize);
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    public static final int MAX_PAGE_SIZE = 500;
  
    public static final int COLUMN_TYPE_NONE = 0;
    public static final int COLUMN_TYPE_STRING = 1;
    public static final int COLUMN_TYPE_DATE = 2;
    public static final int COLUMN_TYPE_NUMBER = 3;
    public static final int COLUMN_TYPE_BOOLEAN = 4;

    public static final short ALIGNMENT_NARROW = 0;
    public static final short ALIGNMENT_WIDE = 1;
  
    public static final int MAX_COLUMNS = 20;
  
    public static final String PROPERTY_SHOW_ROWS_ON_INIT = "Page.DefaultFilterOnInit";
    public static final String PROPERTY_PAGE_ALIGNMENT = "Page.Alignment";
    
    private int currentRow = -1;
    private int currentPage = 0;
    private int currentPageSize = -1;
    private int numberOfPages = Integer.MAX_VALUE;
    private Filter currentFilter = null;
    private Map<String,String> filterValues = new HashMap<String,String>();
    private String currentFilterValue;
    private final Map columnSortOrders;
    private boolean showRowSelectors = false;
    private final String lookupType;
    private final Filter[] filters;
    private Object[] templateRows;
    private final Action[] objectCreators;
    private final Action addObjectAction;
    private final Action removeObjectAction;
    private final Action multiDeleteAction;
    private final Action saveAction;
    private final Action setCurrentFilterAsDefaultAction;
    protected final DataBinding dataBinding;
    private final boolean isComposite;
    private final boolean isChangeable;
  
    // holders for setPage() request
    private int setPageRequestNewPage = 0;
    private int setPageRequestNewPageSize = -1;
    private boolean showRows = true;
  
}

//--- End of File -----------------------------------------------------------
