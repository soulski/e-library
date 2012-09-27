package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.annotation.Where;

import models.Rental.RentalStatus;

import play.Logger;
import play.db.ebean.Model;
import play.data.validation.Constraints;

@Entity
public class User extends Model {	
	@Id
	public String id;
	@Constraints.Required
	public String name;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Where(clause = "status = 'RENTAL'")
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
	
	public boolean rentBook(Rental rental) {
		Logger.debug("User type : " + type.toString());
		Logger.debug("size type : " + rentals.size());		
		if (type.equals(UserType.STUDENT) && rentals.size() >= 3) {			
			return false;
		}
		if (type == UserType.EMPLOYEE && rentals.size() >= 5) {
			return false;
		}
		
		return rentals.add(rental);
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
