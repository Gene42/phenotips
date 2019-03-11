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
package org.phenotips.data.internal;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.PatientWritePolicy;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.reference.DocumentReference;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link Patient} implementation.
 */
@NotThreadSafe
public class PhenoTipsPatientTest
{
    private static final String ID = "id";

    private static final String LABEL = "label";

    private static final String FEATURE_1 = "feature1";

    private static final String FEATURE_2 = "feature2";

    private static final String FEATURE_3 = "feature3";

    private static final String DISORDER_1 = "disorder1";

    private static final String DISORDER_2 = "disorder2";

    private static final String DISORDER_3 = "disorder3";

    private static final String IDENTIFIERS = "identifiers";

    private static final String EXTERNAL_ID = "external_id";

    private static final String PATIENT_EID = "patientEID";

    private static final String FEATURES = "features";

    private static final String DISORDERS = "disorders";

    @InjectMocks
    private PhenoTipsPatient patient;

    @Mock
    private DocumentReference docRef;

    @Mock
    private DocumentReference creatorReference;

    @Mock
    private BaseObject patientBaseObj;

    @Mock
    private PatientDataController controllerA;

    @Mock
    private PatientDataController controllerB;

    @Mock
    private PatientDataController controllerC;

    @Mock
    private PatientData<String> identifiersData;

    @Mock
    private PatientData<Feature> featuresData;

    @Mock
    private PatientData<Disorder> disordersData;

    private XWikiDocument doc;

    private Feature feature1;

    private Feature feature2;

    private Feature feature3;

    private Disorder disorder1;

    private Disorder disorder2;

    private Disorder disorder3;

    private Map<String, PatientData<?>> extraData;

    private Map<String, PatientDataController<?>> serializers;

    @Before
    public void setUp() throws Exception
    {
        this.patient = mock(PhenoTipsPatient.class, Mockito.CALLS_REAL_METHODS);
        MockitoAnnotations.initMocks(this);

        this.extraData = new TreeMap<>();
        ReflectionUtils.setFieldValue(this.patient, "extraData", this.extraData);

        this.serializers = new TreeMap<>();
        ReflectionUtils.setFieldValue(this.patient, "serializers", this.serializers);

        this.doc = spy(new XWikiDocument(this.docRef));
        final Locale locale = new Locale("eng");
        when(this.docRef.getLocale()).thenReturn(locale);
        ReflectionUtils.setFieldValue(this.patient, "document", this.doc);

        this.feature1 = new PhenoTipsFeature(new JSONObject().put(ID, FEATURE_1).put(LABEL, FEATURE_1));
        this.feature2 = new PhenoTipsFeature(new JSONObject().put(ID, FEATURE_2).put(LABEL, FEATURE_2));
        this.feature3 = new PhenoTipsFeature(new JSONObject().put(ID, FEATURE_3).put(LABEL, FEATURE_3));

        this.disorder1 = new PhenoTipsDisorder(new JSONObject().put(ID, DISORDER_1).put(LABEL, DISORDER_1));
        this.disorder2 = new PhenoTipsDisorder(new JSONObject().put(ID, DISORDER_2).put(LABEL, DISORDER_2));
        this.disorder3 = new PhenoTipsDisorder(new JSONObject().put(ID, DISORDER_3).put(LABEL, DISORDER_3));
    }

    @Test
    public void getType()
    {
        Assert.assertEquals(Patient.CLASS_REFERENCE, this.patient.getType());
    }

    @Test
    public void getExternalIdInternalException()
    {
        when(this.patient.<String>getData(IDENTIFIERS)).thenReturn(null);
        Assert.assertNull(this.patient.getExternalId());
    }

    @Test
    public void getExternalIdNullEid()
    {
        when(this.patient.<String>getData(IDENTIFIERS)).thenReturn(this.identifiersData);
        when(this.identifiersData.get(EXTERNAL_ID)).thenReturn(null);
        Assert.assertNull(this.patient.getExternalId());
    }

    @Test
    public void getExternalIdEmptyEid()
    {
        when(this.patient.<String>getData(IDENTIFIERS)).thenReturn(this.identifiersData);
        when(this.identifiersData.get(EXTERNAL_ID)).thenReturn(StringUtils.EMPTY);
        Assert.assertEquals(StringUtils.EMPTY, this.patient.getExternalId());
    }

    @Test
    public void getExternalIdBlankEid()
    {
        when(this.patient.<String>getData(IDENTIFIERS)).thenReturn(this.identifiersData);
        when(this.identifiersData.get(EXTERNAL_ID)).thenReturn(StringUtils.SPACE);
        Assert.assertEquals(StringUtils.SPACE, this.patient.getExternalId());
    }

    @Test
    public void getExternalIdHasEid()
    {
        when(this.patient.<String>getData(IDENTIFIERS)).thenReturn(this.identifiersData);
        when(this.identifiersData.get(EXTERNAL_ID)).thenReturn(PATIENT_EID);
        Assert.assertEquals(PATIENT_EID, this.patient.getExternalId());
    }

    @Test
    public void getReporterNonNull()
    {
        doReturn(this.creatorReference).when(this.doc).getCreatorReference();
        Assert.assertEquals(this.creatorReference, this.patient.getReporter());
    }

    @Test
    public void getReporterNull()
    {
        doReturn(null).when(this.doc).getCreatorReference();
        Assert.assertNull(null);
    }

