package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.IAmHereBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private int random = 0 ;
	private List<Behaviour> lb;
	private String wumpusPos = null;
	public  Map<String,String> nearAgents = null; // contains the positions of the agents our agent meet
	public String lastPos = null ;
	public String nearAgent = null;
	public boolean mov = true;
	public boolean nearestOrUknown = true;
	public boolean wumpusFound = false;
	public String randGoalNode = "";
	public HashMap<String,SerializableSimpleGraph<String,MapAttribute>> mapSendedMemory = new HashMap<String,SerializableSimpleGraph<String,MapAttribute>>();


	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		final Object[] args = getArguments();
		
		//List<String> list_agentNames=new ArrayList<String>();
		List<String> list_agentNames = getAgentsList();
		if(nearAgents==null) nearAgents = new HashMap<String,String>();
		for (String a:list_agentNames) nearAgents.put(a, null);
		/*
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		*/
		lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		lb.add(new ExploCoopBehaviour(this,list_agentNames));



		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new startMyBehaviours(this,lb));
		//add behaviour say hello   list agent name 
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	private List <String> getAgentsList(){
		AMSAgentDescription [] agentsDescriptionCatalog = null;
		List <String> agentsNames= new ArrayList<String>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults (Long.valueOf(-1)  );
			agentsDescriptionCatalog = AMSService.search(this, new AMSAgentDescription(), c );
		}
		catch (Exception e) {
			System.out. println ( "Problem searching AMS: " + e );
			e.printStackTrace () ;
		}
		for ( int i=0; i<agentsDescriptionCatalog.length ; i++){
		AID agentID = agentsDescriptionCatalog[i ]. getName();
		if(agentID.compareTo(this.getAID()) != 0) {
			agentsNames.add(agentID.getLocalName());
		}
		}
		return agentsNames;
	}

	public int getRandom() {
		return random;
	}

	public void setRandom(int random) {
		this.random = random;
	}

	public List<Behaviour> getLB(){
		return this.lb;
	}
	public String getStench2() {
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=this.observe();//myPosition
		lobs.remove(0);
		List<String> stenches = new ArrayList<String>();
		for(Couple<String,List<Couple<Observation,Integer>>> po:lobs){
			for(Couple<Observation,Integer> o:po.getRight()) 
				if(o.getLeft().equals(Observation.STENCH)) 
					stenches.add(po.getLeft());
			}
		if(stenches.size()>1) {
			if (stenches.contains(lastPos)) stenches.remove(lastPos);
			Random r = new Random();
			return stenches.get(r.nextInt(stenches.size()));
		}
		else if (stenches.size()==1) return stenches.get(0); 
		return null;		
	}
	public String getStench() {
		
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs=this.observe();//myPosition
		int i = 0 ;
		boolean Stench = false;
		String first = null ;
		for(Couple<String,List<Couple<Observation,Integer>>> po:lobs){
			for(Couple<Observation,Integer> o:po.getRight()) {
				if(i==0) {
					Stench = o.getLeft().equals(Observation.STENCH) ;
					first = po.getLeft();
				}
				else
				if(o.getLeft().equals(Observation.STENCH)) {//aleatoire et //lastpos
					if (lastPos!= null ) {
						if (Stench) {
							if(po.getLeft().compareTo(lastPos)!=0) {
								//System.out.println(lastPos + "rani f stench w place jdida tssma nro7");
								return po.getLeft() ;
						}
							else if (this.observe().size() == 2) return po.getLeft() ;
						} 
						else return po.getLeft() ;
						}					
					else return po.getLeft() ;	
				}
			}
			i++;
		}
		return null;		
	}
	public String getWumpusPos() {
		return wumpusPos;
	}

	public void setWumpusPos(String wumpusPos) {
		this.wumpusPos = wumpusPos;
	}

	public boolean isOnStench() {
		String wumpusPos = this.getWumpusPos();
		String myPosition=((AbstractDedaleAgent)this).getCurrentPosition();
		if (wumpusPos == null) return false;
		else {
			return myPosition.compareTo(wumpusPos)==0;
		}
	}
	
}
