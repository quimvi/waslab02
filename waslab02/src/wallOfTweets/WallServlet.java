package wallOfTweets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;



@SuppressWarnings("serial")
@WebServlet(urlPatterns = {"/tweets", "/tweets/*"})
public class WallServlet extends HttpServlet {

	private String TWEETS_URI = "/waslab02/tweets/";

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
				String tweetResponse = newTweet.toString();
				resp.getWriter().println(tweetResponse);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	// Implements DELETE http://localhost:8080/waslab02/tweets/:id
	public void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		System.out.println("delete server");

				String uri = req.getRequestURI();
				System.out.println(uri);
				int lastIndex = uri.lastIndexOf("/");
				long id = Long.valueOf(uri.substring(lastIndex+1, uri.length()));		
				System.out.println(id);
				Database.deleteTweet(id);
			
		
	}

}
