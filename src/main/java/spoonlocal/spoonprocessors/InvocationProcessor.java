package spoonlocal.spoonprocessors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import utils.graph.AbstractGraph;
import utils.graph.Node;

import java.util.ArrayList;
import java.util.List;

public class InvocationProcessor extends AbstractProcessor<CtInvocation> {
    private AbstractGraph couplingGraph;
    private List<String> qualifiedNameOfApplicationClasses;
    private List<CtInvocation> ctInvocations;
    private List<String> stringsFilter;

    public InvocationProcessor(AbstractGraph couplingGraph, List<String> qualifiedNameOfApplicationClasses) {
        this.couplingGraph = couplingGraph;
        this.qualifiedNameOfApplicationClasses = qualifiedNameOfApplicationClasses;
        this.ctInvocations = new ArrayList<>();
        this.stringsFilter = new ArrayList<>();
    }

    @Override
    public boolean isToBeProcessed(CtInvocation candidate) {
        CtClass caller = getCallerClass(candidate);
        String callerMethodName = getCallerMethod(candidate) == null ? getCallerConstructor(candidate).getSimpleName() : getCallerMethod(candidate).getSimpleName();
        return this.qualifiedNameOfApplicationClasses.contains(getCalleeClass(candidate))
                && !this.stringsFilter.contains(caller.getQualifiedName()+"::"+callerMethodName+"-->"+getCalleeClass(candidate)+"::"+candidate.prettyprint())
                && !caller.getQualifiedName().equals(getCalleeClass(candidate))
                && !candidate.prettyprint().startsWith("super("); // Pour éviter de prendre en compte l'appel à super
    }

    @Override
    public void process(CtInvocation ctInvocation) {
        this.ctInvocations.add(ctInvocation);
//        System.err.println(getCallerClass(ctInvocation)+"||||||||"+getCalleeClass(ctInvocation)+"."+ctInvocation.isImplicit());
        CtClass caller = getCallerClass(ctInvocation);
        String callerMethodName = getCallerMethod(ctInvocation) == null ? getCallerConstructor(ctInvocation).getSimpleName() : getCallerMethod(ctInvocation).getSimpleName();
        this.stringsFilter.add(caller.getQualifiedName()+"::"+callerMethodName+"-->"+getCalleeClass(ctInvocation)+"::"+ctInvocation.prettyprint());
        this.couplingGraph.addEdge(new Node(caller.getPackage().getQualifiedName(), caller.getSimpleName()),
                new Node(ctInvocation.getTarget().getType().getPackage().getQualifiedName(), ctInvocation.getTarget().getType().getSimpleName())/*getCallerClass(ctInvocation), getCalleeClass(ctInvocation)*/);
    }

    private CtClass getCallerClass(CtInvocation ctInvocation) {
        CtElement ctElement = ctInvocation;
        while (! (ctElement instanceof CtClass)) {
            ctElement = ctElement.getParent();
        }
        CtClass ctClass = (CtClass) ctElement;
        return ctClass;
    }

    private CtMethod getCallerMethod(CtInvocation ctInvocation) {
        CtElement ctElement = ctInvocation;
        while (! (ctElement instanceof CtMethod)) {
            if (ctElement == null)
                return null;
            ctElement = ctElement.getParent();
        }
        CtMethod ctMethod = (CtMethod) ctElement;
        return ctMethod;
    }

    private CtConstructor getCallerConstructor(CtInvocation ctInvocation) {
        CtElement ctElement = ctInvocation;
        while (! (ctElement instanceof CtConstructor)) {
            if (ctElement == null)
                return null;
            ctElement = ctElement.getParent();
        }
        CtConstructor ctConstructor = (CtConstructor) ctElement;
        return ctConstructor;
    }

    private String getCalleeClass(CtInvocation ctInvocation) {
        if (ctInvocation.getTarget() != null) {
            return ctInvocation.getTarget().getType().getQualifiedName();
        }
        return getCallerClass(ctInvocation).getQualifiedName();
    }
}
