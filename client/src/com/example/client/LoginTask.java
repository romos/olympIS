package com.example.client;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import com.example.client.exceptions.XMLgenerationException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.concurrent.TimeoutException;

/**
 * Class for country login through Internet.
 * @author Podkopaev Anton
 */
public class LoginTask extends AsyncTask<String, Integer, Boolean> {
	public LoginTask(String login, String password, URL serverURL, AuthorizationActivity authorizationActivityObject) {
		this.login       = login;
		this.password    = password;
		this.serverURL   = serverURL;
		this.authorizationActivityObject = authorizationActivityObject;
		this.country     = null;
	}

	@Override
	protected Boolean doInBackground(String... data) {
		try {
			TimeoutClient cl = new TimeoutClient(serverURL);
			String requestXML = generateXML();
			String answerXML  = cl.execute(requestXML);

			Log.d("ANL", answerXML);

			country = getCountryName(answerXML);
		} catch (IOException e) {
			Log.d("ANL", "Server LoginTask IOException error!");
			country = null;
		} catch (XMLgenerationException e) {
			Log.d("ANL", "XML generation error!");
			country = null;
		} catch (TimeoutException e) {
			Log.d("ANL", "LoginTask timeout!");
			country = null;
		}

		return country != null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.d("ANL", "Login by " + country + " was successfully done.");
		} else {
			Log.d("ANL", "Login fail!");
		}
		authorizationActivityObject.onLogin(result);
	}

	protected String generateXML() throws XMLgenerationException {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter  writer     = new StringWriter();

		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "login-request");
			serializer.attribute("", "login"   , login);
			serializer.attribute("", "password", password);
			serializer.endTag("", "login-request");

			serializer.endDocument();
			return writer.toString();

		} catch (IOException e) {
			throw new XMLgenerationException();
		}
	}

	protected String getCountryName(String xmlResponse) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(xmlResponse));
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String name = parser.getName();
					if (name.equalsIgnoreCase("login-response")) {
						String countryName = parser.getAttributeValue("", "country");
						if (!countryName.equals("")) {
							return countryName;
						} else {
							return null;
						}
					}
				}
				eventType = parser.next();
			}

		} catch (XmlPullParserException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return null;
	}

	private String login;
	private String password;
	private URL serverURL;
	AuthorizationActivity authorizationActivityObject;
	private String country;
}