    @Test
    public void getFeaturesNullFeatures()
    {
        when(this.patient.<Feature>getData(FEATURES)).thenReturn(null);
        Assert.assertTrue(this.patient.getFeatures().isEmpty());
    }

    @Test
    public void getFeaturesEmptyFeatures()
    {
        when(this.patient.<Feature>getData(FEATURES)).thenReturn(this.featuresData);
        when(this.featuresData.iterator()).thenReturn(IteratorUtils.emptyIterator());
        Assert.assertTrue(this.patient.getFeatures().isEmpty());
    }

    @Test
    public void getFeaturesNonEmptyFeatures()
    {
        when(this.patient.<Feature>getData(FEATURES)).thenReturn(this.featuresData);
        final Iterator<Feature> featureIterator = Arrays.asList(this.feature1, this.feature2, this.feature3).iterator();
        when(this.featuresData.iterator()).thenReturn(featureIterator);
        final Set<Feature> features = this.patient.getFeatures();
        Assert.assertEquals(3, features.size());
        Assert.assertTrue(features.contains(this.feature1));
        Assert.assertTrue(features.contains(this.feature2));
        Assert.assertTrue(features.contains(this.feature3));
    }

    @Test
    public void getDisordersEmptyDisorders()
    {
        when(this.patient.<Disorder>getData(DISORDERS)).thenReturn(this.disordersData);
        when(this.disordersData.iterator()).thenReturn(IteratorUtils.emptyIterator());
        Assert.assertTrue(this.patient.getDisorders().isEmpty());
    }

    @Test
    public void getDisordersNonEmptyDisorders()
    {
        when(this.patient.<Disorder>getData(DISORDERS)).thenReturn(this.disordersData);
        final Iterator<Disorder> iterator = Arrays.asList(this.disorder1, this.disorder2, this.disorder3).iterator();
        when(this.disordersData.iterator()).thenReturn(iterator);
        final Set<Disorder> disorders = this.patient.getDisorders();
        Assert.assertEquals(3, disorders.size());
        Assert.assertTrue(disorders.contains(this.disorder1));
        Assert.assertTrue(disorders.contains(this.disorder2));
        Assert.assertTrue(disorders.contains(this.disorder3));
    }

    @Test
    public void getDataNotCachedRetrievesNullNoSerializer()
    {
        Assert.assertNull(this.patient.<String>getData(IDENTIFIERS));
    }

    @Test
    public void getDataNotCachedRetrievesNullNoData()
    {
        this.serializers.put(IDENTIFIERS, this.controllerA);
        when(this.controllerA.load(this.patient)).thenReturn(null);
        Assert.assertNull(this.patient.<String>getData(IDENTIFIERS));
    }

    @Test
    public void getDataNotCachedRetrievesNotNull()
    {
        this.serializers.put(IDENTIFIERS, this.controllerA);
        when(this.controllerA.load(this.patient)).thenReturn(this.identifiersData);
        when(this.identifiersData.getName()).thenReturn(IDENTIFIERS);
        Assert.assertEquals(this.identifiersData, this.patient.<String>getData(IDENTIFIERS));
    }

    @Test
    public void getDataCached()
    {
        this.extraData.put(IDENTIFIERS, this.identifiersData);
        Assert.assertEquals(this.identifiersData, this.patient.<String>getData(IDENTIFIERS));
    }

    @Test
    public void toJSONForwardsCalls()
    {
        doReturn(new JSONObject()).when(this.patient).toJSON(anyCollectionOf(String.class));
        this.patient.toJSON();
        verify(this.patient, times(1)).toJSON(anyCollectionOf(String.class));
    }

    @Test
    public void toJSONPickFieldsNone()
    {
    }

    @Test
    public void toJSONPickFieldsSome()
    {
    }

    @Test
    public void toJSONPickControllersNoneWithInclude()
    {
    }

    @Test
    public void toJSONPickControllersSomeWithInclude()
    {
    }

    @Test
    public void toJSONPickControllersNoneWithExclude()
    {
    }

    @Test
    public void toJSONPickControllersSomeWithExclude()
    {
    }

    @Test
    public void updateFromJSONForwardCalls()
    {
        final JSONObject json = new JSONObject();
        doNothing().when(this.patient).updateFromJSON(json, PatientWritePolicy.UPDATE);
        this.patient.updateFromJSON(json);
        verify(this.patient, times(1)).updateFromJSON(json, PatientWritePolicy.UPDATE);
    }

    @Test
    public void updateFromJSONEmptyWithSave()
    {
        final JSONObject json = new JSONObject();

    }

    @Test
    public void updateFromJSONEmptyNoSave()
    {
        final JSONObject json = new JSONObject();
    }

    @Test
    public void updateFromJSONNotEmptyWithSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyNoSave()
    {
    }

    @Test
    public void updateFromJSONEmptyPickPolicy()
    {
    }

    @Test
    public void updateFromJSONNotEmptyPickPolicy()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONUpdatePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONMergePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONReplacePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONUpdatePolicyWithSave()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONMergePolicyWithSave()
    {
    }

    @Test
    public void updateFromJSONEmptyJSONReplacePolicyWithSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONUpdatePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONMergePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONReplacePolicyNoSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONUpdatePolicyWithSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONMergePolicyWithSave()
    {
    }

    @Test
    public void updateFromJSONNotEmptyJSONReplacePolicyWithSave()
    {
    }

    @Test
    public void toStringMakesPrettyJSON()
    {
    }
}