package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareWumpusBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> receivers;
	private boolean finished;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareWumpusBehaviour(Agent a, MapRepresentation mymap, List<String> receivers) {
		super(a);
		this.myMap=mymap;
		this.receivers=receivers;	
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	
	public void sendMessage() {
		//Example to retrieve the current position		
			String wumpusPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
			String tmp =((ExploreCoopAgent)this.myAgent).getStench() ;					
			if ( wumpusPos != null) {
				//System.out.println(this.myAgent.getLocalName()+"Wumpus Sent");
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("Wumpus");
				msg.setContent(wumpusPos);
				
				for (String agentName : receivers) {
					msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
				//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
			

	}
	@Override
	public void action() {
		if(((ExploreCoopAgent)this.myAgent).wumpusFound) sendMessage(); //envoyer sauf si l'agent connait la position r??elle de wumpus
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("Wumpus"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			String wumpusPos = msgReceived.getContent();
			if(!((ExploreCoopAgent)this.myAgent).smell && !((ExploreCoopAgent)this.myAgent).wumpusFound) ((ExploreCoopAgent)this.myAgent).setWumpusPos(wumpusPos); //le conditon est pour pouvoir aller ?? l'autre cot?? de wumpus indiqu?? par le sender sans changer d'objectif
		}
		finished = true;	
	}

	@Override
	public boolean done() {
		return finished;
	}

}
