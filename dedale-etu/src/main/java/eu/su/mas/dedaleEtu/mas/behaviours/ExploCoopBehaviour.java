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

	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;
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
				//this.myAgent.doWait(50);
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
			if (!this.myMap.hasOpenNode()){
				if (!((ExploreCoopAgent)this.myAgent).finish) System.out.println(this.myAgent.getLocalName()+" - Exploration successufully done, behaviour removed.");
				((ExploreCoopAgent)this.myAgent).finish = true ;
			}
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					String tmpPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
						if(!((ExploreCoopAgent)this.myAgent).finish) {
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
							if(tmpPos==null) {
								String nodeGoal = ((ExploreCoopAgent)this.myAgent).randGoalNode;
								List<String> closednodes=this.myMap.getClosedNodes();
								Random r = new Random();

								while(nodeGoal.equals("") || myPosition.equals(nodeGoal)) {
									nodeGoal = closednodes.get(r.nextInt(closednodes.size()));
								}
								int rand=((ExploreCoopAgent)this.myAgent).getRandom();
								if(rand>0) {
									if(closednodes.size()>0) {
									try {
										nextNode = this.myMap.getShortestPath(myPosition,closednodes.get(r.nextInt(closednodes.size()))).get(0);
									}
									catch(Exception ex) {
									}
									}
									((ExploreCoopAgent)this.myAgent).setRandom(rand-1);
								}
								else
									try{
										nextNode = this.myMap.getShortestPath(myPosition, nodeGoal).get(0);
									}
									catch(Exception ex) {
										nextNode = this.myMap.getShortestPath(myPosition,closednodes.get(r.nextInt(closednodes.size()))).get(0);
									}
								if(nextNode!=null && nextNode.equals(nodeGoal)) {
									nodeGoal = "";
								}
								((ExploreCoopAgent)this.myAgent).randGoalNode =  nodeGoal ;
								}
							else {
								try {
									nextNode = this.myMap.getShortestPath(myPosition,tmpPos).get(0);
									}
									catch(Exception ex) {
									}
							}
							}
					
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				this.myAgent.addBehaviour(new PingBehaviour(this.myAgent,list_agentNames));
				this.myAgent.addBehaviour(new UpdateWumpusBehaviour(this.myAgent));
				this.myAgent.addBehaviour(new IAmHereBehaviour(this.myAgent,list_agentNames,this.myMap));
				
				String nearAgent = ((ExploreCoopAgent)this.myAgent).nearAgent ;
				String tmpPos = ((ExploreCoopAgent)this.myAgent).getWumpusPos();
				
				if (!((ExploreCoopAgent)this.myAgent).mov && tmpPos!=null) 
					if (nearAgent!=null && nextNode!=null && nextNode.compareTo(nearAgent)==0)
					{
						((ExploreCoopAgent)this.myAgent).smell = false ; 
						((ExploreCoopAgent)this.myAgent).wumpusFound = false ;
						((ExploreCoopAgent)this.myAgent).setWumpusPos(null) ;
					}
					else if(nextNode!=null && nextNode.compareTo(tmpPos)==0){
						((ExploreCoopAgent)this.myAgent).wumpusFound = true ; 
					}
				if (nearAgent!=null && nextNode!=null && tmpPos != null && ((ExploreCoopAgent)this.myAgent).finish) {
					nextNode = this.myMap.getPathWithoutPassingByNearAgents(nearAgent,tmpPos ,myPosition);
				}
				if (nextNode!=null) ((ExploreCoopAgent)this.myAgent).mov = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				else ((ExploreCoopAgent)this.myAgent).mov = false ;
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
