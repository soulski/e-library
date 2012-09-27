package models;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import models.User.UserType;

import com.avaje.ebean.annotation.EnumValue;

import play.Logger;
import play.db.ebean.Model;

@Entity
public class Rental extends Model {
	public static final long MINLISECOND_DATE = 1000 * 60 * 60 * 24;
	
	@Id	
	public Long id;
	@OneToOne
	public Book book;
	public Date rentalDate;
	public Date returnDate;
	public RentalStatus status;
	
	public static Finder<String, Rental> find = new Finder<String, Rental>(String.class, Rental.class);
	
	public Rental(Book book) {
		this.book = book;		
		this.status = RentalStatus.RENTAL;
		this.rentalDate = new Date();
	}
	
	public Date getDueDate(User user) {
		int maxRentDate = 0;
		if (user.type.equals(UserType.STUDENT)) {
			maxRentDate = 10;
		}
		else if (user.type.equals(UserType.EMPLOYEE)) {
			maxRentDate = 30;
		}
		else if (user.type.equals(UserType.PROFESSOR)) {
			maxRentDate = 120;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(rentalDate);
		calendar.add(Calendar.DATE, maxRentDate);
		return calendar.getTime();
	}
	
	public int getFine(User user) {
		Date dueDate = getDueDate(user);
		Date currentDate = new Date();		
		long diff = currentDate.getTime() - dueDate.getTime();
		int diffDate = (int) (diff / MINLISECOND_DATE);		
		
		if (diffDate <= 0) {
			return 0;
		}
		
		return diffDate * 3; 
	}
	
	public enum RentalStatus {
		@EnumValue("RENTAL")
		RENTAL, 
		@EnumValue("RETURN")
		RETURN
	}
}
