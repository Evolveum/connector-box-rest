package com.evolveum.polygon.connector.box.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;

import org.json.JSONArray;
import org.json.JSONObject;

public class GroupHandler extends ObjectsProcessing {

	public GroupHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(GroupHandler.class);

	private static final String ATTR_NAME = "name";
	private static final String ATTR_ID = "groupID";
	private static final String ATTR_PROVENANCE = "provenance";
	private static final String ATTR_IDENTIFIER = "external_sync_identifier";
	private static final String ATTR_DESCRIPTION = "description";
	private static final String ATTR_INVITABILITY = "invitability_level";
	private static final String ATTR_VIEWABILITY = "member_viewability_level";
	private static final String ATTR_CREATED = "created_at";
	private static final String ATTR_MODIFIED = "modified_at";
	private static final String ATTR_SYNC = "is_sync_enabled";
	private static final String ATTR_MEMBERS = "member";
	private static final String ATTR_ADMINS = "admin";
	private static final String ATTR_CO_OWNER = "co_owner";
	private static final String ATTR_EDITOR = "editor";
	private static final String ATTR_PREVIEWER = "previewer";
	private static final String ATTR_PREVIEWER_UPLOADER = "previewer_uploader";
	private static final String ATTR_UPLOADER = "uploader";
	private static final String ATTR_VIEWER = "viewer";
	private static final String ATTR_VIEWER_UPLOADER = "viewer_uploader";

	private static final String CRUD_GROUP = "/2.0/groups";
	private static final String OFFSET = "offset";
	private static final String LIMIT = "limit";
	private static final String UID = "id";

	private static final String MEMBERSHIPS = "memberships";
	private static final String COLLABORATIONS = "collaborations";

