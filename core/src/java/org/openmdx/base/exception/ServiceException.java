/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ServiceException.java,v 1.14 2008/10/06 17:34:52 hburger Exp $
 * Description: ServiceException class
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/06 17:34:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.base.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.log.SysLog;

public final class ServiceException
    extends Exception
    implements BasicException.Wrapper
{

    /**
     * Implements <code>Serializable</code>
     */
//    private static final long serialVersionUID = 4120847750670858549L;

    /**
     * Constructor  
     *
     * @param exception the Exception to be wrapped 
     */
    public ServiceException (
        BasicException exception
    ){
        super.initCause(
            exception == null ? BasicException.toStackedException(this) : exception
        );
    }

    /**
     * Constructor  
     *
     * @param exception the Exception to be wrapped 
     */
    public ServiceException (
        Exception exception
    ){
        super.initCause(
            BasicException.toStackedException(exception, this)
        );
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   cause
     *          The exception cause.
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   description
     *          A readable description usually not including the parameters.
     */
    public ServiceException(
        Exception cause,
        String exceptionDomain,
        int exceptionCode,
        BasicException.Parameter[] parameters,
        String description
    ){
        this(cause, exceptionDomain, exceptionCode, description, parameters);
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   cause
     *          The exception cause.
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   description
     *          A readable description usually not including the parameters.
     * @param   parameters
     *          The exception specific parameters.
     */
    public ServiceException(
        Exception cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        super.initCause(
            new BasicException(
                cause,
                exceptionDomain,
                exceptionCode,
                parameters,
                description,
                this
            )
        );
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   message
     *          A readable description not including the parameters.
     */
    public ServiceException(
        String exceptionDomain,
        int exceptionCode,
        BasicException.Parameter[] parameters,
        String description
    ) {
        this(exceptionDomain, exceptionCode, description, parameters);
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   message
     *          A readable description not including the parameters.
     */
    public ServiceException(
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ) {
        this(
            null, // none
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }


    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -851622086260585234L;
    
    /**
     * Log the exception.
     *
     * @return the ServiceException
     */
    public ServiceException log(
    ) {
        SysLog.warning(this);
        return this;
    }


    /**
     * 
     */
    public ServiceException appendCause(
        Throwable cause
    ){
        getCause().appendCause(cause);
        return this;
    }


    //------------------------------------------------------------------------
    // Implements StackedException.Wrapper
    //------------------------------------------------------------------------

    /**
     * Return a StackedException, this exception object's cause.
     * 
     * @return the BasicException wrapped by this object.
     * 
     * @deprecated use getCause()
     */
    public BasicException getExceptionStack (
    ) {
        return getCause();
    }

    /**
     * Retrieves the exception domain of this <code>ServiceException</code>.
     *
     * @return the exception domain
     */
    public String getExceptionDomain()
    {
        BasicException exceptionStack = getCause();
        return exceptionStack == null ? 
            BasicException.Code.DEFAULT_DOMAIN : 
                exceptionStack.getExceptionDomain();
    }

    /**
     * Retrieves the exception code of this <code>ServiceException</code>.
     *
     * @return the exception code
     */
    public int getExceptionCode()
    {
        BasicException exceptionStack = getCause();
        return exceptionStack == null ? 
            BasicException.Code.GENERIC : 
                exceptionStack.getExceptionCode();
    }

    /**
     * Returns the cause belonging to a specific exception domain.
     * 
     * @param 	exceptionDomain
     * 			the desired exception domain,
     *          or <code>null</code> to retrieve the initial cause.
     *
     * @return  Either the cause belonging to a specific exception domain
     *          or the initial cause if <code>exceptionDomain</code> is
     * 			<code>null</code>.  
     */
    public BasicException getCause(
        String exceptionDomain
    ){
        BasicException exceptionStack = getCause();
        return exceptionStack == null ? 
            null : 
                exceptionStack.getCause(exceptionDomain);
    }


    //------------------------------------------------------------------------
    // Extends Throwable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public final BasicException getCause() {
        return (BasicException) super.getCause();
    }
    
    /**
     * Returns the detail message string of this RuntimeServiceException.  
     */
    public String getMessage(
    ){
        BasicException exceptionStack = getCause();
        return exceptionStack == null ?
            super.getMessage() : 
                exceptionStack.getMessage() + ": " +
                exceptionStack.getDescription();
    }

    /**
     * A String consisting of the class of this exception, the exception 
     * domain, the exception code, the exception description and the exception
     * stack.
     * 
     * @return a multiline representation of this exception.
     */     
    public String toString(){
        BasicException exceptionStack = getCause();
        return 
        exceptionStack == null ?
            super.toString() : 
                super.toString() + '\n' + exceptionStack;
    }

    
    //----------------------------------------------------------------------------
    // Deprecated
    //----------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void printStackTrace(PrintStream s) {
        BasicException exceptionStack = getCause();
        if(exceptionStack == null){
            super.printStackTrace(s);
        } else {
            exceptionStack.printStack(this, s, true);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void printStackTrace(PrintWriter s) {
        BasicException exceptionStack = getCause();
        if(exceptionStack == null){
            super.printStackTrace(s);
        } else {
            exceptionStack.printStack(this, s, true);
        }
    }

}

