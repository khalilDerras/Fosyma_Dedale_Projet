package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class IAmHereBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	private List<String> receivers ;
	private MapRepresentation myMap;
	private boolean finished = false;



	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public IAmHereBehaviour (final Agent myagent , List<String> receivers , MapRepresentation myMap ) {
		super(myagent); //String []reciver en parametre
		this.receivers=receivers;	
		this.myMap = myMap ; 
	}

	@Override
	public void action() {
		final MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("HelloProtocol"), 
				MessageTemplate.MatchPerformative(ACLMessage.INFORM) );	

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			((ExploreCoopAgent)this.myAgent).setRandom(2);
			String c = msg.getContent();
			String t = ((ExploreCoopAgent)this.myAgent).getWumpusPos() ;
			if (t != null && c!=null) 
				if (c.compareTo(t)==0) {
					System.out.println("ooh he's already there");
					((ExploreCoopAgent)this.myAgent).setWumpusPos(null); //predict
				}
			//System.out.println(this.myAgent.getLocalName()+"Received response from "+msg.getSender().getLocalName());
			this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, this.myMap, this.receivers));
			this.myAgent.addBehaviour(new receiveAndUpdateMapBehaviour(this.myAgent,this.myMap));
			this.myAgent.addBehaviour(new ShareWumpusBehaviour(this.myAgent, this.myMap, this.receivers));

		}
		finished=true;

}
	@Override
	public boolean done() {
		return finished;
	}
}