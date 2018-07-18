/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.data;

import org.phenotips.Constants;
import org.phenotips.entities.PrimaryEntity;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.stability.Unstable;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.json.JSONObject;

/**
 * Information about a patient.
 *
 * @version $Id$
 * @since 1.0M8
 * @since 1.3M2 this class was retrofitted to use the Entities API
 */
@Unstable
public interface Patient extends PrimaryEntity
{
    /** The XClass used for storing patient data. */
    EntityReference CLASS_REFERENCE = new EntityReference("PatientClass", EntityType.DOCUMENT,
        Constants.CODE_SPACE_REFERENCE);

    /** The default space where patient data is stored. */
    EntityReference DEFAULT_DATA_SPACE = new EntityReference("data", EntityType.SPACE);

    /**
     * Returns the external identifier of the patient record.
     *
     * @return string ID
     */
    String getExternalId();

    /**
     * Returns a reference to the profile of the user that created the patient record.
     *
     * @return a valid document reference
     * @todo Replace with a UserReference once they're available
     */
    DocumentReference getReporter();

    /**
     * Returns the list of recorded features, both positive and negative observations.
     *
     * @return an unmodifiable set of {@link Feature features}, or an empty set if no features are recorded yet
     */
    Set<? extends Feature> getFeatures();

    /**
     * Returns the list of recorded disorders.
     *
     * @return an unmodifiable set of {@link Disorder disorders}, or an empty set if no disorders have been identified
     *         yet
     */
    Set<? extends Disorder> getDisorders();

    /**
     * Returns other custom data enabled in this PhenoTips instance. The returned data is a list, potentially empty, or
     * with a single item, or with many different entries, or it may be {@code null} if the type of data requested isn't
     * available/enabled on the system. The format of each entry is not fixed and depends on the type of data requested.
     *
     * @param name the name of the custom data to access; this is a label for a category
     * @param <T> the type of data expected
     * @return the requested data, may be {@code null}
     * @since 1.0M10
     */
    <T> PatientData<T> getData(String name);

    /**
     * Retrieve all the patient data in a JSON format. For example:
     *
     * <pre>
     * {
     *   "id": "xwiki:data.P0000001",
     *   "reporter": "xwiki.XWiki.PatchAdams",
     *   "features": [
     *     // See the documentation for {@link Feature#toJSON()}
     *   ],
     *   "disorders": [
     *     // See the documentation for {@link Disorder#toJSON()}
     *   ]
     * }
     * </pre>
     *
     * @return the patient data, using the org.json classes
     */
    @Override
    JSONObject toJSON();

    /**
     * Retrieve patient data in a JSON format, restricted to the fields listed in onlyFieldNames.
     *
     * @param selectedFields list of fields which should be included in the resulting JSON. All available fields will be
     *            included if null (in which case this method is equivalent to {@link #toJSON()})
     * @return selected sub-set of patient data, using the org.json classes
     */
    JSONObject toJSON(Collection<String> selectedFields);

    /**
     * Retrieve patient data in JSON format. The provided set of {@code controllers} will be excluded from the result
     * iff {@code excludeControllers} is false. If {@code excludeControllers} is set to true, then the resulting json
     * will be restricted to the listed {@code controllers}.
     *
     * @param controllers a set of controllers {@link PatientDataController#getName()} that will either be included in
     *     or excluded from the resulting {@link JSONObject}. All available controllers will be included if {@code null}
     * @param excludeControllers iff true {@code controllers} will be treated as an exclusion set; will be treated as
     *     the set of selected {@code controllers} otherwise
     * @return a {@link JSONObject} containing the selected sub-set of patient data
     */
    default JSONObject toJSON(@Nullable Set<String> controllers, boolean excludeControllers)
    {
        return null;
    }

    /**
     * Update patient data using the provided json object (in the format generated by {@link #toJSON()}).
     * <p>
     * In the current implementation all existing fields which are present in JSON will be overwritten with data from
     * JSON. All fields not present in JSON will be left as is.
     *
     * @param json JSON object containing patient data
     */
    @Override
    void updateFromJSON(JSONObject json);
}
