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

public class MembershipHandler extends ObjectsProcessing {

	public MembershipHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(GroupHandler.class);

	private static final String ATTR_ROLE = "role";
	private static final String ATTR_USER = "user";
	private static final String ATTR_GROUP = "group";
	private static final String CRUD_MEMBER = "/2.0/group_memberships";
	private static final String CRUD_GROUP = "/2.0/groups";
	private static final String MEMBERSHIPS = "memberships";
	private static final String UID = "id";

	public void deleteMembership(String objectUid, String subjectUid) {
		if (objectUid == null || objectUid.isEmpty()) {
			throw new InvalidAttributeValueException("objectId not provided or empty");
		}
		if (subjectUid == null || subjectUid.isEmpty()) {
			throw new InvalidAttributeValueException("subjectId not provided or empty");
		}

		HttpDelete request;
		URI memberUri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_MEMBER + "/" + getGroupMemeberships(objectUid, subjectUid));

		try {
			memberUri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		request = new HttpDelete(memberUri);
		callRequest(request, false);

	}

	private String getGroupMemeberships(String uid, String subjectUid) {
		if (uid == null || uid.isEmpty()) {
			throw new InvalidAttributeValueException("objectId not provided or empty");
		}
		if (subjectUid == null || subjectUid.isEmpty()) {
			throw new InvalidAttributeValueException("subjectId not provided or empty");
		}

		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		JSONObject users = new JSONObject();
		String memUid = null;

		uriBuilder.setPath(CRUD_GROUP + "/" + uid + "/" + MEMBERSHIPS);

		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		HttpGet request = new HttpGet(uri);
		JSONObject user = callRequest(request, true);
		JSONArray members = user.getJSONArray("entries");

		for (int i = 0; i < members.length(); i++) {

			JSONObject member = members.getJSONObject(i);

			if (member.has("user")) {
				if (member.get("user") != null && !JSONObject.NULL.equals(member.get("user"))) {

					users = (JSONObject) member.get("user");
					if (subjectUid.equals(String.valueOf(users.get("id")))) {
						memUid = String.valueOf(member.get("id"));
						LOGGER.info("MEMEBERSHIP ID: {0}", memUid);

					}
				}

			}

		}

		return memUid;
	}

	public Uid createMembership(String objectUid, String subjectUid, String role) {
		if (objectUid == null || objectUid.isEmpty()) {
			throw new InvalidAttributeValueException("objectId not provided or empty");
		}
		JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", objectUid);
		if (subjectUid == null || subjectUid.isEmpty()) {
			throw new InvalidAttributeValueException("subjectId not provided or empty");
		}
		JSONObject jsonUser = new JSONObject();
		jsonUser.put("id", subjectUid);

		if (role == null || role.isEmpty()) {
			throw new InvalidAttributeValueException("Role not provided or empty");
		}
		JSONObject json = new JSONObject();
		json.put(ATTR_ROLE, role);

		URIBuilder uriBuilder = getURIBuilder();

		json.put(ATTR_USER, jsonUser);
		json.put(ATTR_GROUP, jsonGroup);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		uriBuilder.setPath(CRUD_MEMBER);
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpPost(uri);
		LOGGER.info("URI {0}", uri.toString());
		LOGGER.info("JSON {0}", json.toString());
		JSONObject jsonReq = callRequest(request, json);
		LOGGER.info("JSON_UID {0}", jsonReq.toString());

		String memUid = jsonReq.getString(UID);
		LOGGER.info("MEMUID {0}", memUid.toString());
		return new Uid(memUid);
	}

}
