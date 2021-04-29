package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.HashMap;
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

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ShareMapBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> receivers;
	private boolean finished;
	private AID sender;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ShareMapBehaviour(Agent a, MapRepresentation mymap, List<String> receivers,AID sender) {
		super(a);
		this.myMap=mymap;
		this.receivers=receivers;
		this.sender = sender ;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
		// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.
		AID agentName = sender ;
		//for (String agentName : receivers) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SHARE-TOPO");
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(agentName);
			
			SerializableSimpleGraph<String,MapAttribute> sgToSend = this.myMap.getSerializableGraph();
			SerializableSimpleGraph<String,MapAttribute> oldMap =((ExploreCoopAgent)this.myAgent).mapSendedMemory.get(agentName.getLocalName());
			if( oldMap != null) {
				sgToSend = this.myMap.difference(oldMap);
				
			}
			((ExploreCoopAgent)this.myAgent).mapSendedMemory.put(agentName.getLocalName() , this.myMap.getSerializableGraph());			

			try {	
				if (!(sgToSend.getAllNodes().isEmpty())) {
					msg.setContentObject(sgToSend);
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
				/*else {
					msg.setContentObject(this.myMap.getSerializableGraph());
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}*/
			} catch (IOException e) {
				e.printStackTrace();
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			//System.out.println(this.myAgent.getLocalName()+" Map Sended");
		//}
		finished = true;

		
	}

	@Override
	public boolean done() {
		return finished;
	}

}
