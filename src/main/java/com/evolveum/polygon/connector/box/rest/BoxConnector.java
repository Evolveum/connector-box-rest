/**
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.polygon.connector.box.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.common.CollectionUtil;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

import com.evolveum.polygon.connector.box.rest.BoxConnectorConfiguration;

/**
 * @author suchanovsky
 *
 */
@ConnectorClass(displayNameKey = "Box.connector.display", configurationClass = BoxConnectorConfiguration.class)

public class BoxConnector implements TestOp, SchemaOp, Connector, DeleteOp, SearchOp<Filter>, UpdateOp, CreateOp,
		UpdateAttributeValuesOp {

	public BoxConnector() {
	}

	private static final Log LOG = Log.getLog(BoxConnector.class);
	private BoxConnectorConfiguration configuration;

	private static final String ADMIN = "admin";
	private static final String FOLDER_NAME = "Folders";
	private static final String MEMBER = "member";
	private static final String CRUD = "/2.0/users";

	private Schema schema = null;
	private URIBuilder uri;

	public URIBuilder getURIBuilder() {
		return this.uri;
	}

	@Override
	public void test() {
		URI uri = null;
		URIBuilder uriBuilder = getURIBuilder();

		uriBuilder.setPath(CRUD);
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It is not possible to create URI from URIBuilder:").append(getURIBuilder().toString())
					.append(";").append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);

		}
		LOG.info("TEST METHOD URI VALUE: {0}", uri);
		HttpGet request = new HttpGet(uri);
		ObjectsProcessing objects = new ObjectsProcessing(configuration);

		try (CloseableHttpResponse response = objects.executeRequest(request)) {
			objects.processResponseErrors(response);
		} catch (IOException e) {
			throw new ConnectorIOException();
		}

	}

	@Override
	public Schema schema() {
		if (null == schema) {
			SchemaBuilder schemaBuilder = new SchemaBuilder(BoxConnector.class);

			UserHandler user = new UserHandler(configuration);
			ObjectClassInfo userSchemaInfo = user.getUserSchema();
			schemaBuilder.defineObjectClass(userSchemaInfo);

			GroupHandler group = new GroupHandler(configuration);
			ObjectClassInfo groupSchemaInfo = group.getGroupSchema();
			schemaBuilder.defineObjectClass(groupSchemaInfo);

			FoldersHandler folder = new FoldersHandler(configuration);
			ObjectClassInfo folderSchemaInfo = folder.getFoldersSchema();
			schemaBuilder.defineObjectClass(folderSchemaInfo);

			return schemaBuilder.build();
		}
		return this.schema;
	}

	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {
		if (uid.getUidValue() == null && uid.getUidValue().isEmpty()) {

			StringBuilder sb = new StringBuilder();
			sb.append("Uid not provided or empty:").append(uid.getUidValue()).append(";");
			throw new InvalidAttributeValueException(sb.toString());
		}
		LOG.info("UPDATE METHOD UID VALUE: {0}", uid.getUidValue());

		if (objectClass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("UPDATE METHOD OBJECTCLASS VALUE: {0}", objectClass);

		if (attributes == null || attributes.isEmpty()) {
			throw new InvalidAttributeValueException("Attributes not provided or empty");
		}

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			UserHandler user = new UserHandler(configuration);
			return user.createOrUpdateUser(uid, attributes);

		} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
			GroupHandler group = new GroupHandler(configuration);
			return group.createOrUpdateGroup(uid, attributes);

		} else if (objectClass.is(FOLDER_NAME)) {
			FoldersHandler folder = new FoldersHandler(configuration);
			return folder.createOrUpdateFolder(uid, attributes);

		} else
			throw new UnsupportedOperationException("Unsupported object class: " + objectClass);

	}

	@Override
	public FilterTranslator<Filter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
		return new FilterTranslator<Filter>() {

			@Override
			public List<Filter> translate(Filter filter) {
				return CollectionUtil.newList(filter);
			}

		};
	}

	@Override
	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
		if (objectClass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("EXECUTE_QUERY METHOD OBJECTCLASS VALUE: {0}", objectClass);

		// StartWithFilter
		if (query instanceof StartsWithFilter && ((StartsWithFilter) query).getAttribute() instanceof Name) {
			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				UserHandler user = new UserHandler(configuration);
				user.executeQuery(objectClass, query, handler, options);
			}

			// EqualsFilter
		} else if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				UserHandler user = new UserHandler(configuration);
				user.executeQuery(objectClass, query, handler, options);
			} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
				GroupHandler group = new GroupHandler(configuration);
				group.executeQuery(objectClass, query, handler, options);
			} else if (objectClass.is(FOLDER_NAME)) {
				FoldersHandler folder = new FoldersHandler(configuration);
				folder.executeQuery(objectClass, query, handler, options);
			}

		} else if (query instanceof Filter) {
			if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
				UserHandler user = new UserHandler(configuration);
				user.executeQuery(objectClass, query, handler, options);
			} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
				GroupHandler group = new GroupHandler(configuration);
				group.executeQuery(objectClass, query, handler, options);
			} else if (objectClass.is(FOLDER_NAME)) {
				FoldersHandler folder = new FoldersHandler(configuration);
				folder.executeQuery(objectClass, query, handler, options);
			}

		} else if (query == null && objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			UserHandler user = new UserHandler(configuration);
			user.executeQuery(objectClass, query, handler, options);
		} else if (query == null && objectClass.is(ObjectClass.GROUP_NAME)) {
			GroupHandler group = new GroupHandler(configuration);
			group.executeQuery(objectClass, query, handler, options);
		} else if (query == null && objectClass.is(FOLDER_NAME)) {
			FoldersHandler folder = new FoldersHandler(configuration);
			folder.executeQuery(objectClass, query, handler, options);
		}
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {
		if (uid.getUidValue() == null && uid.getUidValue().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Uid not provided or empty:").append(uid.getUidValue()).append(";");
			throw new InvalidAttributeValueException(sb.toString());
		}
		LOG.info("DELETE METHOD UID VALUE: {0}", uid.getUidValue());

		if (objectClass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("DELETE METHOD OBJECTCLASS VALUE: {0}", objectClass);

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			UserHandler user = new UserHandler(configuration);
			user.delete(objectClass, uid, operationOptions);
		} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
			GroupHandler group = new GroupHandler(configuration);
			group.delete(objectClass, uid, operationOptions);
		} else if (objectClass.is(FOLDER_NAME)) {
			FoldersHandler folder = new FoldersHandler(configuration);
			folder.delete(objectClass, uid, operationOptions);
		}
	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
		if (objectClass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("CREATE METHOD OBJECTCLASS VALUE: {0}", objectClass);

		if (attributes == null) {
			throw new InvalidAttributeValueException("Attributes not provided or empty");
		}

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			UserHandler user = new UserHandler(configuration);
			return user.createOrUpdateUser(null, attributes);

		} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
			GroupHandler group = new GroupHandler(configuration);
			return group.createOrUpdateGroup(null, attributes);

		} else if (objectClass.is(FOLDER_NAME)) {
			FoldersHandler folder = new FoldersHandler(configuration);
			return folder.createOrUpdateFolder(null, attributes);

		} else
			throw new UnsupportedOperationException("Unsupported object class " + objectClass);
	}

	@Override
	public void init(Configuration configuration) {

		this.configuration = (BoxConnectorConfiguration) configuration;
		this.configuration.validate();
		this.uri = new URIBuilder().setScheme("https").setHost(this.configuration.getUri());

	}

	@Override
	public void dispose() {
		LOG.info("Configuration cleanup");
		configuration = null;
	}

	@Override
	public Configuration getConfiguration() {
		LOG.info("Fetch configuration");
		return this.configuration;
	}

	@Override
	public Uid addAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToAdd, OperationOptions options) {
		if (uid.getUidValue() == null && uid.getUidValue().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Uid not provided or empty:").append(uid.getUidValue()).append(";");
			throw new InvalidAttributeValueException(sb.toString());
		}
		LOG.info("ADD_ATTRIBUTE_VALUES METHOD UID VALUE: {0}", uid.getUidValue());

		if (objclass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("ADD_ATTRIBUTE_VALUES METHOD OBJECTCLASS VALUE: {0}", objclass);

		if (valuesToAdd == null) {
			throw new InvalidAttributeValueException("Values To Add not provided");
		}

		for (Attribute attr : valuesToAdd) {

			if (objclass.is(ObjectClass.GROUP_NAME)) {
				List<Object> attrValues = attr.getValue();
				String objectName = attr.getName();

				for (Object user : attrValues) {

					if ((objectName.equals(ADMIN)) || (objectName.equals(MEMBER))) {

						MembershipHandler membership = new MembershipHandler(configuration);
						membership.createMembership(uid.getUidValue(), (String) user, attr.getName());

					} else {

						CollaborationHandler collab = new CollaborationHandler(configuration);
						collab.createCollaboration(uid.getUidValue(), (String) user, attr.getName());
					}

				}
			}

		}
		return uid;
	}

	@Override
	public Uid removeAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToRemove,
			OperationOptions options) {
		if (uid.getUidValue() == null && uid.getUidValue().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Uid not provided or empty:").append(uid.getUidValue()).append(";");
			throw new InvalidAttributeValueException(sb.toString());
		}
		LOG.info("REMOVE_ATTRIBUTE_VALUES METHOD UID VALUE: {0}", uid.getUidValue());

		if (objclass == null) {
			throw new InvalidAttributeValueException("ObjectClass value not provided");
		}
		LOG.info("REMOVE_ATTRIBUTE_VALUES METHOD OBJECTCLASS VALUE: {0}", objclass);

		if (valuesToRemove == null) {
			throw new InvalidAttributeValueException("Values To Add not provided");
		}
		for (Attribute attr : valuesToRemove) {

			if (objclass.is(ObjectClass.GROUP_NAME)) {
				String objectName = attr.getName();
				List<Object> attrValues = attr.getValue();

				for (Object group : attrValues) {
					if ((objectName.equals(ADMIN)) || (objectName.equals(MEMBER))) {
						MembershipHandler membership = new MembershipHandler(configuration);
						membership.deleteMembership(uid.getUidValue(), (String) group);

					} else {

						CollaborationHandler collab = new CollaborationHandler(configuration);
						collab.deleteCollaboration(uid.getUidValue(), (String) group);
					}
				}
			}

		}
		return uid;
	}

}
