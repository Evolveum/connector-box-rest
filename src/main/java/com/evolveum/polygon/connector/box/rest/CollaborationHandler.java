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
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.json.JSONArray;
import org.json.JSONObject;

public class CollaborationHandler extends ObjectsProcessing {

	public CollaborationHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(CollaborationHandler.class);

	private static final String ATTR_ITEM_ID = "item.id";
	private static final String ATTR_ITEM_TYPE = "item.type";
	private static final String ATTR_BY_ID = "accessible_by.id";
	private static final String ATTR_BY_TYPE = "accessible_by.type";
	private static final String ATTR_BY_LOGIN = "accessible_by.login";
	private static final String ATTR_ROLE = "role";
	private static final String ATTR_PATH = "can_view_path";
	private static final String COLLABORATION_NAME = "Collaborations";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_ITEM = "item";
	private static final String ATTR_ACCESSIBLE = "accessible_by";
	private static final String CRUD_COLLAB = "/2.0/collaborations";
	private static final String CRUD_GROUP = "/2.0/groups";
	private static final String COLLAB = "collaborations";
	private static final String UID = "id";

	public ObjectClassInfo getCollaborationSchema() {
		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(COLLABORATION_NAME);

		AttributeInfoBuilder attrItemIdBuilder = new AttributeInfoBuilder(ATTR_ITEM_ID);
		attrItemIdBuilder.setRequired(true);
		builder.addAttributeInfo(attrItemIdBuilder.build());

		AttributeInfoBuilder attrItemTypeBuilder = new AttributeInfoBuilder(ATTR_ITEM_TYPE);
		attrItemTypeBuilder.setRequired(true);
		builder.addAttributeInfo(attrItemTypeBuilder.build());

		AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
		attrRoleBuilder.setRequired(true);
		builder.addAttributeInfo(attrRoleBuilder.build());

		AttributeInfoBuilder attrByIdBuilder = new AttributeInfoBuilder(ATTR_BY_ID);
		builder.addAttributeInfo(attrByIdBuilder.build());

		AttributeInfoBuilder attrByTypeBuilder = new AttributeInfoBuilder(ATTR_BY_TYPE);
		builder.addAttributeInfo(attrByTypeBuilder.build());

		AttributeInfoBuilder attrByLoginBuilder = new AttributeInfoBuilder(ATTR_BY_LOGIN);
		builder.addAttributeInfo(attrByLoginBuilder.build());

		AttributeInfoBuilder attrPathBuilder = new AttributeInfoBuilder(ATTR_PATH, Boolean.class);
		builder.addAttributeInfo(attrPathBuilder.build());

		/*AttributeInfoBuilder attrSequence = new AttributeInfoBuilder(ATTR_ITEM_SEQUENCE);
		builder.addAttributeInfo(attrSequence.build());

		AttributeInfoBuilder attrEtag = new AttributeInfoBuilder(ATTR_ITEM_ETAG);
		builder.addAttributeInfo(attrEtag.build());

		AttributeInfoBuilder attrItemName = new AttributeInfoBuilder(ATTR_ITEM_NAME);
		builder.addAttributeInfo(attrItemName.build());

		AttributeInfoBuilder attrByName = new AttributeInfoBuilder(ATTR_BY_NAME);
		builder.addAttributeInfo(attrByName.build());

		AttributeInfoBuilder attrAcknowledged = new AttributeInfoBuilder(ATTR_ACKNOWLEDGED);
		builder.addAttributeInfo(attrAcknowledged.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		builder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		builder.addAttributeInfo(attrModified.build());

		AttributeInfoBuilder attrExpires = new AttributeInfoBuilder(ATTR_EXPIRES);
		builder.addAttributeInfo(attrExpires.build());*/

		AttributeInfoBuilder attrStatus = new AttributeInfoBuilder(ATTR_STATUS);
		builder.addAttributeInfo(attrStatus.build());

		ObjectClassInfo collaborationSchemaInfo = builder.build();
		LOGGER.info("The constructed collaboration schema representation: {0}", collaborationSchemaInfo);
		return collaborationSchemaInfo;

	}

	/*public void executeCollabQuery(ObjectClass objectClass, Filter query, ResultsHandler handler,
			OperationOptions options) {

		Name name = null;
		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();

		// StartWithFilter
		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();
			if (name != null) {

				uriBuilder.setPath(CRUD_COLLAB);
				uriBuilder.addParameter(FILTERTERM, name.getNameValue());

				try {
					uri = uriBuilder.build();
				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				handleCollabs(request, handler, options);
			}
			// EqualsFilter
		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();

			uriBuilder.setPath(CRUD_COLLAB + "/" + uid.getUidValue().toString());

			if (uid != null) {
				try {
					uri = uriBuilder.build();
				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject collab = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(collab);
				handler.handle(connectorObject);
			}

		} else if (query == null && objectClass.is(COLLABORATION_NAME)) {

			uriBuilder.setPath(CRUD_COLLAB);

			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleCollabs(request, handler, options);

		} else if (configuration.isEnableFilteredResultsHandler() == true) {

			uriBuilder.setPath(CRUD_COLLAB);
			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleCollabs(request, handler, options);
		}

		
		 else if (query instanceof ContainsFilter && ((ContainsFilter)
		 query).getAttribute() instanceof Name) { name = (Name)
		  ((StartsWithFilter) query).getAttribute(); if (name != null) { try {
		  uri = getURIBuilder().setPath(CRUD_COLLAB).addParameter(FILTERTERM,
		  name.getNameValue()) .build();
		  
		  } catch (URISyntaxException e) { StringBuilder sb = new
		  StringBuilder();
		  sb.append("It is not possible to create URI from URIBuilder:")
		  .append(getURIBuilder().toString()).append(";").append(e.
		  getLocalizedMessage()); throw new ConnectorException(sb.toString(),
		  e); } HttpGet request = new HttpGet(uri); handleCollabs(request,
		  handler, options, CRUD_COLLAB); } // ContainsAllValuesFilter made
		  like StartWith } else if (query instanceof ContainsAllValuesFilter &&
		  ((ContainsAllValuesFilter) query).getAttribute() instanceof Name) {
		  name = (Name) ((StartsWithFilter) query).getAttribute(); if (name !=
		  null) { try { uri =
		  getURIBuilder().setPath(CRUD_COLLAB).addParameter(FILTERTERM,
		  name.getNameValue()) .build();
		  
		  } catch (URISyntaxException e) { StringBuilder sb = new
		  StringBuilder();
		  sb.append("It is not possible to create URI from URIBuilder:")
		 .append(getURIBuilder().toString()).append(";").append(e.
		  getLocalizedMessage()); throw new ConnectorException(sb.toString(),
		  e); } HttpGet request = new HttpGet(uri); handleCollabs(request,
		  handler, options, CRUD_COLLAB); } // List all object }
		 
	}*/

