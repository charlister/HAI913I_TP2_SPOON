package spoonlocal.spoonprocessors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import utils.Graph;

import java.util.ArrayList;
import java.util.List;

public class ClassProcessor extends AbstractProcessor<CtClass> {
    private List<String> qualifiedNameOfApplicationClasses;
    private Graph graph;

    public ClassProcessor(Graph graph) {
        this.qualifiedNameOfApplicationClasses = new ArrayList<>();
        this.graph = graph;
    }

    @Override
    public void process(CtClass ctClass) {
        this.graph.addNode(ctClass.getQualifiedName());
        this.qualifiedNameOfApplicationClasses.add(ctClass.getQualifiedName());
    }

    public List<String> getQualifiedNameOfApplicationClasses() {
        return qualifiedNameOfApplicationClasses;
    }
}