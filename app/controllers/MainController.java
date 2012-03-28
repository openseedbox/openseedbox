package controllers;

import code.MessageException;

public class MainController extends BaseController {

	public static void index() throws MessageException {
		ClientController.index();
	}

}
