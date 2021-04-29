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
public class SayHelloBehaviour extends SimpleBehaviour{

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
	public SayHelloBehaviour (final Agent myagent , List<String> receivers ) {
		super(myagent); //String []reciver en parametre
		this.receivers=receivers;	
		//super(myagent);
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("HelloProtocol");

		if (myPosition!=""){
			String wumpusPoss = ((ExploreCoopAgent)this.myAgent).getStench2();
			String wumpusPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
			if(((ExploreCoopAgent)this.myAgent).wumpusFound) wumpusPoss = wumpusPos ;
			//if (wumpusPoss != null ) System.out.println(this.myAgent.getLocalName() +" found golem");
			if(wumpusPos!=null && myPosition.compareTo(wumpusPos)==0) {
				if (wumpusPoss==null) ((ExploreCoopAgent)this.myAgent).setWumpusPos(null) ;
				((ExploreCoopAgent)this.myAgent).nearestOrUknown = true ;
			}
			else if(wumpusPos==null) ((ExploreCoopAgent)this.myAgent).nearestOrUknown = true ;
			if(((ExploreCoopAgent)this.myAgent).nearestOrUknown) if(wumpusPoss!=null) ((ExploreCoopAgent)this.myAgent).setWumpusPos(wumpusPoss);
			if(wumpusPoss == null) {
				((ExploreCoopAgent)this.myAgent).wumpusFound = false ;
				((ExploreCoopAgent)this.myAgent).nearestOrUknown = true ;
			}
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			msg.setContent(myPosition);
			for (String agentName : receivers) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			//System.out.println(this.myAgent.getLocalName() + " is checking if anybody near him");
			

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			//((ExploreCoopAgent)this.myAgent).lastStenches = wumpusPoss ;
		}
		finished=true;

	}
	@Override
	public boolean done() {
		return finished;
	}
}