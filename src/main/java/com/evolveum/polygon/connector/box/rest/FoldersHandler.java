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

public class FoldersHandler extends ObjectsProcessing {

	public FoldersHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final Log LOGGER = Log.getLog(FoldersHandler.class);

	private static final String ATTR_NAME = "name";
	private static final String ATTR_PARENT = "parent";
	private static final String ATTR_FOLDER_DESC = "description";
	private static final String ATTR_SYNC_STATE = "sync_state";
	private static final String ATTR_TAGS = "tags";
	private static final String ATTR_NONOWNERS_INVITE = "can_non_owners_invite";
	private static final String ATTR_SEQUENCE_ID = "sequence_id";
	private static final String ATTR_ETAG = "etag";
	private static final String ATTR_CREATED = "created_at";
	private static final String ATTR_MODIFIED = "modified_at";
	private static final String ATTR_SIZE = "size";
	private static final String ATTR_PC_TOTAL_COUNT = "path_collection.total_count";
	private static final String ATTR_PC_ENTRIES = "path_collection.entries";
	private static final String ATTR_CB_TYPE = "created_by.type";
	private static final String ATTR_CB_ID = "created_by.id";
	private static final String ATTR_CB_LOGIN = "created_by.login";
	private static final String ATTR_CB_NAME = "created_by.name";
	private static final String ATTR_MB_TYPE = "modified_by.type";
	private static final String ATTR_MB_ID = "modified_by.id";
	private static final String ATTR_MB_LOGIN = "modified_by.login";
	private static final String ATTR_MB_NAME = "modified_by.name";
	private static final String ATTR_OB_TYPE = "owned_by.type";
	private static final String ATTR_OB_ID = "owned_by.id";
	private static final String ATTR_OB_LOGIN = "owned_by.login";
	private static final String ATTR_OB_NAME = "owned_by.name";
	private static final String ATTR_SL_ACCESS = "shared_link.access";
	private static final String ATTR_SL_PASSWORD = "shared_link.password";
	private static final String ATTR_SL_UNSHARED = "shared_link.unshared_at";
	private static final String ATTR_SL_PER_DOWNLOAD = "shared_link.permissions.can_download";
	private static final String ATTR_SL_PER_PREVIEW = "shared_link.permissions.can_preview";
	private static final String ATTR_SL_URL = "shared_link.url";
	private static final String ATTR_SL_DOWN_URL = "shared_link.download_url";
	private static final String ATTR_SL_VANITY_URL = "shared_link.vanity_url";
	private static final String ATTR_SL_PSSW_ENABLED = "shared_link.is_password_enabled";
	private static final String ATTR_SL_DOWN_COUNT = "shared_link.download_count";
	private static final String ATTR_SL_PREV_COUNT = "shared_link.preview_count";
	private static final String ATTR_FUE_ACCESS = "folder_upload_email.access";
	private static final String ATTR_FUE_EMAIL = "folder_upload_email.email";
	private static final String ATTR_PARENT_ID = "parent.id";
	private static final String ATTR_PARENT_NAME = "parent.name";
	private static final String ATTR_PARENT_SEQUENCE_ID = "parent.sequence_id";
	private static final String ATTR_PARENT_ETAG = "parent.etag";
	private static final String ATTR_ITEM_STATUS = "item";
	private static final String ATTR_ITEM_COLLECTION_ENTRIES = "item_collection.entries";

	private static final String PATH = "path_collection";
	private static final String CREATED = "created_by";
	private static final String MODIFIED = "modified_by";
	private static final String OWNED = "owned_by";
	private static final String SHARED = "shared_link";
	private static final String FUE = "folder_upload_email";
	private static final String COLLECTION = "item_collection";
	private static final String ENTRIES = "entries";
	private static final String TYPE = "type";
	private static final String LOGIN = "login";
	private static final String PERMISSIONS = "permissions";
	private static final String UID = "id";
	private static final String FOLDER_NAME = "Folders";
	private static final String CRUD_FOLDER = "/2.0/folders";

