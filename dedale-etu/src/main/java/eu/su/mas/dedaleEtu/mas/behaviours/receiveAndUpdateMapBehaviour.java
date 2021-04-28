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

public class receiveAndUpdateMapBehaviour extends SimpleBehaviour {
	private boolean finished = false;
	private MapRepresentation myMap;
	private HashMap<String,SerializableSimpleGraph<String,MapAttribute>> mapSendedMemory ;


	public receiveAndUpdateMapBehaviour(Agent myAgent,MapRepresentation mymap, HashMap<String,SerializableSimpleGraph<String,MapAttribute>> mapSendedMemory) {
		super(myAgent);
		this.myMap=mymap;
		this.mapSendedMemory = mapSendedMemory ;

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
			System.out.println(this.myMap.getClosedNodes().size()+"psp");
			this.myMap.mergeMap(sgreceived);
			this.mapSendedMemory.put(msgReceived.getSender().getLocalName() , this.myMap.getSerializableGraph());			
			//System.out.println(this.myAgent.getLocalName()+" Map Recieved And Updated");
			System.out.println(this.myMap.getClosedNodes().size()+ "dsd");

		}
		finished = true ;
		
	}

	@Override
	public boolean done() {
		return finished ;
	}

}
