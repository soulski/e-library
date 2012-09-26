package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.annotation.EnumValue;

@Entity
public class Book extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	public String isbn;
	public String name;
	public String auther;
	public int publishYear;
	public BookStatus status;
	public Date createDate;
	
	public static Finder<String, Book> find = new Finder<String, Book>(String.class, Book.class);
	
	public enum BookStatus {
		@EnumValue("AVAILABLE")
		AVAILABLE,
		@EnumValue("RENT")
		RENT,
		@EnumValue("RENEW")
		RENEW,
		@EnumValue("BUYING_PROCESSING")
		BUYING_PROCESSING
	}
}
