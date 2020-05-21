package project;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.text.DecimalFormat;
import bridges.base.GraphAdjListSimple;
import bridges.base.Edge;
import bridges.connect.Bridges;
import bridges.validation.RateLimitException;

public class Solution {
	private int roomCt;
	private int connectCt;
	private String enter;
	GraphAdjListSimple<String> graph;
	private HashMap<String, Room> rooms;
	private HashMap<String, Objective> objectives;
	private LinkedList<String> orderObjectives;
	Bridges bridges;
	//Decimal formats used for rounding and truncation
	private static DecimalFormat df = new DecimalFormat("0.00");
	private static DecimalFormat median = new DecimalFormat("0.0");

	//Constructor initializes values
	public Solution() {
		this.roomCt = 0;
		graph = new GraphAdjListSimple<String>();
		bridges = new Bridges(117, "wagnerb", "31895118076");
		this.rooms = new HashMap<>();
		this.connectCt = 0;
		this.enter = "";
		this.objectives = new HashMap<>();
		this.orderObjectives = new LinkedList<>();
	}

	//Creates visualization of graph using bridges
	private void visualize(int id, String title, GraphAdjListSimple graph) throws IOException, RateLimitException {
		bridges.setTitle("Dungeons and Djikstra's");
		bridges.setDescription(title);
		bridges.setAssignment(id);
		bridges.setDataStructure(graph);
		bridges.visualize();
	}