	public ObjectClassInfo getFoldersSchema() {

		ObjectClassInfoBuilder builder = new ObjectClassInfoBuilder();

		builder.setType(FOLDER_NAME);

		AttributeInfoBuilder attrParentBuilder = new AttributeInfoBuilder(ATTR_PARENT_ID);
		attrParentBuilder.setRequired(true);
		attrParentBuilder.setMultiValued(false);
		attrParentBuilder.setCreateable(true);
		attrParentBuilder.setReadable(true);
		attrParentBuilder.setUpdateable(true);
		builder.addAttributeInfo(attrParentBuilder.build());

		AttributeInfoBuilder attrParentName = new AttributeInfoBuilder(ATTR_PARENT_NAME);
		attrParentName.setRequired(false);
		attrParentName.setMultiValued(false);
		attrParentName.setCreateable(false);
		attrParentName.setReadable(true);
		attrParentName.setUpdateable(false);
		builder.addAttributeInfo(attrParentName.build());

		AttributeInfoBuilder attrDesc = new AttributeInfoBuilder(ATTR_FOLDER_DESC);
		attrDesc.setRequired(false);
		attrDesc.setMultiValued(false);
		attrDesc.setCreateable(false);
		attrDesc.setReadable(true);
		attrDesc.setUpdateable(true);
		builder.addAttributeInfo(attrDesc.build());

		AttributeInfoBuilder attrSlAccess = new AttributeInfoBuilder(ATTR_SL_ACCESS);
		attrSlAccess.setRequired(false);
		attrSlAccess.setMultiValued(false);
		attrSlAccess.setCreateable(false);
		attrSlAccess.setReadable(true);
		attrSlAccess.setUpdateable(true);
		builder.addAttributeInfo(attrSlAccess.build());

		AttributeInfoBuilder attrSlPassword = new AttributeInfoBuilder(ATTR_SL_PASSWORD);
		attrSlPassword.setRequired(false);
		attrSlPassword.setMultiValued(false);
		attrSlPassword.setCreateable(false);
		attrSlPassword.setReadable(true);
		attrSlPassword.setUpdateable(true);
		builder.addAttributeInfo(attrSlPassword.build());

		AttributeInfoBuilder attrSlUnshared = new AttributeInfoBuilder(ATTR_SL_UNSHARED, Integer.class);
		attrSlUnshared.setRequired(false);
		attrSlUnshared.setMultiValued(false);
		attrSlUnshared.setCreateable(false);
		attrSlUnshared.setReadable(true);
		attrSlUnshared.setUpdateable(true);
		builder.addAttributeInfo(attrSlUnshared.build());

		AttributeInfoBuilder attrSlPerDownload = new AttributeInfoBuilder(ATTR_SL_PER_DOWNLOAD, Boolean.class);
		attrSlPerDownload.setRequired(false);
		attrSlPerDownload.setMultiValued(false);
		attrSlPerDownload.setCreateable(false);
		attrSlPerDownload.setReadable(true);
		attrSlPerDownload.setUpdateable(true);
		builder.addAttributeInfo(attrSlPerDownload.build());

		AttributeInfoBuilder attrSlPerPreview = new AttributeInfoBuilder(ATTR_SL_PER_PREVIEW, Boolean.class);
		attrSlPerPreview.setRequired(false);
		attrSlPerPreview.setMultiValued(false);
		attrSlPerPreview.setCreateable(false);
		attrSlPerPreview.setReadable(true);
		attrSlPerPreview.setUpdateable(false);
		builder.addAttributeInfo(attrSlPerPreview.build());

		AttributeInfoBuilder attrNonOwnersInvite = new AttributeInfoBuilder(ATTR_NONOWNERS_INVITE);
		attrNonOwnersInvite.setRequired(false);
		attrNonOwnersInvite.setMultiValued(false);
		attrNonOwnersInvite.setCreateable(false);
		attrNonOwnersInvite.setReadable(true);
		attrNonOwnersInvite.setUpdateable(true);
		builder.addAttributeInfo(attrNonOwnersInvite.build());

		AttributeInfoBuilder attrSyncState = new AttributeInfoBuilder(ATTR_SYNC_STATE);
		attrSyncState.setRequired(false);
		attrSyncState.setMultiValued(false);
		attrSyncState.setCreateable(false);
		attrSyncState.setReadable(true);
		attrSyncState.setUpdateable(true);
		builder.addAttributeInfo(attrSyncState.build());

		AttributeInfoBuilder attrTags = new AttributeInfoBuilder(ATTR_TAGS);
		attrTags.setRequired(false);
		attrTags.setMultiValued(false);
		attrTags.setCreateable(false);
		attrTags.setReadable(true);
		attrTags.setUpdateable(true);
		builder.addAttributeInfo(attrTags.build());

		AttributeInfoBuilder attrSequenceID = new AttributeInfoBuilder(ATTR_SEQUENCE_ID);
		attrSequenceID.setRequired(false);
		attrSequenceID.setMultiValued(false);
		attrSequenceID.setCreateable(false);
		attrSequenceID.setReadable(true);
		attrSequenceID.setUpdateable(false);
		builder.addAttributeInfo(attrSequenceID.build());

		AttributeInfoBuilder attrEtag = new AttributeInfoBuilder(ATTR_ETAG);
		attrEtag.setRequired(false);
		attrEtag.setMultiValued(false);
		attrEtag.setCreateable(false);
		attrEtag.setReadable(true);
		attrEtag.setUpdateable(false);
		builder.addAttributeInfo(attrEtag.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		attrCreated.setRequired(false);
		attrCreated.setMultiValued(false);
		attrCreated.setCreateable(false);
		attrCreated.setReadable(true);
		attrCreated.setUpdateable(false);
		builder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		attrModified.setRequired(false);
		attrModified.setMultiValued(false);
		attrModified.setCreateable(false);
		attrModified.setReadable(true);
		attrModified.setUpdateable(false);
		builder.addAttributeInfo(attrModified.build());

		AttributeInfoBuilder attrSize = new AttributeInfoBuilder(ATTR_SIZE, Integer.class);
		attrSize.setRequired(false);
		attrSize.setMultiValued(false);
		attrSize.setCreateable(false);
		attrSize.setReadable(true);
		attrSize.setUpdateable(false);
		builder.addAttributeInfo(attrSize.build());

		AttributeInfoBuilder attrPCTotalCount = new AttributeInfoBuilder(ATTR_PC_TOTAL_COUNT);
		attrPCTotalCount.setRequired(false);
		attrPCTotalCount.setMultiValued(false);
		attrPCTotalCount.setCreateable(false);
		attrPCTotalCount.setReadable(true);
		attrPCTotalCount.setUpdateable(false);
		builder.addAttributeInfo(attrPCTotalCount.build());

		AttributeInfoBuilder attrPCEntries = new AttributeInfoBuilder(ATTR_PC_ENTRIES);
		attrPCEntries.setRequired(false);
		attrPCEntries.setMultiValued(true);
		attrPCEntries.setCreateable(false);
		attrPCEntries.setReadable(true);
		attrPCEntries.setUpdateable(false);
		builder.addAttributeInfo(attrPCEntries.build());

		AttributeInfoBuilder attrCBType = new AttributeInfoBuilder(ATTR_CB_TYPE);
		attrCBType.setRequired(false);
		attrCBType.setMultiValued(false);
		attrCBType.setCreateable(false);
		attrCBType.setReadable(true);
		attrCBType.setUpdateable(false);
		builder.addAttributeInfo(attrCBType.build());

		AttributeInfoBuilder attrCBId = new AttributeInfoBuilder(ATTR_CB_ID);
		attrCBId.setRequired(false);
		attrCBId.setMultiValued(false);
		attrCBId.setCreateable(false);
		attrCBId.setReadable(true);
		attrCBId.setUpdateable(false);
		builder.addAttributeInfo(attrCBId.build());

		AttributeInfoBuilder attrCBName = new AttributeInfoBuilder(ATTR_CB_NAME);
		attrCBName.setRequired(false);
		attrCBName.setMultiValued(false);
		attrCBName.setCreateable(false);
		attrCBName.setReadable(true);
		attrCBName.setUpdateable(false);
		builder.addAttributeInfo(attrCBName.build());

		AttributeInfoBuilder attrCBLogin = new AttributeInfoBuilder(ATTR_CB_LOGIN);
		attrCBLogin.setRequired(false);
		attrCBLogin.setMultiValued(false);
		attrCBLogin.setCreateable(false);
		attrCBLogin.setReadable(true);
		attrCBLogin.setUpdateable(false);
		builder.addAttributeInfo(attrCBLogin.build());

		AttributeInfoBuilder attrMBType = new AttributeInfoBuilder(ATTR_MB_TYPE);
		attrMBType.setRequired(false);
		attrMBType.setMultiValued(false);
		attrMBType.setCreateable(false);
		attrMBType.setReadable(true);
		attrMBType.setUpdateable(false);
		builder.addAttributeInfo(attrMBType.build());

		AttributeInfoBuilder attrMBId = new AttributeInfoBuilder(ATTR_MB_ID);
		attrMBId.setRequired(false);
		attrMBId.setMultiValued(false);
		attrMBId.setCreateable(false);
		attrMBId.setReadable(true);
		attrMBId.setUpdateable(false);
		builder.addAttributeInfo(attrMBId.build());

		AttributeInfoBuilder attrMBLogin = new AttributeInfoBuilder(ATTR_MB_LOGIN);
		attrMBLogin.setRequired(false);
		attrMBLogin.setMultiValued(false);
		attrMBLogin.setCreateable(false);
		attrMBLogin.setReadable(true);
		attrMBLogin.setUpdateable(false);
		builder.addAttributeInfo(attrMBLogin.build());

		AttributeInfoBuilder attrMBName = new AttributeInfoBuilder(ATTR_MB_NAME);
		attrMBName.setRequired(false);
		attrMBName.setMultiValued(false);
		attrMBName.setCreateable(false);
		attrMBName.setReadable(true);
		attrMBName.setUpdateable(false);
		builder.addAttributeInfo(attrMBName.build());

		AttributeInfoBuilder attrOBType = new AttributeInfoBuilder(ATTR_OB_TYPE);
		attrOBType.setRequired(false);
		attrOBType.setMultiValued(false);
		attrOBType.setCreateable(false);
		attrOBType.setReadable(true);
		attrOBType.setUpdateable(false);
		builder.addAttributeInfo(attrOBType.build());

		AttributeInfoBuilder attrOBId = new AttributeInfoBuilder(ATTR_OB_ID);
		attrOBId.setRequired(false);
		attrOBId.setMultiValued(false);
		attrOBId.setCreateable(false);
		attrOBId.setReadable(true);
		attrOBId.setUpdateable(false);
		builder.addAttributeInfo(attrOBId.build());

		AttributeInfoBuilder attrOBLogin = new AttributeInfoBuilder(ATTR_OB_LOGIN);
		attrOBLogin.setRequired(false);
		attrOBLogin.setMultiValued(false);
		attrOBLogin.setCreateable(false);
		attrOBLogin.setReadable(true);
		attrOBLogin.setUpdateable(false);
		builder.addAttributeInfo(attrOBLogin.build());

		AttributeInfoBuilder attrOBName = new AttributeInfoBuilder(ATTR_OB_NAME);
		attrOBName.setRequired(false);
		attrOBName.setMultiValued(false);
		attrOBName.setCreateable(false);
		attrOBName.setReadable(true);
		attrOBName.setUpdateable(false);
		builder.addAttributeInfo(attrOBName.build());

		AttributeInfoBuilder attrSLUrl = new AttributeInfoBuilder(ATTR_SL_URL);
		attrSLUrl.setRequired(false);
		attrSLUrl.setMultiValued(false);
		attrSLUrl.setCreateable(false);
		attrSLUrl.setReadable(true);
		attrSLUrl.setUpdateable(false);
		builder.addAttributeInfo(attrSLUrl.build());

		AttributeInfoBuilder attrSLDownUrl = new AttributeInfoBuilder(ATTR_SL_DOWN_URL);
		attrSLDownUrl.setRequired(false);
		attrSLDownUrl.setMultiValued(false);
		attrSLDownUrl.setCreateable(false);
		attrSLDownUrl.setReadable(true);
		attrSLDownUrl.setUpdateable(false);
		builder.addAttributeInfo(attrSLDownUrl.build());

		AttributeInfoBuilder attrSLVanityUrl = new AttributeInfoBuilder(ATTR_SL_VANITY_URL);
		attrSLVanityUrl.setRequired(false);
		attrSLVanityUrl.setMultiValued(false);
		attrSLVanityUrl.setCreateable(false);
		attrSLVanityUrl.setReadable(true);
		attrSLVanityUrl.setUpdateable(false);
		builder.addAttributeInfo(attrSLVanityUrl.build());

		AttributeInfoBuilder attrSLPsswdEnabl = new AttributeInfoBuilder(ATTR_SL_PSSW_ENABLED, Boolean.class);
		attrSLPsswdEnabl.setRequired(false);
		attrSLPsswdEnabl.setMultiValued(false);
		attrSLPsswdEnabl.setCreateable(false);
		attrSLPsswdEnabl.setReadable(true);
		attrSLPsswdEnabl.setUpdateable(false);
		builder.addAttributeInfo(attrSLPsswdEnabl.build());

		AttributeInfoBuilder attrSLDownCount = new AttributeInfoBuilder(ATTR_SL_DOWN_COUNT);
		attrSLDownCount.setRequired(false);
		attrSLDownCount.setMultiValued(false);
		attrSLDownCount.setCreateable(false);
		attrSLDownCount.setReadable(true);
		attrSLDownCount.setUpdateable(false);
		builder.addAttributeInfo(attrSLDownCount.build());

		AttributeInfoBuilder attrSLPrevCount = new AttributeInfoBuilder(ATTR_SL_PREV_COUNT);
		attrSLPrevCount.setRequired(false);
		attrSLPrevCount.setMultiValued(false);
		attrSLPrevCount.setCreateable(false);
		attrSLPrevCount.setReadable(true);
		attrSLPrevCount.setUpdateable(false);
		builder.addAttributeInfo(attrSLPrevCount.build());

		AttributeInfoBuilder attrFUEAccess = new AttributeInfoBuilder(ATTR_FUE_ACCESS);
		attrFUEAccess.setRequired(false);
		attrFUEAccess.setMultiValued(false);
		attrFUEAccess.setCreateable(false);
		attrFUEAccess.setReadable(true);
		attrFUEAccess.setUpdateable(false);
		builder.addAttributeInfo(attrFUEAccess.build());

		AttributeInfoBuilder attrFUEEmail = new AttributeInfoBuilder(ATTR_FUE_EMAIL);
		attrFUEEmail.setRequired(false);
		attrFUEEmail.setMultiValued(false);
		attrFUEEmail.setCreateable(false);
		attrFUEEmail.setReadable(true);
		attrFUEEmail.setUpdateable(false);
		builder.addAttributeInfo(attrFUEEmail.build());

		AttributeInfoBuilder attrParentSeqId = new AttributeInfoBuilder(ATTR_PARENT_SEQUENCE_ID);
		attrParentSeqId.setRequired(false);
		attrParentSeqId.setMultiValued(false);
		attrParentSeqId.setCreateable(false);
		attrParentSeqId.setReadable(true);
		attrParentSeqId.setUpdateable(false);
		builder.addAttributeInfo(attrParentSeqId.build());

		AttributeInfoBuilder attrParentEtag = new AttributeInfoBuilder(ATTR_PARENT_ETAG);
		attrParentEtag.setRequired(false);
		attrParentEtag.setMultiValued(false);
		attrParentEtag.setCreateable(false);
		attrParentEtag.setReadable(true);
		attrParentEtag.setUpdateable(false);
		builder.addAttributeInfo(attrParentEtag.build());

		AttributeInfoBuilder attrItemStatus = new AttributeInfoBuilder(ATTR_ITEM_STATUS);
		attrItemStatus.setRequired(false);
		attrItemStatus.setMultiValued(false);
		attrItemStatus.setCreateable(false);
		attrItemStatus.setReadable(true);
		attrItemStatus.setUpdateable(false);
		builder.addAttributeInfo(attrItemStatus.build());

		AttributeInfoBuilder attrItemCollectionEntries = new AttributeInfoBuilder(ATTR_ITEM_COLLECTION_ENTRIES);
		attrItemCollectionEntries.setRequired(false);
		attrItemCollectionEntries.setMultiValued(true);
		attrItemCollectionEntries.setCreateable(false);
		attrItemCollectionEntries.setReadable(true);
		attrItemCollectionEntries.setUpdateable(false);
		builder.addAttributeInfo(attrItemCollectionEntries.build());

		ObjectClassInfo foldersSchemaInfo = builder.build();
		LOGGER.info("The constructed folders schema representation: {0}", foldersSchemaInfo);
		return foldersSchemaInfo;

	}

	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {

		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();

		if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {

			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());
				try {
					uri = uriBuilder.build();

				} catch (URISyntaxException e) {
					StringBuilder sb = new StringBuilder();
					sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
							.append(";").append(e.getLocalizedMessage());
					throw new ConnectorException(sb.toString(), e);
				}
				HttpGet request = new HttpGet(uri);
				JSONObject folder = callRequest(request, true);
				ConnectorObject connectorObject = convertToConnectorObject(folder);
				handler.handle(connectorObject);
			}

		} else if (query == null && objectClass.is(FOLDER_NAME)) {
			handlerJson(handler, new Uid("0"));

		} else if (configuration.isEnableFilteredResultsHandler()) {
			handlerJson(handler, new Uid("0"));

		} else {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";")
					.append("Please note that its recommended to enable filteredResultsHandler");
			throw new ConnectorException(sb.toString());

		}

	}

	private void handlerJson(ResultsHandler handler, Uid uid) {

		if (uid == null) {
			throw new InvalidAttributeValueException("uid not provided");
		}

		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());

		try {
			uri = uriBuilder.build();

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		HttpGet request = new HttpGet(uri);
		JSONObject result = callRequest(request, true);
		if (result.has("item_collection")) {
			JSONObject collection = result.getJSONObject("item_collection");
			JSONArray array = collection.getJSONArray("entries");
			for (int i = 0; i < array.length(); i++) {
				JSONObject folder = array.getJSONObject(i);
				handlerJson(handler, new Uid(String.valueOf(folder.get("id"))));
				ConnectorObject connectorObject = convertToConnectorObject(folder);
				handler.handle(connectorObject);

			}
		} else {

			ConnectorObject connectorObject = convertToConnectorObject(result);
			handler.handle(connectorObject);

		}

	}

	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {

		if (uid == null) {
			throw new InvalidAttributeValueException("uid not provided");
		}

		HttpDelete request;
		URI folderUri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD_FOLDER + "/" + uid.getUidValue().toString());

		try {
			folderUri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		request = new HttpDelete(folderUri);
		callRequest(request, false);
	}

	public Uid createOrUpdateFolder(Uid fid, Set<Attribute> attributes) {

		if (attributes == null || attributes.isEmpty()) {
			throw new InvalidAttributeValueException("attributes not provided or empty");
		}

		boolean create = fid == null;
		JSONObject json = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		JSONObject jsonSharedLink = new JSONObject();
		JSONObject jsonFolderUpload = new JSONObject();
		JSONObject jsonOwnedBy = new JSONObject();
		JSONObject sharedPermission = new JSONObject();
		URIBuilder uriBuilder = getURIBuilder();
		String parentId = null;

		String name = getStringAttr(attributes, "__NAME__");
		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}

		if (name != null) {
			json.put(ATTR_NAME, name);
		}

		if (create && StringUtil.isBlank(parentId = getAttr(attributes, ATTR_PARENT_ID, String.class, null))) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_PARENT_ID);
		} else
			jsonParent.put("id", parentId);
		json.put(ATTR_PARENT, jsonParent);

		putChildFieldIfExists(attributes, "access", ATTR_SL_ACCESS, String.class, jsonSharedLink);
		putChildFieldIfExists(attributes, "password", ATTR_SL_PASSWORD, String.class, jsonSharedLink);
		putChildFieldIfExists(attributes, "unshared_at", ATTR_SL_UNSHARED, Integer.class, jsonSharedLink);
		putChildFieldIfExists(attributes, "can_download", ATTR_SL_PER_DOWNLOAD, Boolean.class, sharedPermission);
		putChildFieldIfExists(attributes, "access", ATTR_FUE_ACCESS, String.class, jsonFolderUpload);
		putChildFieldIfExists(attributes, "id", ATTR_OB_ID, String.class, jsonOwnedBy);

		if (sharedPermission != null && (sharedPermission.length()) > 0) {
			jsonSharedLink.put("permissions", sharedPermission);
		}
		if (jsonSharedLink != null && (jsonSharedLink.length()) > 0) {
			json.put("shared_link", jsonSharedLink);
		}
		if (jsonFolderUpload != null && (jsonFolderUpload.length()) > 0) {
			json.put("folder_upload_email", jsonFolderUpload);
		}
		if (jsonOwnedBy != null && (jsonOwnedBy.length()) > 0) {
			json.put("owned_by", jsonOwnedBy);
		}
		putFieldIfExists(attributes, ATTR_FOLDER_DESC, String.class, json);
		putFieldIfExists(attributes, ATTR_SYNC_STATE, String.class, json);
		putFieldIfExists(attributes, ATTR_TAGS, String.class, json);
		putFieldIfExists(attributes, ATTR_NONOWNERS_INVITE, Boolean.class, json);

		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		if (create) {
			uriBuilder.setPath(CRUD_FOLDER);
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
			uriBuilder.setPath(CRUD_FOLDER + "/" + fid.getUidValue().toString());
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

		String newFid = jsonReq.getString(UID);

		return new Uid(newFid);

	}

	public ConnectorObject convertToConnectorObject(JSONObject json) {

		if (json == null) {
			throw new InvalidAttributeValueException("FolderObject not provided");
		}

		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		JSONObject jsonParentName = new JSONObject();
		JSONObject jsonPath = new JSONObject();
		JSONObject jsonCreatedBy = new JSONObject();
		JSONObject jsonModifiedBy = new JSONObject();
		JSONObject jsonOwnedBy = new JSONObject();
		JSONObject jsonSharedLink = new JSONObject();
		JSONObject jsonFUE = new JSONObject();
		JSONObject jsonItemCollection = new JSONObject();
		JSONArray jsonPathEntries = new JSONArray();
		JSONArray jsonCollectionEntries = new JSONArray();
		JSONObject jsonSharedLinkPermissions = new JSONObject();

		builder.setUid(new Uid(json.getString(UID)));
		builder.setObjectClass(new ObjectClass(FOLDER_NAME));
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}

		if (json.has(ATTR_PARENT)) {
			if (json.get(ATTR_PARENT) != null && !JSONObject.NULL.equals(json.get(ATTR_PARENT))) {
				jsonParentName = (JSONObject) json.get(ATTR_PARENT);
			}

		}
		if (json.has(PATH)) {
			if (json.get(PATH) != null && !JSONObject.NULL.equals(json.get(PATH))) {
				jsonPath = (JSONObject) json.get(PATH);
			}
			if (jsonPath.has(ENTRIES)) {
				if (jsonPath.get(ENTRIES) != null && !JSONObject.NULL.equals(jsonPath.get(ENTRIES))) {
					jsonPathEntries = (JSONArray) jsonPath.get(ENTRIES);
					builder.addAttribute(ATTR_PC_ENTRIES, jsonPathEntries.toString());
				}

			}

		}
		if (json.has(CREATED)) {
			if (json.get(CREATED) != null && !JSONObject.NULL.equals(json.get(CREATED))) {
				jsonCreatedBy = (JSONObject) json.get(CREATED);
			}

		}
		if (json.has(MODIFIED)) {
			if (json.get(MODIFIED) != null && !JSONObject.NULL.equals(json.get(MODIFIED))) {
				jsonModifiedBy = (JSONObject) json.get(MODIFIED);
			}

		}
		if (json.has(OWNED)) {
			if (json.get(OWNED) != null && !JSONObject.NULL.equals(json.get(OWNED))) {
				jsonOwnedBy = (JSONObject) json.get(OWNED);
			}

		}
		if (json.has(FUE)) {
			if (json.get(FUE) != null && !JSONObject.NULL.equals(json.get(FUE))) {
				jsonFUE = (JSONObject) json.get(FUE);
			}

		}
		if (json.has(SHARED)) {
			if (json.get(SHARED) != null && !JSONObject.NULL.equals(json.get(SHARED))) {
				jsonSharedLink = (JSONObject) json.get(SHARED);
			}
			if (jsonSharedLink.has(PERMISSIONS)) {
				if (jsonSharedLink.get(PERMISSIONS) != null
						&& !JSONObject.NULL.equals(jsonSharedLink.get(PERMISSIONS))) {
					jsonSharedLinkPermissions = (JSONObject) jsonSharedLink.get(PERMISSIONS);

				}

			}

		}
		if (json.has(COLLECTION)) {
			if (json.get(COLLECTION) != null && !JSONObject.NULL.equals(json.get(COLLECTION))) {
				jsonItemCollection = (JSONObject) json.get(COLLECTION);
			}
			if (jsonItemCollection.has(ENTRIES)) {
				if (jsonItemCollection.get(ENTRIES) != null
						&& !JSONObject.NULL.equals(jsonItemCollection.get(ENTRIES))) {
					jsonCollectionEntries = (JSONArray) jsonItemCollection.get(ENTRIES);
					builder.addAttribute(ATTR_ITEM_COLLECTION_ENTRIES, jsonCollectionEntries.toString());
				}

			}

		}

		getIfExists(json, ATTR_FOLDER_DESC, builder);
		getIfExists(json, ATTR_SEQUENCE_ID, builder);
		getIfExists(json, ATTR_ETAG, builder);
		getIfExists(json, ATTR_CREATED, builder);
		getIfExists(json, ATTR_MODIFIED, builder);
		getIfExists(json, ATTR_SIZE, builder);
		getIfExists(json, ATTR_ITEM_STATUS, builder);
		getIfExists(jsonPath, "total_count", ATTR_PC_TOTAL_COUNT, builder);
		getIfExists(jsonCreatedBy, TYPE, ATTR_CB_TYPE, builder);
		getIfExists(jsonCreatedBy, UID, ATTR_CB_ID, builder);
		getIfExists(jsonCreatedBy, ATTR_NAME, ATTR_CB_LOGIN, builder);
		getIfExists(jsonCreatedBy, LOGIN, ATTR_CB_NAME, builder);
		getIfExists(jsonModifiedBy, TYPE, ATTR_MB_TYPE, builder);
		getIfExists(jsonModifiedBy, UID, ATTR_MB_ID, builder);
		getIfExists(jsonModifiedBy, LOGIN, ATTR_MB_LOGIN, builder);
		getIfExists(jsonModifiedBy, ATTR_NAME, ATTR_MB_NAME, builder);
		getIfExists(jsonOwnedBy, TYPE, ATTR_OB_TYPE, builder);
		getIfExists(jsonOwnedBy, LOGIN, ATTR_OB_LOGIN, builder);
		getIfExists(jsonOwnedBy, UID, ATTR_OB_ID, builder);
		getIfExists(jsonOwnedBy, ATTR_NAME, ATTR_OB_NAME, builder);
		getIfExists(jsonFUE, "access", ATTR_FUE_ACCESS, builder);
		getIfExists(jsonFUE, "email", ATTR_FUE_EMAIL, builder);
		getIfExists(jsonSharedLink, "url", ATTR_SL_URL, builder);
		getIfExists(jsonSharedLink, "download_url", ATTR_SL_DOWN_URL, builder);
		getIfExists(jsonSharedLink, "vanity_url", ATTR_SL_VANITY_URL, builder);
		getIfExists(jsonSharedLink, "is_password_enabled", ATTR_SL_PSSW_ENABLED, builder);
		getIfExists(jsonSharedLink, "unshared_at", ATTR_SL_UNSHARED, builder);
		getIfExists(jsonSharedLink, "download_count", ATTR_SL_DOWN_COUNT, builder);
		getIfExists(jsonSharedLink, "preview_count", ATTR_SL_PREV_COUNT, builder);
		getIfExists(jsonSharedLink, "access", ATTR_SL_ACCESS, builder);
		getIfExists(jsonSharedLinkPermissions, "can_download", ATTR_SL_PER_DOWNLOAD, builder);
		getIfExists(jsonSharedLinkPermissions, "can_preview", ATTR_SL_PER_PREVIEW, builder);
		getIfExists(jsonParentName, UID, ATTR_PARENT_ID, builder);
		getIfExists(jsonParentName, "sequence_id", ATTR_SEQUENCE_ID, builder);
		getIfExists(jsonParentName, "etag", ATTR_ETAG, builder);
		getIfExists(jsonParentName, ATTR_NAME, ATTR_PARENT_NAME, builder);

		ConnectorObject connectorObject = builder.build();
		LOGGER.ok("convertUserToConnectorObject,\n\tconnectorObject: {0}", connectorObject);
		return connectorObject;

	}

}
