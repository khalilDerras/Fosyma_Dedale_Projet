package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;


import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class UpdateWumpusBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	private List<String> receivers ;
	private boolean finished = false;


	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public UpdateWumpusBehaviour (final Agent myagent) {
		super(myagent); //String []reciver en parametre
		//super(myagent);
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if (myPosition!=""){
			String wumpusPoss = ((ExploreCoopAgent)this.myAgent).getStench2();
			String wumpusPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
			if(((ExploreCoopAgent)this.myAgent).wumpusFound) wumpusPoss = wumpusPos ;
			if(wumpusPos!=null && myPosition.compareTo(wumpusPos)==0) {
				if (wumpusPoss==null) ((ExploreCoopAgent)this.myAgent).setWumpusPos(null) ;
				((ExploreCoopAgent)this.myAgent).smell = true ;
			}
			else if(wumpusPos==null) ((ExploreCoopAgent)this.myAgent).smell = true ;
			if(((ExploreCoopAgent)this.myAgent).smell) if(wumpusPoss!=null) ((ExploreCoopAgent)this.myAgent).setWumpusPos(wumpusPoss);
			if(wumpusPoss == null) {
				((ExploreCoopAgent)this.myAgent).wumpusFound = false ;
				((ExploreCoopAgent)this.myAgent).smell = true ;
			}		
		}
		finished=true;

	}
	@Override
	public boolean done() {
		return finished;
	}
}