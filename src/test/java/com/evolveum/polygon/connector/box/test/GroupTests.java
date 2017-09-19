package com.evolveum.polygon.connector.box.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.common.objects.filter.StartsWithFilter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

import com.evolveum.polygon.connector.box.rest.BoxConnector;
import com.evolveum.polygon.connector.box.rest.BoxConnectorConfiguration;

public class GroupTests {

	BoxConnectorConfiguration config = new BoxConnectorConfiguration();

	String clientId = "PleaseEnterValue";
	GuardedString clientSecret = new GuardedString(new String("PleaseEnterValue").toCharArray());
	GuardedString refreshToken = new GuardedString(
			new String("PleaseEnterValue").toCharArray());
	GuardedString accessToken = new GuardedString(new String("PleaseEnterValue").toCharArray());
	String boxEndpoint = "api.box.com";

	private static final Log LOG = Log.getLog(BoxConnector.class);
	private static final Set<Attribute> groupAttributes = new HashSet<Attribute>();
	private static final BoxConnector connector = new BoxConnector();
	private static final ObjectClass groupObject = new ObjectClass("__GROUP__");
	private static final OperationOptions options = new OperationOptions(new HashMap<String, Object>());
	private static final ArrayList<ConnectorObject> groupResults = new ArrayList<>();
	private static final String newAccountName = "ľščťžýáíéäúôöüß$#@%^&*<>(?";
	private static final String updatedName = "TestUpdateGroup";
	private Uid groupUid;

