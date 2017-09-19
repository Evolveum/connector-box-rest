package com.evolveum.polygon.connector.box.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
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

public class AccountTests {

	BoxConnectorConfiguration config = new BoxConnectorConfiguration();

	String clientId = "PleaseEnterValue";
	GuardedString clientSecret = new GuardedString(new String("PleaseEnterValue").toCharArray());
	GuardedString refreshToken = new GuardedString(
			new String("PleaseEnterValue").toCharArray());
	GuardedString accessToken = new GuardedString(new String("PleaseEnterValue").toCharArray());

	GuardedString invalidClientSecret = new GuardedString(new String("PleaseEnterValue").toCharArray());
	GuardedString invalidRefreshToken = new GuardedString(
			new String("PleaseEnterValue").toCharArray());
	GuardedString invalidAccessToken = new GuardedString(new String("PleaseEnterValue").toCharArray());
	String boxEndpoint = "api.box.com";

	private static final Log LOG = Log.getLog(BoxConnector.class);
	private static final Set<Attribute> accountAttributes = new HashSet<Attribute>();
	private static final BoxConnector connector = new BoxConnector();
	private static final ObjectClass accountObject = new ObjectClass("__ACCOUNT__");
	private static final OperationOptions options = new OperationOptions(new HashMap<String, Object>());
	private static final ArrayList<ConnectorObject> accountResults = new ArrayList<>();
	private static final String newAccountName = "TestCreateAccount";
	private static final String updatedName = "+ľščťžýáíé&$#UpdateAccount";
	private Uid accountUid;

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
	public void accountSchemaTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		Schema schema = connector.schema();

		Set<AttributeInfo> accountAttributesInfo = schema.findObjectClassInfo(ObjectClass.ACCOUNT_NAME)
				.getAttributeInfo();

