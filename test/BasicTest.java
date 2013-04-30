import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Before
    public void setup(){
        Fixtures.deleteDatabase();
    }

    @Test
    public void createAndRetriveUser(){
        //Create a new user and save it
        new User("Stefan@digiplant.se", "digiplant", "Stefan Jurmu").save();

        //Retrive the user with e-mail address Stefan@digiplant.se
        User stefan = User.find("byEmail", "Stefan@digiplant.se").first();

        //Test
        assertNotNull(stefan);
        assertEquals("Stefan Jurmu", stefan.fullname);

    }

    @Test
    public void tryConnectAsUser() {
        //Create a new user and save it
        new User("Stefan@digiplant.se", "digiplant", "Stefan Jurmu").save();

        //Test
        assertNotNull(User.connect("Stefan@digiplant.se", "digiplant"));
        assertNull(User.connect("Stefan@digiplant.se", "fefefw"));
        assertNull(User.connect("tefan@digiplant.se", "digiplant"));
    }

    @Test
    public void createPost(){

        // Create a new user and save it
        User stefan = new User("Stefan@digiplant.se", "digiplant", "Stefan Jurmu").save();

        //Create a new post
        new Post(stefan, "My first post", "Hello world!").save();

        //Test that the post has been created
        assertEquals(1, Post.count());

        //Retrieve all posts created by Stefan
        List<Post> stefanPosts = Post.find("byAuthor", stefan).fetch();

        //Tests
        assertEquals(1, stefanPosts.size());
        Post firstPost = stefanPosts.get(0);
        assertNotNull(firstPost);
        assertEquals(stefan, firstPost.author);
        assertEquals("My first post", firstPost.title);
        assertEquals("Hello world!", firstPost.content);
        assertNotNull(firstPost.postedAt);

    }

    @Test
    public void postComment() {
        //Create a new user and save it
        User stefan = new User("Stefan@digiplant.se", "digiplant", "Stefan Jurmu").save();

        //Create a new post
        Post stefanPost = new Post(stefan, "My first post", "Hello world!").save();

        //Post a first comment
        new Comment(stefanPost, "Jeff", "Nice post").save();
        new Comment(stefanPost, "Tom", "I knew that!").save();

        //Retrieve all comments
        List<Comment> stefanPostComments = Comment.find("byPost", stefanPost).fetch();

        //Tests
        assertEquals(2, stefanPostComments.size());
        Comment firstComment = stefanPostComments.get(0);
        assertNotNull(firstComment);
        assertEquals("Jeff", firstComment.author);
        assertEquals("Nice post", firstComment.content);
        assertNotNull(firstComment.postedAt);

        Comment secondComment = stefanPostComments.get(1);
        assertNotNull(secondComment);
        assertEquals("Tom", secondComment.author);
        assertEquals("I knew that!", secondComment.content);
        assertNotNull(secondComment.postedAt);

    }

    @Test
    public void useTheCommentRelation(){
        //Create a new user and save it
        User stefan = new User("Stefan@digiplant.se", "digiplant", "Stefan Jurmu").save();

        //Create a new post
        Post stefanPost = new Post(stefan, "My first post", "Hello world!").save();

        //Post a first comment
        stefanPost.addComment("Jeff", "Nice post!");
        stefanPost.addComment("Tom", "I knew that!");

        //Count things
        assertEquals(1, User.count());
        assertEquals(1, Post.count());
        assertEquals(2, Comment.count());

        //Retrieve Stefan's post
        stefanPost = Post.find("byAuthor", stefan).first();
        assertNotNull(stefanPost);

        //Navigate to comments
        assertEquals(2, stefanPost.comments.size());
        assertEquals("Jeff", stefanPost.comments.get(0).author);

        //Delete the post
        stefanPost.delete();

        //Check that all comments have been deleted
        assertEquals(1, User.count());
        assertEquals(0, Post.count());
        assertEquals(0, Comment.count());

    }

    @Test
    public void fullTest(){
        Fixtures.loadModels("data.yml");

        //Count Things
        assertEquals(2, User.count());
        assertEquals(3, Post.count());
        assertEquals(3, Comment.count());

        //Try to connect as users
        assertNotNull(User.connect("Stefan@digiplant.se", "digiplant"));
        assertNotNull(User.connect("jeff@gmail.com", "secret"));
        assertNull(User.connect("jeff@gmail.com", "basPassword"));
        assertNull(User.connect("tom@gmail.com", "secret"));

        //Find all of Stefan's posts
        List<Post> stefanPosts = Post.find("author.email", "Stefan@digiplant.se").fetch();
        assertEquals(2, stefanPosts.size());

        //Find all comments related to Stefan's posts
        List<Comment> stefanComments = Comment.find("post.author.email", "Stefan@digiplant.se").fetch();
        assertEquals(3,stefanComments.size());

        //Find the most recent post
        Post frontPost = Post.find("order by postedAt desc").first();
        assertNotNull(frontPost);
        assertEquals("About the model layer", frontPost.title);

        //Check that this post has two comments
        assertEquals(2, frontPost.comments.size());

        //Post a new comments
        frontPost.addComment("Jim", "Hello guys!");
        assertEquals(3, frontPost.comments.size());
        assertEquals(4, Comment.count());
    }

}
