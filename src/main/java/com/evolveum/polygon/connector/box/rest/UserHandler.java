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
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evolveum.polygon.connector.box.rest.BoxConnectorConfiguration;

public class UserHandler extends ObjectsProcessing {
	private static final Log LOGGER = Log.getLog(UserHandler.class);

	public UserHandler(BoxConnectorConfiguration conf) {
		super(conf);
	}

	private static final String ATTR_LOGIN = "login";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_ROLE = "role";
	private static final String ATTR_ID = "userID";
	private static final String ATTR_LANGUAGE = "language";
	private static final String ATTR_SYNC = "is_sync_enabled";
	private static final String ATTR_TITLE = "job_title";
	private static final String ATTR_PHONE = "phone";
	private static final String ATTR_ADDRESS = "address";
	private static final String ATTR_SPACE = "space_amount";
	private static final String ATTR_MANAGED = "can_see_managed_users";
	private static final String ATTR_TIMEZONE = "timezone";
	private static final String ATTR_DEVICELIMITS = "is_exempt_from_device_limits";
	private static final String ATTR_LOGINVERIFICATION = "is_exempt_from_login_verification";
	private static final String ATTR_COLLAB = "is_external_collab_restricted";
	private static final String ATTR_STATUS = "status";
	private static final String ATTR_AVATAR = "avatar_url";
	private static final String ATTR_ENTERPRISE = "enterprise";
	private static final String ATTR_NOTIFY = "notify";
	private static final String ATTR_CREATED = "created_at";
	private static final String ATTR_MODIFIED = "modified_at";
	private static final String ATTR_USED = "space_used";
	private static final String ATTR_PSSWD = "is_password_reset_required";
	private static final String ATTR_CODE = "tracking_codes";
	private static final String ATTR_MEMBERSHIPS = "group_membership";

	private static final String FILTERTERM = "filter_term";
	private static final String OFFSET = "offset";
	private static final String LIMIT = "limit";
	private static final String CRUD = "/2.0/users";
	private static final String UID = "id";
	private static final String AVATAR = "avatar_url";
	private static final String MEMBERSHIPS = "memberships";

