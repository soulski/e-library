package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.EnumValue;

import models.Rental.RentalStatus;

import play.db.ebean.Model;
import play.data.validation.Constraints;

@Entity
public class User extends Model {	
	@Id
	public String id;
	@Constraints.Required
	public String name;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	public List<Rental> rentals = new ArrayList<Rental>();
	public UserType type;
	
	public static Finder<String, User> find = new Finder<String, User>(String.class, User.class);
	
	public boolean containBook(String isbn) {
		for (Rental rental : rentals) {
			if (rental.book.isbn.equals(isbn) && rental.status == RentalStatus.RENTAL) {
				return true;
			}
		}
		
		return false;
	}
	
	public enum UserType {
		@EnumValue("STUDENT")
		STUDENT, 
		@EnumValue("PROFESSOR")
		PROFESSOR,
		@EnumValue("EMPLOYEE")
		EMPLOYEE
	}
}
