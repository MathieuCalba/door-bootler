package org.boorbeller.library.notifymyandroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import android.util.Log;

/*
 * Author: Adriano Maia (adriano@usk.bz)
 * Version: 1.0.0 (4/10/2011)
 * Description:
 * 		This library provides easy access to Notify My Android public API. Methods implemented:
 * 			- notify
 * 			- verify
 * 
 * Edited by Mathieu Calba:
 * 			- Constants transformed in static final
 * 			- Remove the main method
 */

public class NMASender {
	private static final String DEFAULT_URL = "https://nma.usk.bz";
	private static final String NOTIFY_PATH = "/publicapi/notify";
	private static final String VERIFY_PATH = "/publicapi/verify";
	private static final String METHOD_POST = "POST";

	// Defining some contants
	public static final String UTF_8_ENCODING = "UTF-8";
	public static final String MESSAGE_KEY_API_KEY = "apikey";
	public static final String MESSAGE_KEY_APP = "application";
	public static final String MESSAGE_KEY_EVENT = "event";
	public static final String MESSAGE_KEY_DESC = "description";
	public static final String MESSAGE_KEY_PRIORITY = "priority";
	public static final String MESSAGE_KEY_DEV_KEY = "developerkey";

	private static String encoding = null;

	/**
	 * Sends a notification using NMA public API.
	 * 
	 * @param app
	 *            Application name (Up to 256 characters)
	 * @param event
	 *            Short description of the even or a subject (Up to 1000 characters)
	 * @param description
	 *            Long description or message body (Up to 10000 characters)
	 * @param priority
	 *            Priority level of the message. -2, -1, 0, 1, 2
	 * @param apiKey
	 *            One or more 48 bytes long API key, separated by commas.
	 * @param devKey
	 *            Developer key.
	 * @return result
	 */
	public static void notify(String app, String event, String description, int priority, String apiKey, String devKey) throws IllegalArgumentException,
	NMAServerException, NMAErrorException {

		// First some parameter validation. Those tests are done again server-side, but there is no need to submit it if we know it's wrong.
		if ((app.length() == 0) || (app.length() > 256)) {
			throw new IllegalArgumentException("app parameter (" + app + ") must have between 1 and 256 characters");
		}
		if ((event.length() == 0) || (event.length() > 1000)) {
			throw new IllegalArgumentException("event parameter (" + event + ") must have between 1 and 1000 characters");
		}
		if ((description.length() == 0) || (description.length() > 10000)) {
			throw new IllegalArgumentException("description parameter (" + description + ") must have between 1 and 10000 characters");
		}
		if ((priority < -2) || (priority > 2)) {
			throw new IllegalArgumentException("priority parameter (" + priority + ") must be one of these values: -2, -1, 0, 1 ,2");
		}
		if (apiKey.indexOf(',') == -1) {
			if (apiKey.length() != 48) {
				throw new IllegalArgumentException("One or more API keys are of an invalid format. Must be a 48 characters hexadecimal string : " + apiKey);
			}
		} else {
			String apiKeysArray[] = apiKey.split(",");
			for (int i = 0; i < apiKeysArray.length; i++) {
				if (apiKeysArray[i].length() != 48) {
					throw new IllegalArgumentException("One or more API keys are of an invalid format. Must be a 48 characters hexadecimal string : "
							+ apiKeysArray[i]);
				}
			}
		}
		if (devKey != null) {
			if (devKey.length() != 48) {
				throw new IllegalArgumentException("Developer key (" + devKey + ") is of an invalid format");
			}
		}

		// Setup objects to submit the data
		URL url = null;
		HttpURLConnection connection = null;
		encoding = UTF_8_ENCODING;
		StringBuilder data = new StringBuilder();

		try {
			url = new URL(DEFAULT_URL + NOTIFY_PATH);
		} catch (MalformedURLException e) {
			throw new NMAErrorException(e);
		}

		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(METHOD_POST);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// Setup the POST data
			try {
				addEncodedParameter(data, MESSAGE_KEY_API_KEY, apiKey);
				addEncodedParameter(data, MESSAGE_KEY_APP, app);
				addEncodedParameter(data, MESSAGE_KEY_EVENT, event);
				addEncodedParameter(data, MESSAGE_KEY_DESC, description);
				addEncodedParameter(data, MESSAGE_KEY_PRIORITY, Integer.toString(priority));
				if (devKey != null) {
					addEncodedParameter(data, MESSAGE_KEY_DEV_KEY, devKey);
				}
			} catch (IOException e) {
				throw new NMAErrorException("Error while adding encoded parameter", e);
			}

			// Buffers and Writers to send the data
			OutputStreamWriter writer;
			writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(data.toString());
			writer.flush();
			writer.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				StringBuffer response = new StringBuffer();
				String line = null;
				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String resultStr = response.toString();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				InputSource inStream = new InputSource();
				inStream.setCharacterStream(new StringReader(resultStr));
				Document doc = db.parse(inStream);

				Element root = doc.getDocumentElement();

				if (root.getTagName().equals("nma")) {
					Node item = root.getFirstChild();
					String childName = item.getNodeName();
					if (childName.equals("success")) {
						Log.v("NMASender", "notify successful");
						return;
					} else {
						throw new NMAErrorException(item.getFirstChild().getNodeValue());
					}
				}
			} else {
				throw new NMAServerException(
						"There was a problem contacting NMA Servers. HTTP Response code different than 200(OK). Try again or contact support@nma.bz if it persists.");
			}

		} catch (Exception e) {
			throw new NMAErrorException(e);
		}
	}

	/**
	 * 
	 * @param app
	 * @param event
	 * @param description
	 * @param priority
	 * @param apiKey
	 * @return
	 */
	public static void notify(String app, String event, String description, int priority, String apiKey) throws IllegalArgumentException, NMAServerException,
	NMAErrorException {
		notify(app, event, description, priority, apiKey, null);
	}

	/**
	 * 
	 * @param app
	 * @param event
	 * @param description
	 * @param apiKey
	 * @return
	 */
	public static void notify(String app, String event, String description, String apiKey) throws IllegalArgumentException, NMAServerException,
	NMAErrorException {
		notify(app, event, description, 0, apiKey, null);
	}

	public static boolean notifyPush(String app, String event, String description, int priority, String apiKey, String devKey) {
		try {
			notify(app, event, description, priority, apiKey, devKey);
			return true;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (NMAServerException e) {
			e.printStackTrace();
			return false;
		} catch (NMAErrorException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Sends a notification using NMA public API.
	 * 
	 * @param apiKey
	 *            Only one 48 bytes long API key. param devKey Developer key. return result
	 */
	public static void verify(String apiKey, String devKey) throws IllegalArgumentException, NMAServerException, NMAErrorException {
		// First some parameter validation. Those tests are done again server-side, but there is no need to submit it if we know it's wrong.
		if (apiKey.length() != 48) {
			throw new IllegalArgumentException("API key is of an invalid format. Must be a 48 characters hexadecimal string : " + apiKey);
		}
		if (devKey != null) {
			if (devKey.length() != 48) {
				throw new IllegalArgumentException("Developer key (" + devKey + ") is of an invalid format");
			}
		}

		// Setup objects to submit the data
		URL url = null;
		HttpURLConnection connection = null;
		encoding = UTF_8_ENCODING;
		StringBuilder data = new StringBuilder();

		try {
			url = new URL(DEFAULT_URL + VERIFY_PATH);
		} catch (MalformedURLException e) {
			throw new NMAErrorException(e);
		}

		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(METHOD_POST);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			// Setup the POST data
			try {
				addEncodedParameter(data, MESSAGE_KEY_API_KEY, apiKey);
				if (devKey != null) {
					addEncodedParameter(data, MESSAGE_KEY_DEV_KEY, devKey);
				}
			} catch (IOException e) {
				throw new NMAErrorException("Error while adding encoded parameter", e);
			}

			// Buffers and Writers to send the data
			OutputStreamWriter writer;
			writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(data.toString());
			writer.flush();
			writer.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				StringBuffer response = new StringBuffer();
				String line = null;
				while ((line = in.readLine()) != null) {
					response.append(line);
				}

				String resultStr = response.toString();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = factory.newDocumentBuilder();
				InputSource inStream = new InputSource();
				inStream.setCharacterStream(new StringReader(resultStr));
				Document doc = db.parse(inStream);

				Element root = doc.getDocumentElement();

				if (root.getTagName().equals("nma")) {
					Node item = root.getFirstChild();
					String childName = item.getNodeName();
					if (childName.equals("success")) {
						Log.v("NMASender", "verify successful");
						return;
					} else {
						throw new NMAErrorException(item.getFirstChild().getNodeValue());
					}
				}
			} else {
				throw new NMAServerException(
						"There was a problem contacting NMA Servers. HTTP Response code different than 200(OK). Try again or contact support@nma.bz if it persists.");
			}

		} catch (Exception e) {
			throw new NMAErrorException(e);
		}
	}

	/**
	 * 
	 * @param apiKey
	 * @return
	 */
	public static void verify(String apiKey) throws IllegalArgumentException, NMAServerException, NMAErrorException {
		verify(apiKey, null);
	}

	/**
	 * Dynamically adds a url-form-encoded key/value to a StringBuilder
	 * 
	 * @param sb
	 *            StringBuilder buffer used to build the final url-form-encoded data
	 * @param name
	 *            Key name
	 * @param value
	 *            Value
	 * @throws IOException
	 */
	private static void addEncodedParameter(StringBuilder sb, String name, String value) throws IOException {
		if (sb.length() > 0) {
			sb.append("&");
		}
		try {
			sb.append(URLEncoder.encode(name, encoding));
			sb.append("=");
			if (value == null) {
				throw new IOException("ERROR: " + name + " is null");
			} else {
				sb.append(URLEncoder.encode(value, encoding));
			}
		} catch (UnsupportedEncodingException e) {
			// Exception handling
		}
	}

	// Test case. Not meant to be used like a full featured command-line program.
	public static void verifyAndNotify(String lApiKey, String lAppName, String lEvent, String lDesc, int lPriority, String devKey) {
		try {
			NMASender.verify(lApiKey);
			Log.v("NMASender", "Key [" + lApiKey + "] is valid!");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NMAServerException e) {
			e.printStackTrace();
		} catch (NMAErrorException e) {
			e.printStackTrace();
		}

		try {
			NMASender.notify(lAppName, lEvent, lDesc, lPriority, lApiKey, devKey);
			Log.v("NMASender", "Message sent!");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NMAServerException e) {
			e.printStackTrace();
		} catch (NMAErrorException e) {
			e.printStackTrace();
		}
	}

}
