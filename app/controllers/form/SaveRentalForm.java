package controllers.form;

import java.util.List;

import play.data.validation.Constraints.Required;

public class SaveRentalForm {
	@Required
	public String userId;
	@Required
	public List<String> bookList;
}
