package com.evolveum.polygon.connector.box.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Uid;
import org.json.JSONArray;
import org.json.JSONObject;

public class CollaborationHandler extends ObjectsProcessing {

	public CollaborationHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(CollaborationHandler.class);

	private static final String ATTR_ROLE = "role";
	private static final String ATTR_ITEM = "item";
	private static final String ATTR_ACCESSIBLE = "accessible_by";
	private static final String CRUD_COLLAB = "/2.0/collaborations";
	private static final String CRUD_GROUP = "/2.0/groups";
	private static final String COLLAB = "collaborations";
	private static final String UID = "id";

	public Uid createCollaboration(String groupUid, String folderUid, String role) {
		if (groupUid == null || groupUid.isEmpty()) {
			throw new InvalidAttributeValueException("GroupId not provided or empty");
		}
		JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", groupUid);
		jsonGroup.put("type", "group");

		if (folderUid == null || folderUid.isEmpty()) {
			throw new InvalidAttributeValueException("FolderId not provided or empty");
		}
		JSONObject jsonItem = new JSONObject();
		jsonItem.put("id", folderUid);
		jsonItem.put("type", "folder");

		if (role == null || role.isEmpty()) {
			throw new InvalidAttributeValueException("Role not provided or empty");
		}
		JSONObject json = new JSONObject();
		if (role.equals("co_owner")) {
			role = "co-owner";
		}
		if (role.equals("viewer_uploader")) {
			role = "viewer uploader";
		}
		if (role.equals("previewer_uploader")) {
			role = "previewer uploader";
		}
		json.put(ATTR_ROLE, role);
		json.put(ATTR_ACCESSIBLE, jsonGroup);
		json.put(ATTR_ITEM, jsonItem);

		URIBuilder uriBuilder = getURIBuilder();
		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		uriBuilder.setPath(CRUD_COLLAB);
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		request = new HttpPost(uri);

		JSONObject jsonReq = callRequest(request, json);

		String collabUid = jsonReq.getString(UID);
		return new Uid(collabUid);

	}

	public void deleteCollaboration(String objectUid, String subjectUid) {
		if (objectUid == null || objectUid.isEmpty()) {
			throw new InvalidAttributeValueException("objectId not provided or empty");
		}
		if (subjectUid == null || subjectUid.isEmpty()) {
			throw new InvalidAttributeValueException("subjectId not provided or empty");
		}
		HttpDelete request;
		URI collabUri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_COLLAB + "/" + getSubjectCollabs(objectUid, subjectUid));

		try {
			collabUri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		request = new HttpDelete(collabUri);
		callRequest(request, false);

	}

	private String getSubjectCollabs(String uid, String subjectUid) {
		if (uid == null || uid.isEmpty()) {
			throw new InvalidAttributeValueException("objectId not provided or empty");
		}
		if (subjectUid == null || subjectUid.isEmpty()) {
			throw new InvalidAttributeValueException("subjectId not provided or empty");
		}

		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		JSONObject groups = new JSONObject();
		String collabUid = null;

		uriBuilder.setPath(CRUD_GROUP + "/" + uid + "/" + COLLAB);

		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		HttpGet request = new HttpGet(uri);
		JSONObject group = callRequest(request, true);
		JSONArray collabs = group.getJSONArray("entries");

		for (int i = 0; i < collabs.length(); i++) {

			JSONObject collab = collabs.getJSONObject(i);

			if (collab.has("item")) {
				if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

					groups = (JSONObject) collab.get("item");
					if (subjectUid.equals(String.valueOf(groups.get("id")))) {
						collabUid = String.valueOf(collab.get("id"));
						LOGGER.info("COLLABORATION ID: {0}", collabUid);

					}
				}

			}

		}

		return collabUid;
	}

}
