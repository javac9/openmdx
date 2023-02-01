/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: MapperFactory_1
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.mof.mapping.spi;

import java.util.Arrays;
import java.util.List;
import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.cci.MappingTypes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.loading.Classes;

/**
 * MapperFactory_1
 */
public class MapperFactory_1 {

	private static final List<String> JAVA_FORMATS = Arrays.asList(
            MappingTypes.CCI2,
            MappingTypes.JMI1,
            MappingTypes.JPA3
	);
	
    /**
     * Create a mapper instance
     * 
     * @param format the format, either predefined or as class name
     * 
     * @return the mapper instance specified by <code>format</code>
     * 
     * @throws ServiceException
     */
    public static Mapper_1_0 create(
        String format
    ) throws ServiceException {
    	final MappingFormatParser parser = new MappingFormatParser(format);
        if(JAVA_FORMATS.contains(parser.getId())) {
            return new org.openmdx.application.mof.mapping.java.Mapper_1(
                format,
                parser.getId(),
                "java"            
            );
        } else if (MappingTypes.XMI1.equals(format)) {
            return new org.openmdx.application.mof.mapping.xmi.XMIMapper_1();
        } else if (MappingTypes.MOF1.equals(format)) {
            return new org.openmdx.application.mof.mapping.java.mof.ModelNameConstantsMapper();
        } else if (MappingTypes.PIMDOC.equals(format)) {
          return parser.getArguments().length == 0 ?
    		  new org.openmdx.application.mof.mapping.pimdoc.PIMDocMapper() :
    		  new org.openmdx.application.mof.mapping.pimdoc.PIMDocMapper(parser.getArguments()[0]);
        } else {
            try {
                return Classes.<Mapper_1_0>newApplicationInstance(
                	Mapper_1_0.class, 
                	parser.getId(), 
                	(Object[])parser.getArguments()
                );
            } catch (Exception exception) {
                throw new ServiceException(exception);
            }
        }
    }

    
}