	public ObjectClassInfo getUserSchema() {

		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		// mail
		AttributeInfoBuilder attrLoginBuilder = new AttributeInfoBuilder(ATTR_LOGIN);
		attrLoginBuilder.setRequired(true);
		attrLoginBuilder.setMultiValued(false);
		attrLoginBuilder.setCreateable(true);
		attrLoginBuilder.setReadable(true);
		attrLoginBuilder.setUpdateable(false);
		ocBuilder.addAttributeInfo(attrLoginBuilder.build());
		// role
		AttributeInfoBuilder attrRoleBuilder = new AttributeInfoBuilder(ATTR_ROLE);
		attrRoleBuilder.setRequired(false);
		attrRoleBuilder.setMultiValued(false);
		attrRoleBuilder.setCreateable(true);
		attrRoleBuilder.setReadable(false);
		attrRoleBuilder.setUpdateable(true);
		attrRoleBuilder.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrRoleBuilder.build());
		// explicit_ID
		AttributeInfoBuilder attrUserId = new AttributeInfoBuilder(ATTR_ID);
		attrUserId.setRequired(false);
		attrUserId.setMultiValued(false);
		attrUserId.setCreateable(false);
		attrUserId.setReadable(true);
		attrUserId.setUpdateable(false);
		attrUserId.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrUserId.build());
		// language
		AttributeInfoBuilder attrLanguageBuilder = new AttributeInfoBuilder(ATTR_LANGUAGE);
		attrLanguageBuilder.setRequired(false);
		attrLanguageBuilder.setMultiValued(false);
		attrLanguageBuilder.setCreateable(true);
		attrLanguageBuilder.setReadable(true);
		attrLanguageBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrLanguageBuilder.build());
		// is_sync_enabled
		AttributeInfoBuilder attrIsSyncEnabledBuilder = new AttributeInfoBuilder(ATTR_SYNC, Boolean.class);
		attrIsSyncEnabledBuilder.setRequired(false);
		attrIsSyncEnabledBuilder.setMultiValued(false);
		attrIsSyncEnabledBuilder.setCreateable(true);
		attrIsSyncEnabledBuilder.setReadable(false);
		attrIsSyncEnabledBuilder.setUpdateable(true);
		attrIsSyncEnabledBuilder.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrIsSyncEnabledBuilder.build());
		// job_titile
		AttributeInfoBuilder attrJobTitleBuilder = new AttributeInfoBuilder(ATTR_TITLE);
		attrJobTitleBuilder.setRequired(false);
		attrJobTitleBuilder.setMultiValued(false);
		attrJobTitleBuilder.setCreateable(true);
		attrJobTitleBuilder.setReadable(true);
		attrJobTitleBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrJobTitleBuilder.build());
		// phone
		AttributeInfoBuilder attrPhoneBuilder = new AttributeInfoBuilder(ATTR_PHONE);
		attrPhoneBuilder.setRequired(false);
		attrPhoneBuilder.setMultiValued(false);
		attrPhoneBuilder.setCreateable(true);
		attrPhoneBuilder.setReadable(true);
		attrPhoneBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrPhoneBuilder.build());
		// address
		AttributeInfoBuilder attrAddressBuilder = new AttributeInfoBuilder(ATTR_ADDRESS);
		attrAddressBuilder.setRequired(false);
		attrAddressBuilder.setMultiValued(false);
		attrAddressBuilder.setCreateable(true);
		attrAddressBuilder.setReadable(true);
		attrAddressBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrAddressBuilder.build());
		// space_amount
		AttributeInfoBuilder attrSpaceAmountBuilder = new AttributeInfoBuilder(ATTR_SPACE, Integer.class);
		attrSpaceAmountBuilder.setRequired(false);
		attrSpaceAmountBuilder.setMultiValued(false);
		attrSpaceAmountBuilder.setCreateable(true);
		attrSpaceAmountBuilder.setReadable(true);
		attrSpaceAmountBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrSpaceAmountBuilder.build());
		// tracking_codes
		AttributeInfoBuilder attrTrackingCodeBuilder = new AttributeInfoBuilder(ATTR_CODE);
		attrTrackingCodeBuilder.setRequired(false);
		attrTrackingCodeBuilder.setMultiValued(true);
		attrTrackingCodeBuilder.setCreateable(true);
		attrTrackingCodeBuilder.setReadable(false);
		attrTrackingCodeBuilder.setUpdateable(true);
		attrTrackingCodeBuilder.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrTrackingCodeBuilder.build());
		// can_see_managed_users
		AttributeInfoBuilder attrCanSeeManagedUsersBuilder = new AttributeInfoBuilder(ATTR_MANAGED, Boolean.class);
		attrCanSeeManagedUsersBuilder.setRequired(false);
		attrCanSeeManagedUsersBuilder.setMultiValued(false);
		attrCanSeeManagedUsersBuilder.setCreateable(true);
		attrCanSeeManagedUsersBuilder.setReadable(false);
		attrCanSeeManagedUsersBuilder.setUpdateable(true);
		attrCanSeeManagedUsersBuilder.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrCanSeeManagedUsersBuilder.build());
		// timezone
		AttributeInfoBuilder attrTimezoneBuilder = new AttributeInfoBuilder(ATTR_TIMEZONE);
		attrTimezoneBuilder.setRequired(false);
		attrTimezoneBuilder.setMultiValued(false);
		attrTimezoneBuilder.setCreateable(true);
		attrTimezoneBuilder.setReadable(true);
		attrTimezoneBuilder.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrTimezoneBuilder.build());
		// is_exempt_from_device_limits
		AttributeInfoBuilder attrIsExemptFromDeviceLimits = new AttributeInfoBuilder(ATTR_DEVICELIMITS, Boolean.class);
		attrIsExemptFromDeviceLimits.setRequired(false);
		attrIsExemptFromDeviceLimits.setMultiValued(false);
		attrIsExemptFromDeviceLimits.setCreateable(true);
		attrIsExemptFromDeviceLimits.setReadable(false);
		attrIsExemptFromDeviceLimits.setUpdateable(true);
		attrIsExemptFromDeviceLimits.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrIsExemptFromDeviceLimits.build());
		// is_exempt_from_login_verification
		AttributeInfoBuilder attrIsExemptFromLoginVerification = new AttributeInfoBuilder(ATTR_LOGINVERIFICATION,
				Boolean.class);
		attrIsExemptFromLoginVerification.setRequired(false);
		attrIsExemptFromLoginVerification.setMultiValued(false);
		attrIsExemptFromLoginVerification.setCreateable(true);
		attrIsExemptFromLoginVerification.setReadable(false);
		attrIsExemptFromLoginVerification.setUpdateable(true);
		attrIsExemptFromLoginVerification.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrIsExemptFromLoginVerification.build());
		// avatar
		AttributeInfoBuilder attrAvatar = new AttributeInfoBuilder(ATTR_AVATAR, byte[].class);
		attrAvatar.setRequired(false);
		attrAvatar.setMultiValued(false);
		attrAvatar.setCreateable(false);
		attrAvatar.setReadable(true);
		attrAvatar.setUpdateable(false);
		ocBuilder.addAttributeInfo(attrAvatar.build());
		// is_external_collab_restricted
		AttributeInfoBuilder attrCollab = new AttributeInfoBuilder(ATTR_COLLAB, Boolean.class);
		attrCollab.setRequired(false);
		attrCollab.setMultiValued(false);
		attrCollab.setCreateable(true);
		attrCollab.setReadable(false);
		attrCollab.setUpdateable(true);
		attrCollab.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrCollab.build());
		// enterprise
		AttributeInfoBuilder attrEnterpise = new AttributeInfoBuilder(ATTR_ENTERPRISE);
		attrEnterpise.setRequired(false);
		attrEnterpise.setMultiValued(false);
		attrEnterpise.setCreateable(false);
		attrEnterpise.setReadable(false);
		attrEnterpise.setUpdateable(true);
		attrEnterpise.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrEnterpise.build());
		// notify
		AttributeInfoBuilder attrNotify = new AttributeInfoBuilder(ATTR_NOTIFY, Boolean.class);
		attrNotify.setRequired(false);
		attrNotify.setMultiValued(false);
		attrNotify.setCreateable(false);
		attrNotify.setReadable(false);
		attrNotify.setUpdateable(true);
		attrNotify.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrNotify.build());

		AttributeInfoBuilder attrCreated = new AttributeInfoBuilder(ATTR_CREATED);
		attrCreated.setRequired(false);
		attrCreated.setMultiValued(false);
		attrCreated.setCreateable(false);
		attrCreated.setReadable(true);
		attrCreated.setUpdateable(false);
		ocBuilder.addAttributeInfo(attrCreated.build());

		AttributeInfoBuilder attrModified = new AttributeInfoBuilder(ATTR_MODIFIED);
		attrModified.setRequired(false);
		attrModified.setMultiValued(false);
		attrModified.setCreateable(false);
		attrModified.setReadable(true);
		attrModified.setUpdateable(false);
		ocBuilder.addAttributeInfo(attrModified.build());

		AttributeInfoBuilder attrUsed = new AttributeInfoBuilder(ATTR_USED, Integer.class);
		attrUsed.setRequired(false);
		attrUsed.setMultiValued(false);
		attrUsed.setCreateable(false);
		attrUsed.setReadable(true);
		attrUsed.setUpdateable(false);
		ocBuilder.addAttributeInfo(attrUsed.build());

		AttributeInfoBuilder attrPsswd = new AttributeInfoBuilder(ATTR_PSSWD, Boolean.class);
		attrPsswd.setRequired(false);
		attrPsswd.setMultiValued(false);
		attrPsswd.setCreateable(false);
		attrPsswd.setReadable(false);
		attrPsswd.setUpdateable(true);
		attrPsswd.setReturnedByDefault(false);
		ocBuilder.addAttributeInfo(attrPsswd.build());

		AttributeInfoBuilder attrMembership = new AttributeInfoBuilder(ATTR_MEMBERSHIPS);
		attrMembership.setMultiValued(true);
		attrMembership.setRequired(false);
		attrMembership.setCreateable(true);
		attrMembership.setReadable(true);
		attrMembership.setUpdateable(true);
		ocBuilder.addAttributeInfo(attrMembership.build());

		ObjectClassInfo userSchemaInfo = ocBuilder.build();
		LOGGER.info("The constructed User core schema: {0}", userSchemaInfo);
		return userSchemaInfo;
	}

	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {

		Name name = null;
		Uid uid = null;
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		int pageNumber = 0;
		int usersPerPage = 0;
		int offset = 0;

		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			name = (Name) ((StartsWithFilter) query).getAttribute();

			if (name != null) {
				uriBuilder.setPath(CRUD);
				uriBuilder.addParameter(FILTERTERM, name.getNameValue());
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

				handleUsers(request, handler, query, options);

			}

		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			uid = (Uid) ((EqualsFilter) query).getAttribute();
			if (uid != null) {
				uriBuilder.setPath(CRUD + "/" + uid.getUidValue().toString());

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
				ConnectorObject connectorObject = convertToConnectorObject(user, query);
				handler.handle(connectorObject);

			}

		} else if (query == null && objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			uriBuilder.setPath(CRUD);
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
			handleUsers(request, handler, query, options);

		} else if (configuration.isEnableFilteredResultsHandler() == true) {

			uriBuilder.setPath(CRUD);

			try {
				uri = uriBuilder.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
						.append(";").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}
			HttpGet request = new HttpGet(uri);
			handleUsers(request, handler, query, options);

		} else {

			StringBuilder sb = new StringBuilder();
			sb.append("Method not allowed: ").append((query).getClass().getSimpleName().toString()).append(";");
			throw new ConnectorException(sb.toString());

		}

	}

	private boolean handleUsers(HttpGet request, ResultsHandler handler, Filter filter, OperationOptions options) {
		boolean finish = false;
		if (request == null) {
			LOGGER.error("Request value not provided {0} ", request);
			throw new InvalidAttributeValueException("Request value not provided");
		}

		JSONObject result = callRequest(request);
		JSONArray users = result.getJSONArray("entries");

		for (int i = 0; i < users.length(); i++) {

			JSONObject user = users.getJSONObject(i);
			ConnectorObject connectorObject = convertToConnectorObject(user, filter);
			finish = !handler.handle(connectorObject);

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
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD + "/" + uid.getUidValue().toString());
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		request = new HttpDelete(uri);
		callRequest(request, false);
	}

	public Uid createOrUpdateUser(Uid uid, Set<Attribute> attributes) {
		if (attributes == null || attributes.isEmpty()) {
			throw new InvalidAttributeValueException("attributes not provided or empty");
		}

		boolean create = uid == null;
		JSONObject json = new JSONObject();
		URIBuilder uriBuilder = getURIBuilder();
		// required attribute e-mail
		String login = getStringAttr(attributes, ATTR_LOGIN);
		if (create && StringUtil.isBlank(login)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_LOGIN);
		}
		if (login != null) {
			json.put(ATTR_LOGIN, login);
		}
		// required attribute name
		String name = getStringAttr(attributes, "__NAME__");
		if (create && StringUtil.isBlank(name)) {
			throw new InvalidAttributeValueException("Missing mandatory attribute " + ATTR_NAME);
		}
		if (name != null) {
			json.put(ATTR_NAME, name);
		}
		if ((getAttr(attributes, OperationalAttributes.ENABLE_NAME, Boolean.class)) != null) {
			Boolean status = getAttr(attributes, OperationalAttributes.ENABLE_NAME, Boolean.class);
			if (status) {
				json.put(ATTR_STATUS, "active");
			} else {
				json.put(ATTR_STATUS, "inactive");
			}
		}

		putFieldIfExists(attributes, ATTR_ID, String.class, json);
		putFieldIfExists(attributes, ATTR_ROLE, String.class, json);
		putFieldIfExists(attributes, ATTR_LANGUAGE, String.class, json);
		putFieldIfExists(attributes, ATTR_SYNC, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_TITLE, String.class, json);
		putFieldIfExists(attributes, ATTR_PHONE, String.class, json);
		putFieldIfExists(attributes, ATTR_ADDRESS, String.class, json);
		putFieldIfExists(attributes, ATTR_SPACE, Integer.class, json);
		putFieldIfExists(attributes, ATTR_MANAGED, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_TIMEZONE, String.class, json);
		putFieldIfExists(attributes, ATTR_DEVICELIMITS, String.class, json);
		putFieldIfExists(attributes, ATTR_LOGINVERIFICATION, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_COLLAB, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_NOTIFY, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_ENTERPRISE, String.class, json);
		putFieldIfExists(attributes, ATTR_PSSWD, Boolean.class, json);
		putFieldIfExists(attributes, ATTR_CODE, String.class, json);
		HttpEntityEnclosingRequestBase request = null;
		URI uri = null;

		if (create) {
			uriBuilder.setPath(CRUD);
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
			// update
			uriBuilder.setPath(CRUD + "/" + uid.getUidValue().toString());
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
			throw new InvalidAttributeValueException("UserObject not provided");
		}
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setUid(new Uid(json.getString(UID)));
		if (json.has(ATTR_NAME)) {
			builder.setName(json.getString(ATTR_NAME));
		}

		if (json.has(ATTR_STATUS)) {
			if (json.get(ATTR_STATUS) != null && !JSONObject.NULL.equals(json.get(ATTR_STATUS))) {
				if (json.get(ATTR_STATUS).equals("active")) {
					addAttr(builder, OperationalAttributes.ENABLE_NAME, true);
				} else if (json.get(ATTR_STATUS).equals("inactive")) {
					addAttr(builder, OperationalAttributes.ENABLE_NAME, false);
				}
			}
		}

		getIfExists(json, ATTR_ID, builder);
		getIfExists(json, ATTR_LOGIN, builder);
		getIfExists(json, ATTR_ADDRESS, builder);
		getIfExists(json, ATTR_DEVICELIMITS, builder);
		getIfExists(json, ATTR_LANGUAGE, builder);
		getIfExists(json, ATTR_PHONE, builder);
		getIfExists(json, ATTR_ROLE, builder);
		getIfExists(json, ATTR_SPACE, builder);
		getIfExists(json, ATTR_TIMEZONE, builder);
		getIfExists(json, ATTR_TITLE, builder);
		getIfExists(json, ATTR_AVATAR, builder);
		getIfExists(json, ATTR_CREATED, builder);
		getIfExists(json, ATTR_MODIFIED, builder);
		getIfExists(json, ATTR_USED, builder);

		if (query instanceof EqualsFilter) {

			getAvatarPhoto(json, builder, AVATAR);
			String[] memberships = getUserMemberships(json.getString(UID));
			if (memberships.length > 0) {
				builder.addAttribute(ATTR_MEMBERSHIPS, getUserMemberships(json.getString(UID)));
			}

		}

		ConnectorObject connectorObject = builder.build();
		return connectorObject;
	}

	private String[] getUserMemberships(String uid) {
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();
		JSONObject group = new JSONObject();

		uriBuilder.setPath(CRUD + "/" + uid + "/" + MEMBERSHIPS);

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
		String[] membershipAttr = new String[members.length()];

		for (int i = 0; i < members.length(); i++) {

			JSONObject member = members.getJSONObject(i);

			if (member.has("group")) {

				if (member.get("group") != null && !JSONObject.NULL.equals(member.get("group"))) {

					group = (JSONObject) member.get("group");
				}

				StringBuilder memInfo = new StringBuilder();
				memInfo.append(String.valueOf(group.get("id"))).append("@").append(String.valueOf(member.get("role")))
						.append("@").append(String.valueOf(member.get("id")));

				membershipAttr[i] = memInfo.toString();

			}
		}
		return membershipAttr;
	}

}