	/*private boolean handleCollabs(HttpGet request, ResultsHandler handler, OperationOptions options) {
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}

		JSONObject result = callRequest(request);
		LOGGER.info("JSON {0}", result);
		JSONArray collabs = result.getJSONArray("entries");

		for (int i = 0; i < collabs.length(); i++) {
			JSONObject collab = collabs.getJSONObject(i);
			LOGGER.ok("response body Handle user: {0}", collab);

			ConnectorObject connectorObject = convertToConnectorObject(collab);
			boolean finish = !handler.handle(connectorObject);
			if (finish) {
				return true;
			}

		}

		return false;
	}*/

	/*public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		HttpDelete request;
		URI collabUri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_COLLAB + "/" + uid.getUidValue().toString());
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
	}*/

	/*public Uid createOrUpdateCollaboration(Uid uid, Set<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			LOGGER.error("Attributes not provided {0} ", attributes);
			return uid;
		}

		boolean create = uid == null;
		JSONObject json = new JSONObject();
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonAccessible = new JSONObject();
		URIBuilder uriBuilder = getURIBuilder();

		// required attribute name
		String itemId = getStringAttr(attributes, ATTR_ITEM_ID);
		if (create && StringUtil.isBlank(itemId)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_ITEM_ID);
		}
		if (itemId != null) {
			jsonItem.put("id", itemId);
		}

		String itemType = getStringAttr(attributes, ATTR_ITEM_TYPE);
		if (create && StringUtil.isBlank(itemType)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_ITEM_TYPE);
		}
		if (itemType != null) {
			jsonItem.put("type", itemType);
		}

		String role = getStringAttr(attributes, ATTR_ROLE);
		if (create && StringUtil.isBlank(role)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_ROLE);
		}
		if (role != null) {
			json.put(ATTR_ROLE, role);
		}

		putChildFieldIfExists(attributes, "id", ATTR_BY_ID, jsonAccessible);
		putChildFieldIfExists(attributes, "login", ATTR_BY_LOGIN, jsonAccessible);
		putChildFieldIfExists(attributes, "type", ATTR_BY_TYPE, jsonAccessible);
		putFieldIfExists(attributes, ATTR_PATH, json);
		putFieldIfExists(attributes, ATTR_STATUS, json);

		json.put(ATTR_ITEM, jsonItem);
		json.put(ATTR_ACCESSIBLE, jsonAccessible);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		if (create) {
			uriBuilder.setPath(CRUD_COLLAB);
			try {
				uri = uriBuilder.build();
				LOGGER.info("URI", uri);
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			request = new HttpPost(uri);

		} else {
			uriBuilder.setPath(CRUD_COLLAB + "/" + uid.getUidValue().toString());
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
		LOGGER.ok("UID {0}", newUid);

		return new Uid(newUid);
	}*/

	/*public ConnectorObject convertToConnectorObject(JSONObject json) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		JSONObject jsonItem = new JSONObject();
		JSONObject jsonAccessible = new JSONObject();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass(COLLABORATION_NAME));
		if (json.has(ATTR_ITEM)) {
			if (json.get(ATTR_ITEM) != null && !JSONObject.NULL.equals(json.get(ATTR_ITEM))) {
				jsonItem = (JSONObject) json.get(ATTR_ITEM);
			}

		}
		if (json.has(ATTR_ACCESSIBLE)) {
			if (json.get(ATTR_ACCESSIBLE) != null && !JSONObject.NULL.equals(json.get(ATTR_ACCESSIBLE))) {
				jsonAccessible = (JSONObject) json.get(ATTR_ACCESSIBLE);
			}

		}
		getIfExists(jsonItem, "id", builder);
		getIfExists(jsonItem, "type", builder);
		getIfExists(jsonAccessible, "id", builder);
		getIfExists(jsonAccessible, "login", builder);
		getIfExists(jsonAccessible, "type", builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;
	}*/
	
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
	
	private String getSubjectCollabs(String uid, String subjectUid){
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
					if(subjectUid.equals(String.valueOf(groups.get("id")))){
						collabUid = String.valueOf(collab.get("id"));
						 LOGGER.info("COLLABORATION ID: {0}", collabUid);
						 
					}
				}
				
			}
			
		}
		
		return collabUid;
	}

}