		for (AttributeInfo attributeInfo : accountAttributesInfo) {
			if (!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()) {
				if (attributeInfo.getName().equals("login")) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "test_user@testmail.com"));
				} else if (attributeInfo.getName().equals("timezone")) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "Europe/Bratislava"));
				} else if (attributeInfo.getName().equals("language")) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "en"));
				} else if (attributeInfo.getName().equals("role")) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "coadmin"));
				} else if (attributeInfo.getType().equals(String.class)) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "test_user"));
				} else if (attributeInfo.getType().equals(Boolean.class)) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), "true"));
				} else if (attributeInfo.getType().equals(Integer.class)) {
					accountAttributes.add(AttributeBuilder.build(attributeInfo.getName(), 0));
				}
				accountAttributes.add(AttributeBuilder.build(OperationalAttributes.ENABLE_NAME, false));

			}

		}

		accountUid = connector.create(accountObject, accountAttributes, null);

		AttributeFilter eqfilter = (EqualsFilter) FilterBuilder.equalTo(accountUid);

		final ArrayList<ConnectorObject> accountResults = new ArrayList<>();

		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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

		connector.executeQuery(accountObject, eqfilter, handlerAccount, options);

		try {
			if (!accountResults.get(0).getAttributes().containsAll(accountAttributes)) {
				throw new InvalidAttributeValueException(
						"Attributes of created account and searched account DO NOT CORRESPOND.");
			} else
				LOG.ok("\n\t-------------------------------------------------------------------------\n\t| PASSED: Attributes of created account and searched account CORRESPOND |\n\t-------------------------------------------------------------------------");
		} finally {
			connector.delete(accountObject, accountUid, options);
			connector.dispose();
		}
	}

	@Test(priority = 3)
	public void CreateAccountTest() {

		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);
		connector.init(config);

		accountAttributes.clear();
		accountAttributes.add(AttributeBuilder.build("login", "Testik@ťžéäúevo.com"));
		accountAttributes.add(AttributeBuilder.build("__NAME__", newAccountName));

		final ArrayList<ConnectorObject> accountResults = new ArrayList<>();
		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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

		accountUid = connector.create(accountObject, accountAttributes, null);

		AttributeFilter equalsFilter = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, accountUid.getUidValue()));
		accountResults.clear();

		connector.executeQuery(accountObject, equalsFilter, handlerAccount, options);

		if (!accountResults.get(0).getAttributes().containsAll(accountAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created account and searched account DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created account and searched account CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 9, expectedExceptions = UnknownUidException.class)
	public void unknownAccountUidNegativeTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(true);
		// connector.init(config);

		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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
		accountResults.clear();
		connector.executeQuery(accountObject, equalsFilter, handlerAccount, options);

	}

	@Test(priority = 4, expectedExceptions = AlreadyExistsException.class)
	public void createDuplicateAccountNegativeTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(true);
		// connector.init(config);

		connector.create(accountObject, accountAttributes, null);

	}

	@Test(priority = 5)
	public void UpdateAccountTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(true);
		// connector.init(config);

		accountAttributes.clear();
		accountAttributes.add(AttributeBuilder.build("phone", "*123456789*"));
		accountAttributes.add(AttributeBuilder.build("__NAME__", updatedName));

		connector.update(accountObject, accountUid, accountAttributes, options);

		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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

		accountResults.clear();
		AttributeFilter equalsFilterUpdate = (EqualsFilter) FilterBuilder
				.equalTo(AttributeBuilder.build(Uid.NAME, accountUid.getUidValue()));

		connector.executeQuery(accountObject, equalsFilterUpdate, handlerAccount, options);

		if (!accountResults.get(0).getAttributes().containsAll(accountAttributes)) {
			throw new InvalidAttributeValueException(
					"Attributes of created account and searched account DO NOT CORRESPOND.");
		} else
			LOG.ok("\n-------------------------------------------------------------------------"
					+ "\n\tPASSED: Attributes of created account and searched account CORRESPOND"
					+ "\n\t-------------------------------------------------------------------------");

	}

	@Test(priority = 6)
	public void equalsUidFilterAccountTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(true);
		//
		// connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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
				.equalTo(AttributeBuilder.build(Uid.NAME, accountUid.getUidValue()));

		accountResults.clear();
		connector.executeQuery(accountObject, equalsUidFilter, handlerFolder, options);

		for (int i = 0; i < accountResults.size(); i++) {
			LOG.info("RESULT: {0}", accountResults.get(i).getAttributes());
			if (accountResults.get(i).getUid().equals(accountUid)) {
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
	public void equalsNameFilterAccountTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(false);
		//
		// connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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

		accountResults.clear();
		connector.executeQuery(accountObject, equalsNameFilter, handlerFolder, options);

		for (int i = 0; i < accountResults.size(); i++) {
			LOG.info("RESULT: {0}", accountResults.get(i).getAttributes());
			if (accountResults.get(i).getUid().equals(accountUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of account and searched account CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created account and searched account DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 6)
	public void nameStartWithFilterAccountTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(false);
		//
		// connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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
				.startsWith(AttributeBuilder.build(Name.NAME, updatedName));

		accountResults.clear();
		connector.executeQuery(accountObject, startNameFilter, handlerFolder, options);

		for (int i = 0; i < accountResults.size(); i++) {
			LOG.info("RESULT: {0}", accountResults.get(i).getAttributes());
			if (accountResults.get(i).getUid().equals(accountUid)) {
				LOG.ok("\n-------------------------------------------------------------------------"
						+ "\n\tPASSED: Attributes of account and searched account CORRESPOND"
						+ "\n\t-------------------------------------------------------------------------");
			} else {
				throw new InvalidAttributeValueException(
						"Attributes of created account and searched account DO NOT CORRESPOND.");
			}
		}

	}

	@Test(priority = 7)
	public void listAllAccountTest() {

		// config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		// config.setClientSecret(clientSecret);
		// config.setRefreshToken(refreshToken);
		// config.setUri("api.box.com");
		// config.setAccessToken(accessToken);
		// config.setEnableFilteredResultsHandler(false);
		//
		// connector.init(config);

		SearchResultsHandler handlerFolder = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				accountResults.add(connectorObject);
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

		accountResults.clear();

		connector.executeQuery(accountObject, null, handlerFolder, options);

		System.out.println("result size : " + String.valueOf(accountResults.size()));
		for (ConnectorObject objectsa : accountResults) {

			System.out.println("Connector Objects *****: " + objectsa.toString());

		}
	}

	@Test(priority = 11, expectedExceptions = InvalidCredentialException.class)
	public void invalidCredentialsTest() {
		config.setClientId("4ig3tzk76msrvropradguxsxuz7lsuhr");
		config.setClientSecret(invalidClientSecret);
		config.setRefreshToken(invalidRefreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(invalidAccessToken);
		config.setEnableFilteredResultsHandler(false);

		connector.init(config);
		connector.test();

	}

	@Test(priority = 12, expectedExceptions = ConnectorIOException.class)
	public void networkErrorTest() {
		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.brx.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(false);

		connector.init(config);

		connector.test();

	}

	@Test(priority = 13, expectedExceptions = ConfigurationException.class)
	public void configurationMandatoryValuesTest() {
		BoxConnectorConfiguration configuration = new BoxConnectorConfiguration();
		configuration.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		configuration.setRefreshToken(refreshToken);
		configuration.setUri("api.box.com");
		configuration.setAccessToken(accessToken);
		configuration.setEnableFilteredResultsHandler(false);

		connector.init(configuration);

		connector.test();

	}

	@Test(priority = 20)
	public void deleteAccountTest() {
		config.setClientId("4ig3tzk76msrvvvpradguxsxuz7lsuhr");
		config.setClientSecret(clientSecret);
		config.setRefreshToken(refreshToken);
		config.setUri("api.box.com");
		config.setAccessToken(accessToken);
		config.setEnableFilteredResultsHandler(true);

		if (accountUid != null) {
			connector.delete(accountObject, accountUid, options);
		}
	}

	@Test(priority = 21)
	public void disposeTest() {
		connector.dispose();
	}

}
