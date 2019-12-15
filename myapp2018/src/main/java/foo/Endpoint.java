package foo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.KeyFactory;

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
		PreparedQuery pq2 = datastore.prepare(q2);
		int mailAlreadyTaken = pq2.countEntities(FetchOptions.Builder.withLimit(1));

		if(usernameAlreadyTaken!=0) {
			throw new Exception("Username already taken");
		}
		
		if(mailAlreadyTaken!=0) {
			throw new Exception("This email adress is already in use");
		}
		
				
		String key = PasswordUtils.getSecurePassword(password);
		
		
		Entity e = new Entity("User", username);
		e.setProperty("name", name);
		e.setProperty("email", email);
		e.setProperty("username", username);
		e.setProperty("password", key);
		e.setProperty("nbPost",0);

		datastore.put(e);
		
		return new Entity("Response", "ok");
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
				
				if(user!=null) {
					//Verify the password
					String registeredPassword = (String) user.getProperty("password");
					
					
					boolean isCorrectPassword = PasswordUtils.verifyPassword(registeredPassword, password);
					
					
					if (isCorrectPassword) {
						return new Entity("Response", "userVerified");
					} else {
						return new Entity("Response", "wrongPassword");
					}
				}else {
					return new Entity("Response", "notFound");
				}
				
				
	}
	
	
	@ApiMethod(name = "getUser", httpMethod = HttpMethod.GET, path ="getUser")
	public Entity getUser(@Named("username") String username) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", username)));

			PreparedQuery pq = datastore.prepare(q);
			Entity user = pq.asSingleEntity();
			
		if(user!=null) {
			return user;
		}else {
			return new Entity("Response","notFound");
		}
	}
	
	
	@ApiMethod(name = "isFollowed", httpMethod = HttpMethod.GET, path ="isfollowed")
	public Entity isfollowed(@Named("follower") String follower, @Named("followed") String followed) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter f1 = new FilterPredicate("follower", FilterOperator.EQUAL, follower);
		Filter f2 = new FilterPredicate("followed", FilterOperator.EQUAL, followed);
		Filter final1 = CompositeFilterOperator.and(f1,f2);
		
		Query q0 =
			    new Query("Follow")
			        .setFilter(final1);

		PreparedQuery pq0 = datastore.prepare(q0);
		Entity result = pq0.asSingleEntity();
			
		if(result!=null) {
			return new Entity("Response", "yes");

		}else {
			return new Entity("Response", "nope");
		}
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
	
	@ApiMethod(name = "unfollow", httpMethod = HttpMethod.POST, path ="unfollow")
	public Entity unfollow(@Named("unfollower") String unfollower, @Named("unfollowed") String unfollowed) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Filter f1 = new FilterPredicate("follower", FilterOperator.EQUAL, unfollower);
		Filter f2 = new FilterPredicate("followed", FilterOperator.EQUAL, unfollowed);
		Filter final1 = CompositeFilterOperator.and(f1,f2);
		
		Query q0 =
			    new Query("Follow")
			        .setFilter(final1);

			PreparedQuery pq0 = datastore.prepare(q0);
			Entity result = pq0.asSingleEntity();
		
		datastore.delete(result.getKey());
		
		return new Entity("Response", "ok");
	}
	
	
	@ApiMethod(name = "post", httpMethod = HttpMethod.POST, path ="post")
	public Entity post(@Named("user") String user, BodyUtils content) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query q =
			    new Query("User")
			        .setFilter(new FilterPredicate("__key__" , FilterOperator.EQUAL, KeyFactory.createKey("User", user)));

			PreparedQuery pq = datastore.prepare(q);
			Entity currentUser = pq.asSingleEntity();
		
		long postCount = (long) currentUser.getProperty("nbPost") + 1;
		
		//Update the user's number of posts
		currentUser.setProperty("nbPost", postCount);
		datastore.put(currentUser);
				
		//Create the post
		String date = DateUtils.getDate();
		String postId = user+"_"+postCount;

		Entity e = new Entity("Post", postId);
		e.setProperty("user", user);
		e.setProperty("date", date);
		e.setProperty("image", content.getImage());
		e.setProperty("text", content.getMessage());
		e.setProperty("likes", 0);
		e.setProperty("id", postId);
		datastore.put(e);
		
		return new Entity("Response", "ok");
	}
	
	
	@ApiMethod(name = "timeline", httpMethod = HttpMethod.GET, path ="timeline")
	public List<Entity> timeline(@Named("user") String user) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query q =
			    new Query("Follow")
			        .setFilter(new FilterPredicate("follower", FilterOperator.EQUAL, user));

			PreparedQuery pq = datastore.prepare(q);
			List<Entity> allFollowed = pq.asList(FetchOptions.Builder.withLimit(10));
			
			List<Entity> allTimelinePosts = new ArrayList<Entity>();
			
			
		for(Entity e : allFollowed) {
			Query q1 =
				    new Query("Post")
				        .setFilter(new FilterPredicate("user", FilterOperator.EQUAL, e.getProperty("followed")));

				PreparedQuery pq1 = datastore.prepare(q1);
				List<Entity> allPosts = pq1.asList(FetchOptions.Builder.withLimit(10));
				
				allTimelinePosts.addAll(allPosts);
		}
			
			
		return allTimelinePosts;
	}
	
	
	@ApiMethod(name = "isLiked", httpMethod = HttpMethod.GET, path ="isliked")
	public Entity isLiked(@Named("liker") String liker,@Named("liked") String postId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Check the like
		Filter f1 = new FilterPredicate("liker", FilterOperator.EQUAL, liker);
		Filter f2 = new FilterPredicate("postId", FilterOperator.EQUAL, postId);
		Filter final1 = CompositeFilterOperator.and(f1,f2);
		
		Query q =
			    new Query("Like")
			        .setFilter(final1);

			PreparedQuery pq = datastore.prepare(q);
			Entity isLiked = pq.asSingleEntity();
						
			if(isLiked==null) {
				return new Entity("Response", "nope");
			}else {
				return new Entity("Response", "yes");
			}
		
	}
	
	
	
	@ApiMethod(name = "like", httpMethod = HttpMethod.POST, path ="like")
	public Entity like(@Named("liker") String liker,@Named("liked") String postId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Create the like
		String likeId = liker+"_"+postId;

		Entity e = new Entity("Like", likeId);
		e.setProperty("liker", liker);
		e.setProperty("postId", postId);
		datastore.put(e);
		
		//Add to post's like count
		Query q =
			    new Query("Post")
			        .setFilter(new FilterPredicate("id" , FilterOperator.EQUAL, postId));

			PreparedQuery pq = datastore.prepare(q);
			Entity post = pq.asSingleEntity();
		
		long likeCount = (long) post.getProperty("likes") + 1;
		
		//Update the user's number of posts
		post.setProperty("likes", likeCount);
		datastore.put(post);
		
		return new Entity("Response", "ok");
	}
	
	@ApiMethod(name = "unlike", httpMethod = HttpMethod.POST, path ="unlike")
	public Entity unlike(@Named("unliker") String unliker,@Named("unliked") String postId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		//Delete the like
		Query q0 =
			    new Query("Like")
			        .setFilter(new FilterPredicate("postId" , FilterOperator.EQUAL, postId));

			PreparedQuery pq0 = datastore.prepare(q0);
			Entity like = pq0.asSingleEntity();
		
		datastore.delete(like.getKey());
		
		//Remove from post's like count
		Query q =
			    new Query("Post")
			        .setFilter(new FilterPredicate("id" , FilterOperator.EQUAL, postId));

			PreparedQuery pq = datastore.prepare(q);
			Entity post = pq.asSingleEntity();
		
		long likeCount = (long) post.getProperty("likes") - 1;
		
		//Update the user's number of posts
		post.setProperty("likes", likeCount);
		datastore.put(post);
		
		return new Entity("Response", "ok");
	}
	
}