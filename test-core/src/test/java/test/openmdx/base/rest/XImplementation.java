/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: XImplementation 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package test.openmdx.base.rest;

import java.util.Collections;
import java.util.List;

/**
 * XImplementation
 */
@SuppressWarnings("unchecked")
public class XImplementation
    implements XInterface
{

    /**
     * Constructor 
     *
     * @param date
     */
    protected XImplementation(long date) {
        this.date = #if CLASSIC_CHRONO_TYPES new java.util.Date #else java.time.Instant.ofEpochMilli#endif(date);
    }

    /**
     * 
     */
    private final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif date;

    #if CLASSIC_CHRONO_TYPES
    /* (non-Javadoc)
     */
    @Override
    public <T extends java.util.Date> java.util.List<T> getList() {
        return Collections.singletonList(this.date);
    }

    @Override
    public <T extends java.util.Date> java.util.List<T> list() {
        return (List<T>) getList();
    }
    #else
    @Override
    public <T extends java.time.Instant> java.util.List<T> getList() {
        return (List<T>) Collections.singletonList(this.date);
    }

    @Override
    public <T extends java.time.Instant> java.util.List<T> list() {
        return getList();
    }

    @Override
    public java.util.List<java.sql.Date> listSqlDates() {
        // Implementation for SQL Date list
        return Collections.emptyList();  // or appropriate implementation
    }
    #endif

    public void operation() throws Exception {
    }

}
