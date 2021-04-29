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
public class PingBehaviour extends SimpleBehaviour{

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
	public PingBehaviour (final Agent myagent , List<String> receivers ) {
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
		msg.setProtocol("Ping");

		if (myPosition!=""){
			msg.setContent(myPosition);
			for (String agentName : receivers) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}			

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