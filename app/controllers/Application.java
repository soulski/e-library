package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.avaje.ebean.Ebean;

import controllers.form.RentalForm;
import controllers.form.SaveRentalForm;

import models.Book;
import models.Book.BookStatus;
import models.Rental;
import models.Rental.RentalStatus;
import models.User;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {
  
	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}
  
	public static Result rent() {	
		Book book;
		return ok(rent.render());
	}
	
	public static Result createUser() {
		Form<User> userForm = form(User.class).bindFromRequest();		
		ObjectNode result = Json.newObject();
		
		if (userForm.hasErrors()) {
			result.put("status", false);
			result.put("error", userForm.errorsAsJson());
			return ok(result);
		}
		
		User user = userForm.get();		
		Ebean.save(user);
				
		result.put("status", true);
		result.put("output", Json.toJson(user));
		return ok(result);
	}
	
	public static Result findUser(String keyword) {
		ObjectNode result = Json.newObject();
		User user = User.find.byId(keyword);
		
		result.put("status", true);
		result.put("output", Json.toJson(user));
		return ok(result);
	}
	
	public static Result findBookByIsbn(String isbn) {
		ObjectNode result = Json.newObject();
		Book book = Book.find.where().eq("isbn", isbn).findUnique();
		
		if (book == null) {
			result.put("status", false);
			result.put("error", "Cannot find book isbn : " + isbn);
			return ok(result);
		}
		else if (BookStatus.AVAILABLE != book.status) {
			result.put("status", false);
			result.put("error", "Book is not available");
			return ok(result);
		}
		
		result.put("status", true);
		result.put("output", Json.toJson(book));
		return ok(result);
	}
	
	public static Result findRentalBook(String userId) {
		ObjectNode result = Json.newObject();		
		
		Logger.debug("UserId : " + userId);
		
		List<Rental> rentalList = Rental.find.where().eq("user_id", userId).eq("status", RentalStatus.RENTAL).findList();
		
		Logger.debug("RentalList : " + rentalList);
		
		List<RentalForm> output = new ArrayList<RentalForm>();
		for (Rental rental : rentalList) {
			RentalForm form = new RentalForm();			
			
			form.isbn = rental.book.isbn;
			form.name = rental.book.name;	
			output.add(form);
		}
		
		result.put("status", true);
		result.put("output", Json.toJson(output));
		return ok(result);
	}
	
	public static Result saveRental() {	
		ObjectNode result = Json.newObject();
		
		Form<SaveRentalForm> saveForm = form(SaveRentalForm.class).bindFromRequest();
	
		if (saveForm.hasErrors()) {
			result.put("status", false);
			result.put("error", saveForm.errorsAsJson());
			return ok(result);
		}
		
		SaveRentalForm saveRentalForm = saveForm.get();
		Logger.debug("[Form]User Id : " + saveRentalForm.userId);
		Logger.debug("[Form]Book List : " + saveRentalForm.bookList.size());
		User user = User.find.byId(saveRentalForm.userId);
		List<Book> bookList = Book.find.where().in("ISBN", saveRentalForm.bookList).findList();
		
		Ebean.beginTransaction();
		
		for (Book book : bookList) {	
			if (!user.containBook(book.isbn)) {
				Rental rental = new Rental(book);			
				user.rentals.add(rental);
				book.status = BookStatus.RENT;
				Ebean.update(book);
			}						
		}
		
		Ebean.update(user);
		Ebean.commitTransaction();
		Ebean.endTransaction();
		
		return ok();
		
	}
  
}