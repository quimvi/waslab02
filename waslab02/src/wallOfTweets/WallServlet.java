package wallOfTweets;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/tweets", "/tweets/*"})
public class WallServlet extends HttpServlet {

	private String TWEETS_URI = "/waslab02/tweets/";
	Cipher desCipher;
	KeyGenerator keygenerator;
	SecretKey myDesKey;
	private Map<String, String> deletionTokens = new HashMap<String, String>();
	
	public WallServlet() throws NoSuchPaddingException {
		super();
		// TODO Auto-generated constructor stub

		try {
			keygenerator = KeyGenerator.getInstance("DES");
		    myDesKey = keygenerator.generateKey();
		    
		    // Creating object of Cipher
            desCipher = Cipher.getInstance("DES");

           
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	// Implements GET http://localhost:8080/waslab02/tweets
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("application/json");
		resp.setHeader("Cache-control", "no-cache");
		List<Tweet> tweets= Database.getTweets();
		JSONArray job = new JSONArray();
		for (Tweet t: tweets) {
			JSONObject jt = new JSONObject(t);
			jt.remove("class");
			job.put(jt);
		}
		resp.getWriter().println(job.toString());
	}

	@Override
	// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
	//        and POST http://localhost:8080/waslab02/tweets
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String uri = req.getRequestURI();
		int lastIndex = uri.lastIndexOf("/likes");
		if (lastIndex > -1) {  // uri ends with "/likes"
			// Implements POST http://localhost:8080/waslab02/tweets/:id/likes
			long id = Long.valueOf(uri.substring(TWEETS_URI.length(),lastIndex));		
			resp.setContentType("text/plain");
			resp.getWriter().println(Database.likeTweet(id));
		}
		else { 
			// Implements POST http://localhost:8080/waslab02/tweets
			int max_length_of_data = req.getContentLength();
			byte[] httpInData = new byte[max_length_of_data];
			ServletInputStream  httpIn  = req.getInputStream();
			httpIn.readLine(httpInData, 0, max_length_of_data);
			String body = new String(httpInData);
			/*      ^
		      The String variable body contains the sent (JSON) Data. 
		      Complete the implementation below.*/
			try {
				JSONObject test = new JSONObject(body);
				String author = test.getString("author");
				String text = test.getString("text");
				Tweet tweet = Database.insertTweet(author, text);
				JSONObject newTweet = new JSONObject(tweet);
				desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
				String tweetId = Long.toString(tweet.getId());
	            byte[] tweetIdEncrypted = desCipher.doFinal(tweetId.getBytes("UTF8"));
	            String tweetIdEncryptedString = Base64.getEncoder().encodeToString(tweetIdEncrypted);
	            newTweet.append("deleteToken", tweetIdEncryptedString );
				String tweetResponse = newTweet.toString();
				 //we have to send here in the response the deletion token
				
				resp.getWriter().println(tweetResponse);
			} catch (JSONException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	// Implements DELETE http://localhost:8080/waslab02/tweets/:id
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
				String uri = req.getRequestURI();
				System.out.println(uri);
				int lastIndex = uri.lastIndexOf("/");
				long id = Long.valueOf(uri.substring(lastIndex+1, uri.length()));	
				// check here that the deleteToken sent matches with he delete token of the tweet id 
				System.out.println(id);
				Database.deleteTweet(id);
	}

}
