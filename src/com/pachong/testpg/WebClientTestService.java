package com.pachong.testpg;
import org.apache.http.*;

public class WebClientTestService {
	
	public void WebClient() {
		  currentURL = null;
		  client = new DefaultHttpClient();
		  client.setCookieStore(new UpdateableCookieStore());
		  client.setRedirectHandler(new MemorizingRedirectHandler());
		  client.getParams().setParameter("http.protocol.cookie-policy",
		    "compatibility");
		  List headers = new ArrayList();
		  headers.add(new BasicHeader(
		      "User-Agent",
		      "Mozilla/5.0 (Windows; U; Windows NT 5.1; nl; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13"));
		  client.getParams().setParameter("http.default-headers", headers);
		 }

}
