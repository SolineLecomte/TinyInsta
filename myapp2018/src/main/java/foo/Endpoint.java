package foo;


import java.util.List;
import java.util.Random;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;


@Api(name = "myApi",
version = "v1")

public class Endpoint {
	
	@ApiMethod(name = "addUser", httpMethod = HttpMethod.POST, path ="addUser")
	public Entity addUser(@Named("name") String name,@Named("email") String email,@Named("username") String username,@Named("password") String password) throws Exception {
			
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Check if username is available
		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", username))); 
		PreparedQuery pq = datastore.prepare(q);
		int usernameAlreadyTaken = pq.countEntities(FetchOptions.Builder.withLimit(1));
		
		//Check if email address is already in use
		Query q2 =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", email))); 
		PreparedQuery pq2 = datastore.prepare(q);
		int mailAlreadyTaken = pq.countEntities(FetchOptions.Builder.withLimit(1));

		if(usernameAlreadyTaken!=0) {
			throw new Exception("Username already taken");
		}
		
		if(mailAlreadyTaken!=0) {
			throw new Exception("This email adress is already in use");
		}
		
		Entity e = new Entity("User", username);
		e.setProperty("name", name);
		e.setProperty("email", email);
		e.setProperty("username", username);
		e.setProperty("password", password);

		datastore.put(e);
		
		return new Entity("Response", "userVerified");
	}
	
	

	
	
	@ApiMethod(name = "verifyUser", httpMethod = HttpMethod.POST, path ="verifyUser")
	public Entity verifyUser(@Named("username") String username,@Named("password") String password) throws Exception {
						
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Get the User Entity with corresponding pseudo
				Query q =
				    new Query("User")
				        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", username)));

				PreparedQuery pq = datastore.prepare(q);
				Entity user = pq.asSingleEntity();
				
				//Verify the password
				String registeredPassword = (String) user.getProperty("password");
				boolean isCorrectPassword = (password.equals(registeredPassword));
				
				if (isCorrectPassword) {
					return new Entity("Response", "userVerified");
				} else {
					throw new Exception("Bad password");
				}
				

	}
	
	@ApiMethod(name = "timeline", httpMethod = HttpMethod.POST, path ="timeline")
	public Entity showTimeline() {
			
		return new Entity("Response", "ok");
	}
	
	
	@ApiMethod(name = "getUser", httpMethod = HttpMethod.POST, path ="getUser")
	public Entity getUser(@Named("username") String username) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", username)));

			PreparedQuery pq = datastore.prepare(q);
			Entity user = pq.asSingleEntity();
			
		return user;
	}
	
	@ApiMethod(name = "follow", httpMethod = HttpMethod.POST, path ="follow")
	public Entity follow(@Named("follower") String follower, @Named("followed") String followed) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Entity e = new Entity("Follow", follower+"->"+followed);
		e.setProperty("follower", follower);
		e.setProperty("followed", followed);
		
		datastore.put(e);
		
		return new Entity("Response", "ok");
	}
	
	
	/*
	Random r=new Random();

	@ApiMethod(name = "getRandom")
	public RandomResult random() {
			return new RandomResult(r.nextInt(6)+1);
	}

	
	@ApiMethod(name = "listAllScore")
	public List<Entity> listAllScoreEntity() {
			Query q =
			    new Query("Score");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}


	@ApiMethod(name = "listScore")
	public List<Entity> listScoreEntity(@Named("name") String name) {
			Query q =
			    new Query("Score")
			        .setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery pq = datastore.prepare(q);
			List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
			return result;
	}
	
	@ApiMethod(name = "addScore")
	public Entity addScore(@Named("score") int score, @Named("name") String name) {
			
			Entity e = new Entity("Score", ""+name+score);
			e.setProperty("name", name);
			e.setProperty("score", score);

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}
	*/

}