package spoonlocal.spoonprocessors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import utils.graph.AbstractGraph;
import utils.graph.Node;
import utils.graph.WeightedCouplingGraph;

import java.util.ArrayList;
import java.util.List;

public class ClassProcessor extends AbstractProcessor<CtClass> {
    private List<String> qualifiedNameOfApplicationClasses;
    private WeightedCouplingGraph graph;

    public ClassProcessor(WeightedCouplingGraph graph) {
        this.qualifiedNameOfApplicationClasses = new ArrayList<>();
        this.graph = graph;
    }

    @Override
    public void process(CtClass ctClass) {
        this.graph.addNode(new Node(ctClass.getPackage().getQualifiedName(), ctClass.getSimpleName()));
        this.qualifiedNameOfApplicationClasses.add(ctClass.getQualifiedName());
    }

    public List<String> getQualifiedNameOfApplicationClasses() {
        return qualifiedNameOfApplicationClasses;
    }
}
