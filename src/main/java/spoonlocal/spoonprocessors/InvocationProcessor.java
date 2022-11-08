package spoonlocal.spoonprocessors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import utils.graph.AbstractGraph;

import java.util.ArrayList;
import java.util.List;

public class InvocationProcessor extends AbstractProcessor<CtInvocation> {
    private AbstractGraph couplingGraph;
    private List<String> qualifiedNameOfApplicationClasses;

    private List<CtInvocation> ctInvocations;

    public InvocationProcessor(AbstractGraph couplingGraph, List<String> qualifiedNameOfApplicationClasses) {
        this.couplingGraph = couplingGraph;
        this.qualifiedNameOfApplicationClasses = qualifiedNameOfApplicationClasses;
        this.ctInvocations = new ArrayList<>();
    }

    @Override
    public boolean isToBeProcessed(CtInvocation candidate) {
        return this.qualifiedNameOfApplicationClasses.contains(getCalleeClass(candidate))
                && !candidate.prettyprint().startsWith("super("); // Pour éviter de prendre en compte l'appel à super
    }

    @Override
    public void process(CtInvocation ctInvocation) {

        this.ctInvocations.add(ctInvocation);
//        System.err.println(getCallerClass(ctInvocation)+"||||||||"+getCalleeClass(ctInvocation)+"."+ctInvocation.isImplicit());
        this.couplingGraph.addEdge(getCallerClass(ctInvocation), getCalleeClass(ctInvocation));
    }

    private String getCallerClass(CtInvocation ctInvocation) {
        CtElement ctElement = ctInvocation;
        while (! (ctElement instanceof CtClass)) {
            ctElement = ctElement.getParent();
        }
        CtClass ctClass = (CtClass) ctElement;
        return ctClass.getQualifiedName();
    }

    private String getCalleeClass(CtInvocation ctInvocation) {
        if (ctInvocation.getTarget() != null) {
            return ctInvocation.getTarget().getType().getQualifiedName();
        }
        return getCallerClass(ctInvocation);
    }
}