	public ObjectClassInfo getGroupSchema() {

		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(ObjectClass.GROUP_NAME);

		AttributeInfoBuilder attrOwner = new AttributeInfoBuilder(ATTR_CO_OWNER);
		attrOwner.setRequired(false);
		attrOwner.setMultiValued(true);
		attrOwner.setCreateable(true);
		attrOwner.setReadable(true);
		attrOwner.setUpdateable(true);
		builder.addAttributeInfo(attrOwner.build());

		AttributeInfoBuilder attrEditor = new AttributeInfoBuilder(ATTR_EDITOR);
		attrEditor.setRequired(false);
		attrEditor.setMultiValued(true);
		attrEditor.setCreateable(true);
		attrEditor.setReadable(true);
		attrEditor.setUpdateable(true);
		builder.addAttributeInfo(attrEditor.build());

		AttributeInfoBuilder attrPreviewer = new AttributeInfoBuilder(ATTR_PREVIEWER);
		attrPreviewer.setRequired(false);
		attrPreviewer.setMultiValued(true);
		attrPreviewer.setCreateable(true);
		attrPreviewer.setReadable(false);
		attrPreviewer.setUpdateable(true);
		attrPreviewer.setReturnedByDefault(false);
		builder.addAttributeInfo(attrPreviewer.build());

		AttributeInfoBuilder attrPrevUpl = new AttributeInfoBuilder(ATTR_PREVIEWER_UPLOADER);
		attrPrevUpl.setRequired(false);
		attrPrevUpl.setMultiValued(true);
		attrPrevUpl.setCreateable(true);
		attrPrevUpl.setReadable(false);
		attrPrevUpl.setUpdateable(true);
		attrPrevUpl.setReturnedByDefault(false);
		builder.addAttributeInfo(attrPrevUpl.build());

		AttributeInfoBuilder attrUploader = new AttributeInfoBuilder(ATTR_UPLOADER);
		attrUploader.setRequired(false);
		attrUploader.setMultiValued(true);
		attrUploader.setCreateable(true);
		attrUploader.setReadable(true);
		attrUploader.setUpdateable(true);
		attrUploader.setReturnedByDefault(false);
		builder.addAttributeInfo(attrUploader.build());

		AttributeInfoBuilder attrViewer = new AttributeInfoBuilder(ATTR_VIEWER);
		attrViewer.setRequired(false);
		attrViewer.setMultiValued(true);
		attrViewer.setCreateable(true);
		attrViewer.setReadable(false);
		attrViewer.setUpdateable(true);
		attrViewer.setReturnedByDefault(false);
		builder.addAttributeInfo(attrViewer.build());

		AttributeInfoBuilder attrViewUpl = new AttributeInfoBuilder(ATTR_VIEWER_UPLOADER);
		attrViewUpl.setRequired(false);
		attrViewUpl.setMultiValued(true);
		attrViewUpl.setCreateable(true);
		attrViewUpl.setReadable(false);
		attrViewUpl.setUpdateable(true);
		attrViewUpl.setReturnedByDefault(false);
		builder.addAttributeInfo(attrViewUpl.build());

		AttributeInfoBuilder attrProvenanceBuilder = new AttributeInfoBuilder(ATTR_PROVENANCE);
		attrProvenanceBuilder.setRequired(false);
		attrProvenanceBuilder.setMultiValued(false);
		attrProvenanceBuilder.setCreateable(true);
		attrProvenanceBuilder.setReadable(false);
		attrProvenanceBuilder.setUpdateable(true);
		attrProvenanceBuilder.setReturnedByDefault(false);
		builder.addAttributeInfo(attrProvenanceBuilder.build());

		AttributeInfoBuilder attrGroupId = new AttributeInfoBuilder(ATTR_ID);
		attrGroupId.setRequired(false);
		attrGroupId.setMultiValued(false);
		attrGroupId.setCreateable(false);
		attrGroupId.setReadable(true);
		attrGroupId.setUpdateable(false);
		builder.addAttributeInfo(attrGroupId.build());

		AttributeInfoBuilder attrMembers = new AttributeInfoBuilder(ATTR_MEMBERS);
		attrMembers.setMultiValued(true);
		attrMembers.setRequired(false);
		attrMembers.setCreateable(true);
		attrMembers.setReadable(true);
		attrMembers.setUpdateable(true);
		builder.addAttributeInfo(attrMembers.build());

		AttributeInfoBuilder attrAdmins = new AttributeInfoBuilder(ATTR_ADMINS);
		attrAdmins.setMultiValued(true);
		attrAdmins.setRequired(false);
		attrAdmins.setCreateable(true);
		attrAdmins.setReadable(true);
		attrAdmins.setUpdateable(true);
		builder.addAttributeInfo(attrAdmins.build());

		AttributeInfoBuilder attrIdentifierBuilder = new AttributeInfoBuilder(ATTR_IDENTIFIER);
		attrIdentifierBuilder.setMultiValued(false);
		attrIdentifierBuilder.setRequired(false);
		attrIdentifierBuilder.setCreateable(true);
		attrIdentifierBuilder.setReadable(false);
		attrIdentifierBuilder.setUpdateable(true);
		attrIdentifierBuilder.setReturnedByDefault(false);
		builder.addAttributeInfo(attrIdentifierBuilder.build());

		AttributeInfoBuilder attrDescriptionBuilder = new AttributeInfoBuilder(ATTR_DESCRIPTION);
		attrDescriptionBuilder.setMultiValued(false);
		attrDescriptionBuilder.setRequired(false);
		attrDescriptionBuilder.setCreateable(true);
		attrDescriptionBuilder.setReadable(false);
		attrDescriptionBuilder.setUpdateable(true);
		attrDescriptionBuilder.setReturnedByDefault(false);
		builder.addAttributeInfo(attrDescriptionBuilder.build());

		AttributeInfoBuilder attrInvitabilityBuilder = new AttributeInfoBuilder(ATTR_INVITABILITY);
		attrInvitabilityBuilder.setMultiValued(false);
		attrInvitabilityBuilder.setRequired(false);
		attrInvitabilityBuilder.setCreateable(true);
		attrInvitabilityBuilder.setReadable(false);
		attrInvitabilityBuilder.setUpdateable(true);
		attrInvitabilityBuilder.setReturnedByDefault(false);
		builder.addAttributeInfo(attrInvitabilityBuilder.build());

		AttributeInfoBuilder attrViewabilityBuilder = new AttributeInfoBuilder(ATTR_VIEWABILITY);
		attrViewabilityBuilder.setMultiValued(false);
		attrViewabilityBuilder.setRequired(false);
		attrViewabilityBuilder.setCreateable(true);
		attrViewabilityBuilder.setReadable(false);
		attrViewabilityBuilder.setUpdateable(true);
		attrViewabilityBuilder.setReturnedByDefault(false);
		builder.addAttributeInfo(attrViewabilityBuilder.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		attrCreated.setMultiValued(false);
		attrCreated.setRequired(false);
		attrCreated.setCreateable(false);
		attrCreated.setReadable(true);
		attrCreated.setUpdateable(false);
		builder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		attrModified.setMultiValued(false);
		attrModified.setRequired(false);
		attrModified.setCreateable(false);
		attrModified.setReadable(true);
		attrModified.setUpdateable(false);
		builder.addAttributeInfo(attrModified.build());

		ObjectClassInfo groupSchemaInfo = builder.build();
		LOGGER.info("The constructed group schema representation: {0}", groupSchemaInfo);
		return groupSchemaInfo;

	}

	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {

		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		int pageNumber = 0;
		int usersPerPage = 0;
		int offset = 0;

		if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {

			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD_GROUP + "/" + uid.getUidValue().toString());

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
				ConnectorObject connectorObject = convertToConnectorObject(group, query);
				handler.handle(connectorObject);

			}

		} else if (query == null && objectClass.is(ObjectClass.GROUP_NAME)) {

			uriBuilder.setPath(CRUD_GROUP);

			if (options != null) {
				if ((options.getPageSize()) != null) {
					usersPerPage = options.getPageSize();
					uriBuilder.addParameter(LIMIT, String.valueOf(usersPerPage));
					if (options.getPagedResultsOffset() != null) {
						pageNumber = options.getPagedResultsOffset();
						offset = (pageNumber * usersPerPage) - usersPerPage;
						uriBuilder.addParameter(OFFSET, String.valueOf(offset));
					}
				}
			}
			try {
				uri = uriBuilder.build();

			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}

			HttpGet request = new HttpGet(uri);
			handleGroups(request, handler, options, query);

		} else if (configuration.isEnableFilteredResultsHandler()) {

			uriBuilder.setPath(CRUD_GROUP);
			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleGroups(request, handler, options, query);

		} else {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";")
					.append("Please note that its recommended to enable filteredResultsHandler");
			throw new ConnectorException(sb.toString());

		}

	}

	private boolean handleGroups(HttpGet request, ResultsHandler handler, OperationOptions options, Filter query) {

		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}

		JSONObject result = callRequest(request);
		JSONArray groups = result.getJSONArray("entries");

		for (int i = 0; i < groups.length(); i++) {
			JSONObject group = groups.getJSONObject(i);

			ConnectorObject connectorObject = convertToConnectorObject(group, query);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}

		return false;
	}

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {

		if (uid == null) {
			throw new InvalidAttributeValueException("uid not provided");
		}

		HttpDelete request;
		URI groupUri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_GROUP + "/" + uid.getUidValue().toString());

		try {
			groupUri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		request = new HttpDelete(groupUri);
		callRequest(request, false);

	}

	public Uid createOrUpdateGroup(Uid uid, Set<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			throw new InvalidAttributeValueException("attributes not provided or empty");

		}

		boolean create = uid == null;
		JSONObject json = new JSONObject();
		URIBuilder uriBuilder = getURIBuilder();

		String name = getStringAttr(attributes, "__NAME__");

		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}

		if (name != null) {
			json.put(ATTR_NAME, name);
		}

		putFieldIfExists(attributes, ATTR_ID, String.class, json);
		putFieldIfExists(attributes, ATTR_PROVENANCE, String.class, json);
		putFieldIfExists(attributes, ATTR_IDENTIFIER, String.class, json);
		putFieldIfExists(attributes, ATTR_DESCRIPTION, String.class, json);
		putFieldIfExists(attributes, ATTR_INVITABILITY, String.class, json);
		putFieldIfExists(attributes, ATTR_VIEWABILITY, String.class, json);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		if (create) {
			uriBuilder.setPath(CRUD_GROUP);
			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);

		} else {
			uriBuilder.setPath(CRUD_GROUP + "/" + uid.getUidValue().toString());
			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}

			request = new HttpPut(uri);
		}
		JSONObject jsonReq = callRequest(request, json);
		String newUid = jsonReq.getString(UID);

		return new Uid(newUid);

	}

	public ConnectorObject convertToConnectorObject(JSONObject json, Filter query) {

		if (json == null) {
			throw new InvalidAttributeValueException("GroupObject not provided");
		}

		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass("__GROUP__"));
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}
		getIfExists(json, ATTR_ID, builder);
		getIfExists(json, ATTR_PROVENANCE, builder);
		getIfExists(json, ATTR_DESCRIPTION, builder);
		getIfExists(json, ATTR_SYNC, builder);
		getIfExists(json, ATTR_INVITABILITY, builder);
		getIfExists(json, ATTR_VIEWABILITY, builder);
		getIfExists(json, ATTR_CREATED, builder);
		getIfExists(json, ATTR_MODIFIED, builder);

		getGroupMemeberships(json.getString(UID), builder);
		getGroupCollaborations(json.getString(UID), builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;
	}

	private void getGroupMemeberships(String uid, ConnectorObjectBuilder builder) {
		if (uid == null) {
			throw new InvalidAttributeValueException("uid not provided");
		}

		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		JSONObject users = new JSONObject();

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
		// String role = String.valueOf(user.get("role"));

		String[] membersAttr = new String[members.length()];
		String[] adminsAttr = new String[members.length()];

		for (int i = 0; i < members.length(); i++) {

			JSONObject member = members.getJSONObject(i);

			String role = String.valueOf(member.get("role"));
			if (role.equals("member")) {

				if (member.has("user")) {
					if (member.get("user") != null && !JSONObject.NULL.equals(member.get("user"))) {

						users = (JSONObject) member.get("user");
					}

					StringBuilder memInfo = new StringBuilder();
					memInfo.append(String.valueOf(users.get("id")));

					membersAttr[i] = memInfo.toString();

				}
			} else {
				if (member.has("user")) {
					if (member.get("user") != null && !JSONObject.NULL.equals(member.get("user"))) {

						users = (JSONObject) member.get("user");
					}

					StringBuilder memInfo = new StringBuilder();
					memInfo.append(String.valueOf(users.get("id")));

					adminsAttr[i] = memInfo.toString();

				}
			}
		}

		if (membersAttr.length > 0) {
			builder.addAttribute(ATTR_MEMBERS, membersAttr);
		}
		if (adminsAttr.length > 0) {
			builder.addAttribute(ATTR_ADMINS, adminsAttr);
		}
	}

	private void getGroupCollaborations(String uid, ConnectorObjectBuilder builder) {
		if (uid == null) {
			throw new InvalidAttributeValueException("uid not provided");
		}

		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		JSONObject item = new JSONObject();

		uriBuilder.setPath(CRUD_GROUP + "/" + uid + "/" + COLLABORATIONS);
		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpGet request = new HttpGet(uri);
		JSONObject groups = callRequest(request, true);
		JSONArray collaborations = groups.getJSONArray("entries");

		String[] editorAttr = new String[collaborations.length()];
		String[] viewerAttr = new String[collaborations.length()];
		String[] previewerAttr = new String[collaborations.length()];
		String[] uploaderAttr = new String[collaborations.length()];
		String[] prevUploaderAttr = new String[collaborations.length()];
		String[] viewUploaderAttr = new String[collaborations.length()];
		String[] coownerAttr = new String[collaborations.length()];

		for (int i = 0; i < collaborations.length(); i++) {

			JSONObject collab = collaborations.getJSONObject(i);

			String role = String.valueOf(collab.get("role"));

			if (role.equals("editor")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					editorAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("viewer")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					viewerAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("previewer")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					previewerAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("uploader")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					uploaderAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("previewer uploader")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					prevUploaderAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("viewer uploader")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					viewUploaderAttr[i] = collabInfo.toString();

				}
			}
			if (role.equals("co-owner")) {

				if (collab.has("item")) {
					if (collab.get("item") != null && !JSONObject.NULL.equals(collab.get("item"))) {

						item = (JSONObject) collab.get("item");
					}
					StringBuilder collabInfo = new StringBuilder();
					collabInfo.append(String.valueOf(item.get("id")));

					coownerAttr[i] = collabInfo.toString();

				}
			}

		}
		if (editorAttr.length > 0) {
			builder.addAttribute(ATTR_EDITOR, editorAttr);
		}
		if (viewerAttr.length > 0) {
			builder.addAttribute(ATTR_VIEWER, viewerAttr);
		}
		if (previewerAttr.length > 0) {
			builder.addAttribute(ATTR_PREVIEWER, previewerAttr);
		}
		if (viewUploaderAttr.length > 0) {
			builder.addAttribute(ATTR_VIEWER_UPLOADER, viewUploaderAttr);
		}
		if (prevUploaderAttr.length > 0) {
			builder.addAttribute(ATTR_PREVIEWER_UPLOADER, prevUploaderAttr);
		}
		if (uploaderAttr.length > 0) {
			builder.addAttribute(ATTR_UPLOADER, uploaderAttr);
		}
		if (coownerAttr.length > 0) {
			builder.addAttribute(ATTR_CO_OWNER, coownerAttr);
		}

	}

}
