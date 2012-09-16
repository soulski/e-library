package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.avaje.ebean.annotation.EnumValue;

import play.db.ebean.Model;

@Entity
public class Rental extends Model {
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
	
	public enum RentalStatus {
		@EnumValue("RENTAL")
		RENTAL, 
		@EnumValue("RETURN")
		RETURN
	}
}
