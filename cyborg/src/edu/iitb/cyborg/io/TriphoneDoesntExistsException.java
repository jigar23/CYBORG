package edu.iitb.cyborg.io;

/**
 * Signals a triPhone not found exception in case expected triphone is not available in mdef training data.
 * 
 * @author  : Nikul Prajapati
 * @contact : nikulprajapati90@gmai.com
 * 
 */


public class TriphoneDoesntExistsException extends Exception{
	public TriphoneDoesntExistsException(String s) {
		super(s);
	}
}
