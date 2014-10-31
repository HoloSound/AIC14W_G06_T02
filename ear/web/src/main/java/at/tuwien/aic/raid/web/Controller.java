package at.tuwien.aic.raid.web;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface;

public class Controller {
	private RaidSessionBeanInterface raid;

	public Controller() {

		InitialContext ctx;
		try {
			ctx = new InitialContext();
			//all beans have to be "injected" here the JNDI bindings name is displayed on deployment
			this.raid = (RaidSessionBeanInterface) ctx.lookup("java:global/ear/sessionBean/RaidSessionBean");
	
		} catch (NamingException e) {
			
			e.printStackTrace();
			System.out.println("ERROR CANT INIT CONTROLLER ");
		}

	}

	public String sayHello() {
		return "<h1>" + raid.sayHello() + "</h1>";
	}
}
