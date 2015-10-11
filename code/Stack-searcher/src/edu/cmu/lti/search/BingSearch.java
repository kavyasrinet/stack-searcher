package edu.cmu.lti.search;

import org.apache.commons.codec.binary.Base64;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class BingSearch {
	static XPathFactory factory = null;
	static XPath xpath = null;
	static XPathExpression expr = null;
	private static boolean VERBOSE = false;
	public static void setVerbose( boolean flag ){
		VERBOSE = flag;
	}

	public static String buildRequest( String queryString , int numResults ) throws URISyntaxException {
		// Note that the query should be in single quotes!
		URI QueryURI = new URI("https", null /* user info */,
				"api.datamarket.azure.com", -1 /* port */,
				"/Bing/Search/Web?",
				"Query='" + queryString + "'&$top=" + numResults + "&$format=atom",
				null /* fragment */);

		return QueryURI.toString();
	}
	
	public static Document getResponse( String requestURL , String AccountKey )
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = null;
		DocumentBuilder db = dbf.newDocumentBuilder();

		if (db != null) {
			URL url = new URL(requestURL);
			if ( VERBOSE ) System.out.println( "Connection URL: " + url );
			URLConnection uc = url.openConnection();
			// The username is empty, the Account key is a password
			//String userpass = AccountKey + ":" + AccountKey;
			String userpass = AccountKey +":" + AccountKey;
			String basicAuth = "Basic " + new String( new Base64().encode( userpass.getBytes() ) ) ;
			uc.setRequestProperty ("Authorization", basicAuth);	

			BufferedReader br = new BufferedReader(new InputStreamReader(
					uc.getInputStream(), "utf-8"));
			String input = "", line = "";

			while ((line = br.readLine()) != null) {
				input += line + "\n";
			}
			br.close();

			// When Bing returns an error, it is just a plain string,
			// not an XML starting with tag <feed

			if (!input.substring(0,10).matches("^\\s*<feed\\s.*")) {	
				throw new SAXException("Bing search failed, error: " + input);
			}

			StringReader reader = new StringReader(input);
			InputSource inputSource = new InputSource(reader);

			doc = db.parse(inputSource);
		}

		return doc;
	}
	
	public static List<Result> processResponse(Document doc, String query)
			throws XPathExpressionException {
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();

		NamespaceContextImpl ctx = new NamespaceContextImpl();

		/*
		 * Prefix mapping for the following XML root tag:
		 *
		 * <feed xmlns:base="https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web"
		 * xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices"
		 * xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" xmlns="http://www.w3.org/2005/Atom">
		 *
		 * NOTE: the default namespace can use any prefix, not necessarily default.
		 * Yet, exactly the same prefix should also be used in XPATH expressions
		 */
		ctx.startPrefixMapping("base", "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/Web");
		ctx.startPrefixMapping("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
		ctx.startPrefixMapping("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
		ctx.startPrefixMapping("default", "http://www.w3.org/2005/Atom");
		xpath.setNamespaceContext(ctx);

		NodeList nodes = (NodeList) xpath.evaluate("/default:feed/default:entry", doc,
				XPathConstants.NODESET);
		List<Result> Reply = new ArrayList<Result>();

		for (int i = 0; i < nodes.getLength(); i++) {
			try {
				Node CurrNode = nodes.item(i);

				String title = (String) xpath.evaluate("default:content/m:properties/d:Title/text()", CurrNode,
						XPathConstants.STRING);
				String desc = (String) xpath.evaluate("default:content/m:properties/d:Description/text()", CurrNode,
						XPathConstants.STRING);
				String url = (String) xpath.evaluate("default:content/m:properties/d:Url/text()", CurrNode,
						XPathConstants.STRING);	

				String DocText = "";
				if (!title.isEmpty()) DocText += title + "\n";
				if (!desc.isEmpty()) DocText += desc + "\n";

				if (!DocText.isEmpty()) {	
					Result res = new Result(DocText, query , url, i);
					res.setScore(-i);

					Reply.add(res);
				}
			} catch (XPathExpressionException e) {
				System.err.printf("[ERROR] cannot parse element # %d, ignoring, error: %s\n",
						i + 1, e.toString());
			}
		}

		if(VERBOSE)
			System.out.println("Bing reply size: " + Reply.size());

		return Reply;
	}
	
}
