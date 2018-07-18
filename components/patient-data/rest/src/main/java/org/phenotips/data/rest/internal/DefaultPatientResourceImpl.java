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
package org.phenotips.data.rest.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.rest.PatientResource;
import org.phenotips.rest.Autolinker;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rest.XWikiResource;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Default implementation for {@link PatientResource} using XWiki's support for REST resources.
 *
 * @version $Id$
 * @since 1.2M5
 */
@Component
@Named("org.phenotips.data.rest.internal.DefaultPatientResourceImpl")
@Singleton
public class DefaultPatientResourceImpl extends XWikiResource implements PatientResource
{
    /** Name of the URL parameter used for excluding a given controller when retrieving a patient. */
    public static final String EXCLUDE_CONTROLLER_URL_PARAM = "excludeController";

    /** Name of the URL parameter used for including a given controller when retrieving a patient. */
    public static final String INCLUDE_CONTROLLER_URL_PARAM = "includeController";

    @Inject
    private Logger logger;

    @Inject
    private PatientRepository repository;

    @Inject
    private AuthorizationManager access;

    @Inject
    private UserManager users;

    @Inject
    private Provider<Autolinker> autolinker;

    /**
     * Needed for getting access to the servlet request, which is used to determine which controllers are selected for
     * JSON export.
     */
    @Inject
    private Container container;

    @Override
    public Response getPatient(String id)
    {
        this.logger.debug("Retrieving patient record [{}] via REST", id);
        Patient patient = this.repository.get(id);
        if (patient == null) {
            this.logger.debug("No such patient record: [{}]", id);
            return Response.status(Status.NOT_FOUND).build();
        }
        User currentUser = this.users.getCurrentUser();
        DocumentReference currentUserProfile = currentUser == null ? null : currentUser.getProfileDocument();
        Right grantedRight;
        if (!this.access.hasAccess(Right.VIEW, currentUserProfile, patient.getDocument())) {
            this.logger.debug("View access denied to user [{}] on patient record [{}]", currentUser, id);
            return Response.status(Status.FORBIDDEN).build();
        } else {
            grantedRight = Right.VIEW;
        }
        if (this.access.hasAccess(Right.EDIT, currentUserProfile, patient.getDocument())) {
            grantedRight = Right.EDIT;
        }
        Right manageRight = Right.toRight("manage");
        if (manageRight != Right.ILLEGAL
            && this.access.hasAccess(manageRight, currentUserProfile, patient.getDocument())) {
            grantedRight = manageRight;
        }
        JSONObject json = getPatientJson(patient);
        json.put("links",
            this.autolinker.get().forResource(getClass(), this.uriInfo).withGrantedRight(grantedRight).build());
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response updatePatient(String json, String id)
    {
        this.logger.debug("Updating patient record [{}] via REST with JSON: {}", id, json);
        Patient patient = this.repository.get(id);
        if (patient == null) {
            this.logger.debug(
                "Patient record [{}] doesn't exist yet. It can be created by POST-ing the JSON to /rest/patients", id);
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        User currentUser = this.users.getCurrentUser();
        if (!this.access.hasAccess(Right.EDIT, currentUser == null ? null : currentUser.getProfileDocument(),
            patient.getDocument())) {
            this.logger.debug("Edit access denied to user [{}] on patient record [{}]", currentUser, id);
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        if (json == null) {
            // json == null does not create an exception when initializing a JSONObject
            // need to handle it separately to give explicit BAD_REQUEST to the user
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        JSONObject jsonInput;
        try {
            jsonInput = new JSONObject(json);
        } catch (Exception ex) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        String idFromJson = jsonInput.optString("id");
        if (StringUtils.isNotBlank(idFromJson) && !patient.getId().equals(idFromJson)) {
            // JSON for a different patient, bail out
            throw new WebApplicationException(Status.CONFLICT);
        }
        try {
            patient.updateFromJSON(jsonInput);
        } catch (Exception ex) {
            this.logger.warn("Failed to update patient [{}] from JSON: {}. Source JSON was: {}", patient.getId(),
                ex.getMessage(), json);
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }

    @Override
    public Response deletePatient(String id)
    {
        this.logger.debug("Deleting patient record [{}] via REST", id);
        Patient patient = this.repository.get(id);
        if (patient == null) {
            this.logger.debug("Patient record [{}] didn't exist", id);
            return Response.status(Status.NOT_FOUND).build();
        }
        User currentUser = this.users.getCurrentUser();
        if (!this.access.hasAccess(Right.DELETE, currentUser == null ? null : currentUser.getProfileDocument(),
            patient.getDocument())) {
            this.logger.debug("Delete access denied to user [{}] on patient record [{}]", currentUser, id);
            return Response.status(Status.FORBIDDEN).build();
        }
        XWikiContext context = this.getXWikiContext();
        XWiki xwiki = context.getWiki();
        try {
            xwiki.deleteDocument(xwiki.getDocument(patient.getDocument(), context), context);
        } catch (XWikiException ex) {
            this.logger.warn("Failed to delete patient record [{}]: {}", id, ex.getMessage());
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        }
        this.logger.debug("Deleted patient record [{}]", id);
        return Response.noContent().build();
    }

    /**
     * Returns the {@link JSONObject} for the selected {@code patient}. Looks at {@link #INCLUDE_CONTROLLER_URL_PARAM}
     * and {@link #EXCLUDE_CONTROLLER_URL_PARAM} HTTP request URL parameters. If both are specified, throws a
     * {@link WebApplicationException}. If both are {@code null}, returns a {@link JSONObject} with all patient data. If
     * {@link #INCLUDE_CONTROLLER_URL_PARAM} is specified, operates in whitelist mode using the provided set of
     * controllers; if {@link #EXCLUDE_CONTROLLER_URL_PARAM} is specified, operates in blacklist mode using the provided
     * set of controllers.
     * <p>
     * Multiple include or exclude controllers maybe specified in typical servlet fashion by repeating the URL parameter
     * with different argument values, e.g. includeController=controller1&amp;includeController=controller2.
     *
     * @param patient the {@link Patient} whose data is being serialized to {@link JSONObject}
     * @return a {@link JSONObject} that contains the requested {@link Patient} data
     */
    private JSONObject getPatientJson(@Nonnull final Patient patient)
    {
        HttpServletRequest httpServletRequest = ((ServletRequest) this.container.getRequest()).getHttpServletRequest();
        String[] excludedControllerArr = httpServletRequest.getParameterValues(EXCLUDE_CONTROLLER_URL_PARAM);
        String[] includedControllerArr = httpServletRequest.getParameterValues(INCLUDE_CONTROLLER_URL_PARAM);
        if (excludedControllerArr != null && includedControllerArr != null) {
            this.logger.warn("Failed to resolve which data to retrieve: both {} and {} were provided",
                INCLUDE_CONTROLLER_URL_PARAM, EXCLUDE_CONTROLLER_URL_PARAM);
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        final boolean excludeControllers = includedControllerArr == null;
        final Set<String> controllers;
        if (excludeControllers) {
            controllers = excludedControllerArr == null ? null : new HashSet<>(Arrays.asList(excludedControllerArr));
        } else {
            controllers = new HashSet<>(Arrays.asList(includedControllerArr));
        }

        return patient.toJSON(controllers, excludeControllers);
    }
}
