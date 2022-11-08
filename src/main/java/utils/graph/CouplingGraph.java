package utils.graph;

public class CouplingGraph extends AbstractGraph {
    public CouplingGraph() {
        super();
    }

    @Override
    public void addEdge(String node1, String node2) {
        Edge edge = this.findEdge(node1, node2);
        if (edge == null) {
            this.edges.add(new WeightEdge(node1, node2));
        }
        else {
            ((WeightEdge) edge).incrWeight();
        }
    }
}
