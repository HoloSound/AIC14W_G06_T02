package at.tuwien.aic.raid.sessionbean;

import javax.ejb.Stateless;

@Stateless()
public class RaidSessionBean implements RaidSessionBeanInterface {

	@Override
	public String sayHello() {
		//TODO REMOVE  EXAMPLE CODE
		return "Hello world from RaidSessionBean";
	}
	
	
}
