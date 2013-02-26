/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package edu.toronto.cs.phenotips.tools;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 * @since 1.0M2
 */
public class FormData
{
    private DisplayMode mode;

    private String positiveFieldName;

    private String negativeFieldName;

    private String positivePropertyName;

    private String negativePropertyName;

    private Collection<String> selectedValues;

    private Collection<String> selectedNegativeValues;

    /**
     * @return the mode
     */
    public DisplayMode getMode()
    {
        return this.mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(DisplayMode mode)
    {
        this.mode = mode;
    }

    /**
     * @return the positiveFieldName
     */
    public String getPositiveFieldName()
    {
        return this.positiveFieldName;
    }

    /**
     * @param positiveFieldName the positiveFieldName to set
     */
    public void setPositiveFieldName(String positiveFieldName)
    {
        this.positiveFieldName = positiveFieldName;
    }

    /**
     * @return the negativeFieldName
     */
    public String getNegativeFieldName()
    {
        return this.negativeFieldName;
    }

    /**
     * @param negativeFieldName the negativeFieldName to set
     */
    public void setNegativeFieldName(String negativeFieldName)
    {
        this.negativeFieldName = negativeFieldName;
    }

    /**
     * @return the positivePropertyName
     */
    public String getPositivePropertyName()
    {
        return this.positivePropertyName;
    }

    /**
     * @param positivePropertyName the positivePropertyName to set
     */
    public void setPositivePropertyName(String positivePropertyName)
    {
        this.positivePropertyName = positivePropertyName;
    }

    /**
     * @return the negativePropertyName
     */
    public String getNegativePropertyName()
    {
        return this.negativePropertyName;
    }

    /**
     * @param negativePropertyName the negativePropertyName to set
     */
    public void setNegativePropertyName(String negativePropertyName)
    {
        this.negativePropertyName = negativePropertyName;
    }

    /**
     * @return the selectedValues
     */
    public Collection<String> getSelectedValues()
    {
        return this.selectedValues;
    }

    /**
     * @param selectedValues the selectedValues to set
     */
    public void setSelectedValues(Collection<String> selectedValues)
    {
        this.selectedValues = selectedValues;
    }

    /**
     * @return the selectedNegativeValues
     */
    public Collection<String> getSelectedNegativeValues()
    {
        return this.selectedNegativeValues;
    }

    /**
     * @param selectedNegativeValues the selectedNegativeValues to set
     */
    public void setSelectedNegativeValues(Collection<String> selectedNegativeValues)
    {
        this.selectedNegativeValues = selectedNegativeValues;
    }
}
