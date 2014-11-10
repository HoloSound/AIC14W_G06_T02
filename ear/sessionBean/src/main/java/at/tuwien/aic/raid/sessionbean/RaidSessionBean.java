package at.tuwien.aic.raid.sessionbean;

import javax.ejb.Stateless;

import at.tuwien.aic.raid.Raid1;

@Stateless()
public class RaidSessionBean implements RaidSessionBeanInterface {
	
	private Raid1 raid1;

	public RaidSessionBean() {
		raid1=new Raid1();
	}

	@Override
	public String sayHello() {
		//TODO REMOVE  EXAMPLE CODE
		return raid1.sayHello();
	}
	
	
}
