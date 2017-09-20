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
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

import com.evolveum.polygon.connector.box.rest.BoxConnector;
import com.evolveum.polygon.connector.box.rest.BoxConnectorConfiguration;

public class FolderTests {

	BoxConnectorConfiguration config = new BoxConnectorConfiguration();

	String clientId = "PleaseEnterValue";
	GuardedString clientSecret = new GuardedString(new String("PleaseEnterValue").toCharArray());
	GuardedString refreshToken = new GuardedString(
			new String("PleaseEnterValue").toCharArray());
	GuardedString accessToken = new GuardedString(new String("PleaseEnterValue").toCharArray());
	String boxEndpoint = "api.box.com";

	private static final Log LOG = Log.getLog(BoxConnector.class);
	private static final String FOLDER_NAME = "Folders";
	private static final Set<Attribute> folderAttributes = new HashSet<Attribute>();
	private static final BoxConnector connector = new BoxConnector();
	private static final ObjectClass folderObject = new ObjectClass("Folders");
	private static final OperationOptions options = new OperationOptions(new HashMap<String, Object>());
	private static final ArrayList<ConnectorObject> folderResults = new ArrayList<>();
	private static final String newFolderName = "ľščťžýáíéäúôöüß$#@%^&*<>(?";
	private static final String updatedName = "TestUpdateFolder";
	private Uid folderUid;

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
	public void folderSchemaTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		connector.init(config);

		Schema schema = connector.schema();

		Set<AttributeInfo> folderAttributesInfo = schema.findObjectClassInfo(FOLDER_NAME).getAttributeInfo();

		for (AttributeInfo attributeInfo : folderAttributesInfo) {
			if (!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()) {
				if (attributeInfo.getName().equals("parent.id")) {
					folderAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "0"));
				} else if (attributeInfo.getType().equals(String.class)) {
					folderAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "test_folder"));
				} else if (attributeInfo.getType().equals(Boolean.class)) {
					folderAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "false"));
				} else if (attributeInfo.getType().equals(Integer.class)) {
					folderAttributes.add(AttributeBuilder.build(attributeInfo.getName(), 0));
				}

			}

		}
		LOG.info("IATTRIBUTES: {0}", folderAttributes);

		folderUid = connector.create(folderObject, folderAttributes, options);

		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(folderUid);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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

		folderResults.clear();
		connector.executeQuery(folderObject, eqfilter, handlerFolder, options);

		try {
			if (!folderResults.get(0).getAttributes().containsAll(folderAttributes)) {
				throw new InvalidAttributeValueException(
						"Attributes of created folder and searched folder DO NOT CORRESPOND.");
			} else
				LOG.ok("\n\t-------------------------------------------------------------------------\n\t| PASSED: Attributes of created folder and searched folder CORRESPOND |\n\t-------------------------------------------------------------------------");
		} finally {
			connector.delete(folderObject, folderUid, options);
			connector.dispose();
		}

	}

	@Test(priority = 3)
	public void CreateFolderTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		connector.init(config);

		folderAttributes.clear();
		folderAttributes.add(AttributeBuilder.build("parent.id", "0"));
		folderAttributes.add(AttributeBuilder.build("__NAME__", newFolderName));

		folderUid = connector.create(folderObject, folderAttributes, null);

		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(folderUid);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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
		folderResults.clear();
		connector.executeQuery(folderObject, eqfilter, handlerFolder, options);

		if (!folderResults.get(0).getAttributes().containsAll(folderAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created folder and searched folder DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created folder and searched folder CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 9, expectedExceptions = UnknownUidException.class)
	public void unknownFolderUidNegativeTest() {

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
				folderResults.add(connectorObject);
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
		folderResults.clear();
		connector.executeQuery(folderObject, equalsFilter, handlerAccount, options);
	}

	@Test(priority = 4, expectedExceptions = AlreadyExistsException.class)
	public void createDuplicateFolderNegativeTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		connector.create(folderObject, folderAttributes, null);

	}

	@Test(priority = 5)
	public void UpdateFolderTest() {

		folderAttributes.clear();
		folderAttributes.add(AttributeBuilder.build("__NAME__", updatedName));
		folderAttributes.add(AttributeBuilder.build("description", "TESTupdateFOLDERdescription"));

		connector.update(folderObject, folderUid, folderAttributes, options);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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
		folderResults.clear();
		AttributeFilter equalsFilterUpdate = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, folderUid.getUidValue()));
		connector.executeQuery(folderObject, equalsFilterUpdate, handlerFolder, options);

		if (!folderResults.get(0).getAttributes().containsAll(folderAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created folder and searched folder DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created folder and searched folder CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 6)
	public void equalsUidFilterFolderTest() {

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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
				.equalTo(AttributeBuilder.build(Uid.NAME, folderUid.getUidValue()));

		folderResults.clear();
		connector.executeQuery(folderObject, equalsUidFilter, handlerFolder, options);

		for (int i = 0; i < folderResults.size(); i++) {
			LOG.info("RESULT: {0}", folderResults.get(i).getAttributes());
			if (folderResults.get(i).getUid().equals(folderUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of account and searched account CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created account and searched account DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 6, expectedExceptions = ConnectorException.class)
	public void equalsNameFilterFolderTest() {

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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

		folderResults.clear();
		connector.executeQuery(folderObject, equalsNameFilter, handlerFolder, options);

		for (int i = 0; i < folderResults.size(); i++) {
			LOG.info("RESULT: {0}", folderResults.get(i).getAttributes());
			if (folderResults.get(i).getUid().equals(folderUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of account and searched account CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created account and searched account DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 8)
	public void listAllGroupTest() {

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				folderResults.add(connectorObject);
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

		folderResults.clear();

		connector.executeQuery(folderObject, null, handlerFolder, options);

		System.out.println("result size : " + String.valueOf(folderResults.size()));
		for (ConnectorObject objectsa : folderResults) {

			System.out.println("Connector Objects *****: " + objectsa.toString());

		}
	}

	@Test(priority = 10)
	public void deleteAccountTest() {
		if (folderUid != null) {
			connector.delete(folderObject, folderUid, options);
		}
	}

	@Test(priority = 21)
	public void disposeTest() {
		connector.dispose();
	}
}
