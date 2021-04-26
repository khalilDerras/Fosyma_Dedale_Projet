package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.HashMap;

import com.sun.javafx.collections.MappingChange.Map;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;



/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author hc
 *
 */
public class ExploCoopBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
    

	private boolean finished = false;
	private String nodeGoal = "";


	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
	private HashMap<String,SerializableSimpleGraph<String,MapAttribute>> mapSendedMemory = new HashMap<String,SerializableSimpleGraph<String,MapAttribute>>();

	private List<String> list_agentNames;

/**
 * 
 * @param myagent
 * @param myMap known map of the world the agent is living in
 * @param agentNames name of the agents to share the map with
 */
	public ExploCoopBehaviour(final AbstractDedaleAgent myagent,List<String> agentNames) {
		super(myagent);
		this.list_agentNames=agentNames;
		//this.list_agentNames=getAgentsList();
		
		
		
	}
	
	@Override
	public void action() {
		boolean mov = true ;
	    
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		}
		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(50);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			boolean nonfinish = true ;
			if (!this.myMap.hasOpenNode()){
				nonfinish = false ;
				//Explo finished
				/*List<Behaviour> lb = ((ExploreCoopAgent)this.myAgent).getLB();
			    for (Behaviour b : lb) {
			    	if (! b.getBehaviourName().equals("ExploCoopBehaviour")) {
			    		System.out.println(b.getBehaviourName());
			    		this.myAgent.removeBehaviour(b);
			    	}
			      }*/
				//finished=true;
				System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
			}
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					String tmpPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
					if(tmpPos  == null) {
						if(nonfinish) {
						int rand=((ExploreCoopAgent)this.myAgent).getRandom();
						if(rand>0) {
							Random r = new Random();
							List<String> openNodes = this.myMap.getOpenNodes();
							if(openNodes.size()>0) {
							try {
								nextNode = this.myMap.getShortestPath(myPosition,openNodes.get(r.nextInt(openNodes.size()))).get(0);
							}
							catch(Exception ex) {
								 nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);
							}
							}
							((ExploreCoopAgent)this.myAgent).setRandom(rand-1);
						}
						else nextNode=this.myMap.getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					}
						else {
							while(nodeGoal.equals("") || myPosition.equals(nodeGoal)) {
								List<String> closednodes=this.myMap.getClosedNodes();
								Random rand = new Random();
								nodeGoal = closednodes.get(rand.nextInt(closednodes.size()));
								System.out.println(this.myAgent.getLocalName()+" ---> Init a new nodeGoal("+nodeGoal+") to search Golem");
							}
							nextNode = this.myMap.getShortestPath(myPosition, nodeGoal).get(0);
							if(nextNode.equals(nodeGoal)) {
								nodeGoal = "";
							}
						}
					}
					else {
						//System.out.println(this.myAgent.getLocalName()+"Hunting "+tmpPos);
						try {
						nextNode = this.myMap.getShortestPath(myPosition,tmpPos).get(0);
						}
						catch(Exception ex) {
							mov = false ;
							/*if(((ExploreCoopAgent)this.myAgent).nearAgent != null && tmpPos.compareTo(((ExploreCoopAgent)this.myAgent).nearAgent)==0) System.out.println(this.myAgent.getLocalName() +" l9ito bss7 mechi howa");
							else {
								System.out.println(this.myAgent.getLocalName() +" l9ito howa");
								mov = false ;
							}*/
						}
					}
					//List<String> openNodes =this.myMap.getOpenNodes();
					//Random r = new Random();
					//nextNode=this.myMap.getShortestPath(myPosition,openNodes.get(r.nextInt(openNodes.size()))).get(0);
					
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				//4) At each time step, the agent blindly send all its graph to its surrounding to illustrate how to share its knowledge (the topology currently) with the the others agents. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set, the receivers be automatically computed, and only a subgraph would be shared.

//				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//				msg.setProtocol("SHARE-TOPO");
//				msg.setSender(this.myAgent.getAID());
//				if (this.myAgent.getLocalName().equals("1stAgent")) {
//					msg.addReceiver(new AID("2ndAgent",false));
//				}else {
//					msg.addReceiver(new AID("1stAgent",false));
//				}
//				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
//				try {					
//					msg.setContentObject(sg);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

				//5) At each time step, the agent check if he received a graph from a teammate. 	
				// If it was written properly, this sharing action should be in a dedicated behaviour set.
				/*MessageTemplate msgTemplate=MessageTemplate.and(
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
					//this.list_agentNames.indexOf(this.myAgent.getName());
					//System.out.println("indice"+this.list_agentNames.indexOf(msgReceived.getSender().getName()));

					//list<String> openNodes =this.sgreceived.getOpenNodes();
					this.myMap.mergeMap(sgreceived);
					//this.mapSendedMemory.put(msgReceived.getSender().getLocalName(),sgreceived);
				}*/
				this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent,list_agentNames));
				this.myAgent.addBehaviour(new IAmHereBehaviour(this.myAgent,list_agentNames,this.myMap));
				String tmp =((ExploreCoopAgent)this.myAgent).lastPos ;
				String nearAgent = ((ExploreCoopAgent)this.myAgent).nearAgent ;
				String tmpPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();

				if (mov) ((ExploreCoopAgent)this.myAgent).mov = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				else ((ExploreCoopAgent)this.myAgent).mov = false ;
				
				if (!((ExploreCoopAgent)this.myAgent).mov && ((ExploreCoopAgent)this.myAgent).getWumpusPos()!=null) 
					if (nearAgent!=null && nextNode!=null && nextNode.compareTo(nearAgent)!=0) {
						((ExploreCoopAgent)this.myAgent).wumpusFound = true ; 
						System.out.println(this.myAgent.getLocalName()+"rani hnaa"+tmpPos); 
					}
					else if (nearAgent!=null && nextNode!=null && nextNode.compareTo(nearAgent)==0)
					{
						((ExploreCoopAgent)this.myAgent).nearestOrUknown = false ; 
						if(tmpPos.compareTo(nextNode)!=0) {
						MapRepresentation tmpMap = null;
						try {
							tmpMap = (MapRepresentation) this.myMap.clone();
						} catch (CloneNotSupportedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Iterator<Edge> iterE=tmpMap.g.edges().iterator();
						while (iterE.hasNext()){
							Edge e=iterE.next();
							if (e==null) continue;
							String sn=e.getSourceNode().getId();			
							String tn=e.getTargetNode().getId();
							if(sn.compareTo(nearAgent)==0 || tn.compareTo(nearAgent)==0) {
								tmpMap.removeEdge(sn, tn);
							}	
						}

						try {
							nextNode = tmpMap.getShortestPath(myPosition,tmpPos).get(0);
							((ExploreCoopAgent)this.myAgent).mov = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
							System.out.println(this.myAgent.getLocalName()+"l9it triiiiiiiiiiiiiiiiiiiiii9"); 
						}
						catch(Exception e) {
							System.out.println(this.myAgent.getLocalName()+"mal9itech tri9"); 
						}
						System.out.println(this.myAgent.getLocalName()+"cha rak dirr" + tmpPos); 
					}
					}
				if (((ExploreCoopAgent)this.myAgent).mov) {
					((ExploreCoopAgent)this.myAgent).wumpusFound = false ;
					((ExploreCoopAgent)this.myAgent).lastPos= myPosition ;
				}

		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
