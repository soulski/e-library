package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import views.html.play20.book;

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
		
		User user = User.find.byId(userId);
		List<Rental> rentalList = Rental.find.where().eq("user_id", userId).eq("status", RentalStatus.RENTAL).findList();
		
		Logger.debug("RentalList : " + rentalList);
		
		List<RentalForm> output = new ArrayList<RentalForm>();
		for (Rental rental : rentalList) {
			RentalForm form = new RentalForm();			
			form.id = rental.book.id;
			form.isbn = rental.book.isbn;
			form.name = rental.book.name;
			form.returnDate = rental.getDueDate(user);
			form.fine = rental.getFine(user);
			output.add(form);
		}
		
		result.put("status", true);
		result.put("output", Json.toJson(output));
		return ok(result);
	}
	
	public static Result returnBook(String userId, Long bookId) {
		ObjectNode result = Json.newObject();		
		Rental rental = Rental.find.where()
				.eq("user_id", userId)
				.eq("book.id", bookId)
				.eq("status", RentalStatus.RENTAL)
				.findUnique();
		
		Logger.debug("test retrun book");
		
		if (rental != null) {
			Ebean.beginTransaction();
			
			rental.status = RentalStatus.RETURN;
			rental.returnDate = new Date();
			Ebean.update(rental);
			
			Book book = rental.book;
			book.status = BookStatus.AVAILABLE;
			Ebean.update(book);
			
			Ebean.commitTransaction();
			Ebean.endTransaction();
			
			result.put("status", true);
			return ok(result);
		}		
		else {
			result.put("status", false);
			result.put("error", "Rental not found!");
			return ok(result);
		}
		
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
		
		List<RentalForm> output = new ArrayList<RentalForm>();
		
		for (Book book : bookList) {	
			if (!user.containBook(book.isbn)) {
				Rental rental = new Rental(book);	
				if (user.rentBook(rental)) {
					book.status = BookStatus.RENT;
					Ebean.update(book);
					
					RentalForm form = new RentalForm();			
					form.id = book.id;
					form.isbn = book.isbn;
					form.name = book.name;	
					output.add(form);
				}				
				else {
					Ebean.rollbackTransaction();
					Ebean.endTransaction();
					
					result.put("status", false);
					result.put("error", "User rent book over limit");
					return ok(result);
				}
			}						
		}
		
		Ebean.update(user);
		Ebean.commitTransaction();
		Ebean.endTransaction();		
		
		result.put("status", true);
		result.put("output", Json.toJson(output));
		return ok(result);
		
	}
  
}