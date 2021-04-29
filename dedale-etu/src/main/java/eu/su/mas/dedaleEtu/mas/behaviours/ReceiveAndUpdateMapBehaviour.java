package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

public class ReceiveAndUpdateMapBehaviour extends SimpleBehaviour {
	private boolean finished = false;
	private MapRepresentation myMap;


	public ReceiveAndUpdateMapBehaviour(Agent myAgent,MapRepresentation mymap) {
		super(myAgent);
		this.myMap=mymap;
	}

	@Override
	public void action() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(sgreceived!=null) {
				this.myMap.mergeMap(sgreceived);
			}
			//System.out.println(this.myAgent.getLocalName()+" Map Recieved And Updated");
			

		}
		finished = true ;
		
	}

	@Override
	public boolean done() {
		return finished ;
	}

}