	//Reads file from machine
	public static List<String> readFile(String filename) {
		List<String> records = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				records.add(line);
			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
		}
		return records;

	}
	
	//Takes list of text lines from files
	//and organizes lines by room, connection, or
	//objective.
	public void load(List<String> dungeon) {
		for (String s : dungeon) {
			String[] line = s.split(",");
			switch (line[0]) {
			case "room":
				Room temp = new Room(line[1], Integer.valueOf(line[2]), Arrays.asList(line[3].split("\\+")), this.roomCt);
				this.roomCt++;
				graph.addVertex(line[1], "");
				this.rooms.put(line[1], temp);
				break;
			case "connection":
				this.connectCt++;
				this.rooms.get(line[1]).addConnection();
				this.rooms.get(line[2]).addConnection();
				graph.addEdge(line[1], line[2]);
				graph.getLinkVisualizer(line[1], line[2]).setLabel(Integer.toString(this.rooms.get(line[2]).challenge));
				graph.addEdge(line[2], line[1]);
				graph.getLinkVisualizer(line[2], line[1]).setLabel(Integer.toString(this.rooms.get(line[1]).challenge));
				graph.setEdgeData(line[1], line[2], Integer.toString(this.rooms.get(line[2]).getChallenge()));
				graph.setEdgeData(line[2], line[1], Integer.toString(this.rooms.get(line[1]).getChallenge()));
				break;
			case "objective":
				String r = "";
				for(String key : this.rooms.keySet()) {
					if (this.rooms.get(key).getObjectives().contains(line[1])) {
						r = this.rooms.get(key).getName();
						break;
					}
				}
				Objective temp2 = new Objective(line[1], Arrays.asList(line[2].split("\\+")), r);
				this.objectives.put(line[1], temp2);
				break;
			}
		}
		this.loadOrder();
	}

	//Problem 5
	//Counts Dead Ends in the graph.
	public int ctDeads() {
		int count = 0;
		Set<String> keys = this.rooms.keySet();
		for (String s : keys) {
			if (this.rooms.get(s).getConnections() == 1) {
				count++;
			}
		}
		return count;
	}
	
	//Problem 6
	//Counts Hubs in the graph.
	public int ctHubs() {
		int count = 0;
		Set<String> keys = this.rooms.keySet();
		for (String s : keys) {
			if (this.rooms.get(s).getConnections() >= 3) {
				count++;
			}
		}
		return count;
	}

	//Problem 8
	//Determines Maximum Challenge
	public int getMax() {
		int max = 0;
		Set<String> keys = this.rooms.keySet();
		for (String s : keys) {
			int temp = this.rooms.get(s).getChallenge();
			if (temp > max) {
				max = temp;
			}
		}
		return max;
	}

	//Problem 9
	//Determines Median Challenge
	public float getMedian() {
		ArrayList<Integer> chal = new ArrayList<>();
		Set<String> keys = this.rooms.keySet();
		int t = keys.size();
		for (String s : keys) {
			chal.add(this.rooms.get(s).getChallenge());
		}
		Collections.sort(chal);
		if (t%2 == 1) {
			return chal.get(t/2);
		}
		else {
			return ((float)chal.get(t/2)+chal.get(t/2-1))/2;
		}
	}

	//Used in Visualization for problem 10
	//Sets shapes of graph based on entrance and objectives
	public void editGraph() {
		for (String key : this.rooms.keySet()) {
			Room temp = this.rooms.get(key);
			if (temp.ct == 0) {
				this.enter = temp.name;
				graph.getVertex(temp.name).setShape("Star");
			} else if (!temp.obectives.contains("none")) {
				graph.getVertex(temp.name).setShape("Triangle");
			}
		}
	}

	//Problem 11 Helper Function
	//Creates HashMap of room name keys and boolean values
	//Is used in valid function
	public HashMap<String, Boolean> validityMap() {
		HashMap<String, Boolean> validity = new HashMap<>();
		Set<String> keys = this.rooms.keySet();
		for (String k : keys) {
			validity.put(k, false);
		}
		return validity;
	}

	//Problem 11
	//Determines if a given graph is valid
	//Traverses through vertices and adds to HashMap
	//If any vertice has a similar difficulty leading
	//up to it, the graph is considered invalid.
	public boolean valid(String r, HashMap<String, Boolean> v) {
		v.put(r, true);
		Iterable<Edge<String, String>> edges = graph.outgoingEdgeSetOf(r);
		for (Edge<String, String> e : edges) {
			if (v.get(e.getTo()) == false) {
				valid(e.getTo(), v);
			}
		}
		if (v.containsValue(false)) {
			return false;
		} else {
			return true;
		}
	}

	//Used in Visualization for problem 13
	//Sets colors based on difficulty and median
	public void addDifficulty(float med) {
		for (String r : this.rooms.keySet()) {
			if (this.rooms.get(r).getChallenge() < med) {
				graph.getVertex(r).setColor("Blue");
			} else {
				graph.getVertex(r).setColor("Red");
			}
		}
	}

	//Problem 14
	//Determines if the graph is balanced
	public boolean balance(String r, HashMap<String, Boolean> v, float med) {
		boolean bal = true;
		v.put(r, true);
		Iterable<Edge<String, String>> edges = graph.outgoingEdgeSetOf(r);
		for (Edge<String, String> e : edges) {
			if ((this.rooms.get(r).getChallenge() < med && this.rooms.get(e.getTo()).getChallenge() < med)
					|| (this.rooms.get(r).getChallenge() >= med && this.rooms.get(e.getTo()).getChallenge() >= med)) {
				return false;
			}
			if (v.get(e.getTo()) == false) {
				bal = balance(e.getTo(), v, med);
				if (bal == false) {
					return false;
				}
			}
		}
		return true;
	}

	//Problem 16 Helper Method
	//Determines if Objective next has Objective check
	//as a prerequisite or directly or even further down
	//the line.
	public boolean cont(Objective next, Objective check) {
		if (next.getReq().contains("none")) {
			return false;
		}
		if (next.contains(check)) {
			return true;
		} else {
			for (String s : next.getReq()) {
				if (this.cont(this.objectives.get(s), check)) {
					return true;
				}
			}
		}
		return false;
	}

	//Problem 16
	//Determines order of objectives
	//Determines if there are prerequisites
	//and compares alphabetically if needed.
	public void loadOrder() {
		for(String name : this.objectives.keySet()) {
			if (this.orderObjectives.isEmpty()) {
				this.orderObjectives.addFirst(name);
			} 
			else {
				int i = 0;
				for (String key : this.orderObjectives) {
					if (this.cont(this.objectives.get(name), this.objectives.get(key))) {
						i++;
						if (i == this.orderObjectives.size()) {
							this.orderObjectives.add(name);
							break;
						}
						continue;
					}
					else if (!this.cont(this.objectives.get(key), this.objectives.get(name))) {
						if (name.compareTo(this.objectives.get(key).name) > 0) {
							i++;
							if (i == this.orderObjectives.size()) {
								this.orderObjectives.add(name);
								break;
							}
							continue;
						}
						else {
							this.orderObjectives.add(i, name);
							break;
						}
					}
					else {
						if (i == this.objectives.size()) {
							this.orderObjectives.addLast(name);
							break;
						}
							else {
								this.orderObjectives.add(i, name);
								break;
							}
					}
				}
			}
		}
	}

	//Problem 16 Print method
	//Prints out the correct order of objectives
	//that was determined by prerequisites and
	//alphabetically.
	public void printObjectives() {
		for (int i = 0; i < this.objectives.size() - 1; i++) {
			System.out.print(this.orderObjectives.get(i) + ",");
		}
		System.out.print(this.orderObjectives.getLast() + "\n");
	}

	//Problem 17
	//Creates graph of objectives
	public GraphAdjListSimple<String> visualizeObjectives() {
		GraphAdjListSimple<String> ob = new GraphAdjListSimple<String>();
		for (String objective : this.orderObjectives) {
			ob.addVertex(objective, "");
		}
		for (int i = 0; i < this.orderObjectives.size() - 1; i++) {
			ob.addEdge(this.orderObjectives.get(i), this.orderObjectives.get(i + 1));
		}
		return ob;
	}
	
	//Helper Function
	//Returns the name of the starting point
	//of the loaded dungeon.
	public String getFirst() {
		for (String r : this.rooms.keySet()) {
			if (this.rooms.get(r).getCt() == 0) {
				return r;
			}
		}
		return "";
	}
	
	//Problem 18
	//Prints the routes for each of the objectives.
	public void printRoutes() {
		String start = this.getFirst();
		for(String obj : this.orderObjectives) {
			Dijkstra d = new Dijkstra(this.graph, start, this.objectives.get(obj).getRoom());
			System.out.println("Route for " + obj + ": " + d.getPath());
			start = this.objectives.get(obj).getRoom();
			this.graph = d.getGraph();
		}
	}

	public static void main(String args[]) throws Exception {
		Solution s = new Solution();
		Scanner scanner = new Scanner(System.in);
		String filename = scanner.next();
		String dungeonFile = String.format(
				"C:\\Users\\got2b\\Documents\\College\\College-Sophomore\\CISC 320\\320Eclipse\\Dungeons\\src\\dungeons/%s_in.txt",
				filename);
		dungeonFile = dungeonFile.replace("\\", "/");
		List<String> dungeon = readFile(dungeonFile);
		scanner.close();
		s.load(dungeon);
		System.out.println("Rooms: " + s.roomCt);
		System.out.println("Connections: " + s.connectCt);
		System.out.println("Objectives: " + s.objectives.size());
		String dense = (df.format((float)(2*s.connectCt) / (s.roomCt*(s.roomCt-1))*100));
		String dec = (String.valueOf(Float.valueOf(dense)/100));
		if (dec.length()>4) {
			dec = dec.substring(0, 4);
		}
		System.out.println("Density: " + dec);
		System.out.println("Dead-ends: " + s.ctDeads());
		System.out.println("Hubs: " + s.ctHubs());
		System.out.println("Max challenge: " + s.getMax());
		System.out.println("Median challenge: " + median.format(s.getMedian()));
		s.editGraph();
		String v;
		if (s.valid(s.enter, s.validityMap())) {
			v = "True";
		} else {
			v = "False";
			System.out.println("Valid: " + v);
			return;
		}
		System.out.println("Valid: " + v);
		s.addDifficulty(s.getMedian());
		if (s.balance(s.enter, s.validityMap(), Float.valueOf(median.format(s.getMedian())))) {
			System.out.println("Balanced: True");
		} else {
			System.out.println("Balanced: False");
		}
		s.visualize(113, "Part 13", s.graph);
		System.out.print("Order: ");
		s.printObjectives();
		GraphAdjListSimple<String> order = s.visualizeObjectives();
		s.visualize(117, "Part 17", order);
		s.printRoutes();
		s.visualize(119, "Part 19", s.graph);
	}

	class Room {
		private String name;
		private int challenge;
		private List<String> obectives;
		private int connections;
		private int ct;

		public Room(String n, int c, List<String> o, int count) {
			this.ct = count;
			this.name = n;
			this.challenge = c;
			this.obectives = o;
			this.connections = 0;
		}

		public String getName() {
			return this.name;
		}

		public int getChallenge() {
			return this.challenge;
		}

		public List<String> getObjectives() {
			return this.obectives;
		}

		public int getConnections() {
			return this.connections;
		}
		
		public int getCt() {
			return this.ct;
		}

		public void addConnection() {
			this.connections++;
		}
	}

	class Objective {
		private String name;
		private List<String> req;
		private String room;

		public Objective(String n, List<String> r, String room) {
			this.name = n;
			this.req = r;
			this.room = room;
		}

		public String getName() {
			return this.name;
		}

		public List<String> getReq() {
			return this.req;
		}
		
		public String getRoom() {
			return this.room;
		}

		//Used to determine if this Objective
		//directly contains Objective o
		//as a prerequisite.
		public boolean contains(Objective o) {
			if (this.req.contains(o.getName())) {
				return true;
			} else {
				return false;
			}
		}

	}
	
	//Problem 18 Helper Class
	class Dijkstra{
		private GraphAdjListSimple<String> graph;
		private String source;
		private String target;
		private HashMap<String, Integer> distance;
		private HashMap<String, String> prev;
		private ArrayList<String> rooms;
		
		public Dijkstra(GraphAdjListSimple<String> graph, String source, String target) {
			this.graph = graph;
			this.source = source;
			this.target = target;
			this.distance = new HashMap<>();
			this.prev = new HashMap<>();
			this.rooms = new ArrayList<>();
			for(String vert : graph.getVertices().keySet()) {
				this.distance.put(vert, 100);
				this.prev.put(vert, null);
				this.rooms.add(vert);
			}
			this.distance.put(source, 0);
			this.update();
		}
		
		public GraphAdjListSimple<String> getGraph(){
			return this.graph;
		}
		
		//Determines next room with minimum distance
		public String minimum() {
			int min = 100;
			String room = this.rooms.get(0);
			for (String r : this.rooms) {
				if (this.distance.get(r) < min) {
					min = this.distance.get(r);
					room = r;
				}
			}
			return room;
		}
		
		//Bulk of the algorithm -> Sets orders
		public void update() {
			while (!this.rooms.isEmpty()) {
				String current = this.rooms.remove(this.rooms.indexOf(this.minimum()));
				Iterable<Edge<String, String>> edges = this.graph.outgoingEdgeSetOf(current);
				for(Edge e : edges) {
					if (this.rooms.contains(e.getTo())) {
						String possible =  String.valueOf(e.getTo());
						int dist = this.distance.get(current);
						int length = Integer.parseInt(String.valueOf(e.getEdgeData()));
						int alt = dist + length;
						if (alt < this.distance.get(possible)) {
							this.distance.put(possible, alt);
							this.prev.put(possible, current);
						}
					}
				}
				
			}
		}
		
		//Returns string for path with given source and target.
		public String getPath() {
			String route = "";
			String current = this.target;
			while(!current.equals(this.source)) {
				this.graph.getLinkVisualizer(this.prev.get(current), current).setThickness(this.graph.getLinkVisualizer(this.prev.get(current), current).getThickness() + 1);
				route = "->" + current + route;
				current = this.prev.get(current);
			}
			route = current + route;
			return route;
		}
	}
}