	@Test(priority = 1)
	public void ConfigurationValidity() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);
		connector.test();

	}

	@Test(priority = 2)
	public void groupSchemaTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		Schema schema = connector.schema();

		Set<AttributeInfo> groupAttributesInfo = schema.findObjectClassInfo(ObjectClass.GROUP_NAME).getAttributeInfo();

		for (AttributeInfo attributeInfo : groupAttributesInfo) {
			if (!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()) {
				if (attributeInfo.getType().equals(String.class)) {
					groupAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "test_group"));
				} else if (attributeInfo.getType().equals(Boolean.class)) {
					groupAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "false"));
				} else if (attributeInfo.getType().equals(Integer.class)) {
					groupAttributes.add(AttributeBuilder.build(attributeInfo.getName(), 0));
				}

			}

		}

		groupUid = connector.create(groupObject, groupAttributes, null);

		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(groupUid);

		final ArrayList<ConnectorObject> groupResults = new ArrayList<>();

		SearchResultsHandler handlerGroup = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		connector.executeQuery(groupObject, eqfilter, handlerGroup, options);
		for (Attribute attr : groupResults.get(0).getAttributes()) {
			LOG.info("NAME: {0},    VALUE {1}", attr.getName(), attr.getValue());
		}
		for (Attribute attr : groupAttributes) {
			LOG.info("NAME: {0}, VALUE {1}      {2}", attr.getName(), attr.getValue().get(0),
					groupResults.get(0).getAttributes().contains(attr));
		}
		try {
			if (!groupResults.get(0).getAttributes().containsAll(groupAttributes)) {
				throw new InvalidAttributeValueException(
						"Attributes of created group and searched group DO NOT CORRESPOND.");
			} else
				LOG.ok("\n\t-------------------------------------------------------------------------\n\t| PASSED: Attributes of created group and searched group CORRESPOND |\n\t-------------------------------------------------------------------------");
		} finally {

			connector.delete(groupObject, groupUid, options);
			connector.dispose();
		}

	}

	@Test(priority = 3)
	public void CreateGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		connector.init(config);

		groupAttributes.clear();
		groupAttributes.add(AttributeBuilder.build("__NAME__", newAccountName));

		groupUid = connector.create(groupObject, groupAttributes, null);

		final ArrayList<ConnectorObject> groupResults = new ArrayList<>();
		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(groupUid);

		SearchResultsHandler handlerGroup = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		groupResults.clear();

		connector.executeQuery(groupObject, eqfilter, handlerGroup, options);

		if (!groupResults.get(0).getAttributes().containsAll(groupAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created group and searched group DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created group and searched group CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 5)
	public void UpdateGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		connector.init(config);

		groupAttributes.clear();
		groupAttributes.add(AttributeBuilder.build("__NAME__", updatedName));

		connector.update(groupObject, groupUid, groupAttributes, options);

		SearchResultsHandler handlerGroup = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		groupResults.clear();
		AttributeFilter equalsFilterUpdate = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, groupUid.getUidValue()));

		connector.executeQuery(groupObject, equalsFilterUpdate, handlerGroup, options);

		if (!groupResults.get(0).getAttributes().containsAll(groupAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created group and searched group DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created group and searched group CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 9, expectedExceptions = UnknownUidException.class)
	public void unknownGroupUidNegativeTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		AttributeFilter equalsFilter = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, "XXXXXXXXX"));
		groupResults.clear();
		connector.executeQuery(groupObject, equalsFilter, handlerAccount, options);
	}

	@Test(priority = 4, expectedExceptions = AlreadyExistsException.class)
	public void createDuplicateGroupNegativeTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		connector.create(groupObject, groupAttributes, null);

	}

	@Test(priority = 6)
	public void equalsUidFilterGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		AttributeFilter equalsUidFilter = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, groupUid.getUidValue()));

		groupResults.clear();
		connector.executeQuery(groupObject, equalsUidFilter, handlerFolder, options);

		for (int i = 0; i < groupResults.size(); i++) {
			LOG.info("RESULT: {0}", groupResults.get(i).getAttributes());
			if (groupResults.get(i).getUid().equals(groupUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of group and searched group CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created group and searched group DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 6, expectedExceptions = ConnectorException.class)
	public void equalsNameFilterGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(false);

		connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		AttributeFilter equalsNameFilter = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Name.NAME, updatedName));

		groupResults.clear();
		connector.executeQuery(groupObject, equalsNameFilter, handlerFolder, options);

		for (int i = 0; i < groupResults.size(); i++) {
			LOG.info("RESULT: {0}", groupResults.get(i).getAttributes());
			if (groupResults.get(i).getUid().equals(groupUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of group and searched group CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created group and searched group DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 6, expectedExceptions = ConnectorException.class)
	public void nameStartWithFilterGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(false);

		connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		AttributeFilter startNameFilter = (StartsWithFilter) FilterBuilder
				.startsWith(AttributeBuilder.build(Name.NAME, "Test"));

		groupResults.clear();
		connector.executeQuery(groupObject, startNameFilter, handlerFolder, options);

		for (int i = 0; i < groupResults.size(); i++) {
			LOG.info("RESULT: {0}", groupResults.get(i).getAttributes());
			if (groupResults.get(i).getUid().equals(groupUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of group and searched group CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created group and searched group DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 7)
	public void listAllGroupTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(false);

		connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				groupResults.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
				LOG.info("im handling {0}", result.getRemainingPagedResults());
				LOG.info("pagedResultsCookie,remainingPagedResults,allResultsReturned {0} {1} {2}",
						result.getPagedResultsCookie(), result.getRemainingPagedResults(),
						result.isAllResultsReturned());

			}
		};

		Map<String, Object> operationOptions = new HashMap<String, Object>();
		operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
		operationOptions.put(OperationOptions.OP_PAGE_SIZE, 13);
		OperationOptions options = new OperationOptions(operationOptions);

		groupResults.clear();

		connector.executeQuery(groupObject, null, handlerFolder, options);

		System.out.println("result size : " + String.valueOf(groupResults.size()));
		for (ConnectorObject objectsa : groupResults) {

			System.out.println("Connector Objects *****: " + objectsa.toString());

		}
	}

	@Test(priority = 10)
	public void deleteAccountTest() {
		if (groupUid != null) {
			connector.delete(groupObject, groupUid, options);
		}
	}

	@Test(priority = 21)
	public void disposeTest() {
		connector.dispose();
	}

}
