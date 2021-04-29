package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
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
		final MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchProtocol("Ping"), 
				MessageTemplate.MatchPerformative(ACLMessage.INFORM) );	

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		if (msg != null) {
			((ExploreCoopAgent)this.myAgent).setRandom(2);
			String c = msg.getContent();
			((ExploreCoopAgent)this.myAgent).nearAgent = c ;
			((ExploreCoopAgent)this.myAgent).nearAgents.replace(msg.getSender().getLocalName(), c);
			if(!((ExploreCoopAgent)this.myAgent).finish) {
				this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, this.myMap, this.receivers,msg.getSender()));
				this.myAgent.addBehaviour(new receiveAndUpdateMapBehaviour(this.myAgent,this.myMap));
			}
			else this.myAgent.addBehaviour(new ShareWumpusBehaviour(this.myAgent, this.myMap, this.receivers));

		}
		finished=true;

}
	@Override
	public boolean done() {
		return finished;
	}
}