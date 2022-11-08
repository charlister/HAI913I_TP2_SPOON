package processor;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import spoonlocal.spoonparser.SpoonParser;
import spoonlocal.spoonprocessors.ClassProcessor;
import spoonlocal.spoonprocessors.InvocationProcessor;
import utils.Graph;
import utils.cluster.Cluster;
import utils.cluster.ICluster;
import utils.cluster.SimpleCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Processor {
	private Graph graph;

	public Processor() {
		this.graph = new Graph();
	}

	public float couplage(String classe1, String classe2) {
		Graph.Edge edge = graph.findEdge(classe1, classe2);
		float a = edge == null ? 0 : edge.getWeight();
		float b = 0;
		for (Graph.Edge e :
				graph.getEdges()) {
			b += e.getWeight();
		}
		return a/b;
	}

	/* EXERCICE 2 */

	public float calculateCouplingBetweenClusters (ICluster cluster1, ICluster cluster2) {
		float result = 0;
		float divisor = 0;

		List<String> monoClusters1 = cluster1.getClusterClasses();
		List<String> monoClusters2 = cluster2.getClusterClasses();

		for (String classMonoClusters1 : monoClusters1) {
			for (String classMonoClusters2 : monoClusters2) {
				if (!classMonoClusters1.equals(classMonoClusters2)) {
					float tmp = couplage(classMonoClusters1, classMonoClusters2);
					result += tmp;
					divisor++;
				}
			}
		}

		result /= divisor;
        System.err.println(format("(%s ; %s) = %f", cluster1, cluster2, result));

		return result;
	}

	public int[] clusterProche(List<ICluster> subClusters) {
		int[] newBestClusterIndex = new int[2];
		float couplageMax = -1;
		int n = subClusters.size();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j)
					continue;
				float tmpCouplage = calculateCouplingBetweenClusters (subClusters.get(i), subClusters.get(j));
				if (tmpCouplage > couplageMax) {
					newBestClusterIndex[0] = i;
					newBestClusterIndex[1] = j;
					couplageMax = tmpCouplage;
				}
			}
		}
		return newBestClusterIndex;
	}

	public ICluster clusteringHierarchic() {
		int[] newBestClusterIndex;
		Cluster mainCluster = new Cluster();
		List<String> classes = graph.getNodes();
		for (String className : classes) {
			SimpleCluster simpleCluster = new SimpleCluster(className);
			mainCluster.addCluster(simpleCluster);
		}
		while (mainCluster.getSubClusters().size() > 1) {
			/*(c1, c2)*/
			newBestClusterIndex = clusterProche(mainCluster.getSubClusters());
			/*c3*/
			Cluster newBestCluster = new Cluster();
			newBestCluster.addCluster(mainCluster.getSubClusters().get(newBestClusterIndex[0]));
			newBestCluster.addCluster(mainCluster.getSubClusters().get(newBestClusterIndex[1]));
			/*enlever c1*/
			mainCluster.getSubClusters().remove(newBestClusterIndex[0]);
			/*enlever c2*/
			mainCluster.getSubClusters().remove(newBestClusterIndex[1]-1);
			/*ajouter c3*/
			mainCluster.addCluster(newBestCluster);
		}
		return mainCluster.getSubClusters().get(0);
	}

	public Map<ICluster, Float> identifyModulesBis(ICluster cluster, float CP) {
		Map<ICluster, Float> mapModuleCoupling = new HashMap<>();

		if (cluster.getSubClusters().size() > 1) {
			float coupling = calculateCouplingBetweenClusters (cluster.getSubClusters().get(0), cluster.getSubClusters().get(1));
			if (coupling > CP) {
				mapModuleCoupling.put(cluster, coupling);
				System.out.println("Module ajouté : " + cluster + " : " + coupling);
			}
			for (ICluster subCluster : cluster.getSubClusters()) {
				mapModuleCoupling.putAll(identifyModulesBis(subCluster, CP));
			}
		}

		return mapModuleCoupling;
	}

	public Set<ICluster> identifyModules(ICluster cluster, float CP) {
		Set<ICluster> modules;
		Map<ICluster, Float> mapModuleCoupling = identifyModulesBis(cluster, CP);

		// sélectionner les meilleurs modules avec une limite de M/2
		int M = graph.getNodes().size();
		modules = mapModuleCoupling
				.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // delete Comparator.reverseOrder() from parenthesis -> asc on map values
				.limit(M/2)
				.collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()))
				.keySet();

		return modules;
	}

	public void writeWeightedCouplingGraphInDotFile(String fileGraphPath) throws IOException {
		FileWriter fW = new FileWriter(fileGraphPath);
		fW.write("digraph CouplingGraph {\n");
		fW.write("edge[dir=none]\n");
		for (Graph.Edge edge : graph.getEdges()) {
			fW.write("\""+edge.getNode1()+"\""+"->"+"\""+edge.getNode2()+"\""+ format(" [ label=\"%s\" ]", edge.getWeight())+"\n");
		}
		fW.write("}");
		fW.close();
		convertDotToSVG(fileGraphPath);
	}

	private void convertDotToSVG(String fileGraphPath) throws IOException {
		Parser p = new Parser();
		MutableGraph g = p.read(new File(fileGraphPath));
		Renderer render = Graphviz.fromGraph(g).render(Format.SVG);
		File imgFile = new File(fileGraphPath+".svg");
		if (imgFile.exists())
			imgFile.delete();
		render.toFile(imgFile);
		if (imgFile.exists())
			System.out.println(imgFile.getAbsolutePath());
	}

	public void analyse(String projectPath) {
		ClassProcessor classProcessor = new ClassProcessor(graph);

		SpoonParser spoonParser = new SpoonParser(projectPath);
		spoonParser.configure();
		spoonParser.addProcessor(classProcessor);

		InvocationProcessor invocationProcessor = new InvocationProcessor(graph, classProcessor.getQualifiedNameOfApplicationClasses());
		spoonParser.addProcessor(invocationProcessor);

		spoonParser.run();

		System.out.println(graph);
	}
}
