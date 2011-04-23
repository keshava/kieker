package kieker.tools.traceAnalysis.plugins.visualization.dependencyGraph;

import java.io.File;
import java.io.IOException;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;
import kieker.tools.traceAnalysis.systemModel.repository.AllocationComponentOperationPairFactory;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.ExecutionContainer;
import kieker.analysis.plugin.configuration.IInputPort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kieker.tools.traceAnalysis.systemModel.Message;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.Signature;
import kieker.tools.traceAnalysis.systemModel.SynchronousReplyMessage;
import kieker.tools.traceAnalysis.systemModel.repository.AbstractSystemSubRepository;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.tools.traceAnalysis.plugins.visualization.util.dot.DotFactory;

/**
 * Refactored copy from LogAnalysis-legacy tool
 * 
 * @author Andre van Hoorn, Lena St&ouml;ver, Matthias Rohr,
 */
public class OperationDependencyGraphPluginAllocation extends AbstractDependencyGraphPlugin<AllocationComponentOperationPair> {

    private static final Log log = LogFactory.getLog(OperationDependencyGraphPluginAllocation.class);
    private final AllocationComponentOperationPairFactory pairFactory;
    private final String COMPONENT_NODE_ID_PREFIX = "component_";
    private final String CONTAINER_NODE_ID_PREFIX = "container_";
    private final File dotOutputFile;
    private final boolean includeWeights;
    private final boolean shortLabels;
    private final boolean includeSelfLoops;

    public OperationDependencyGraphPluginAllocation(
            final String name,
            final SystemModelRepository systemEntityFactory,
            final File dotOutputFile,
            final boolean includeWeights,
            final boolean shortLabels,
            final boolean includeSelfLoops) {
        super(name, systemEntityFactory,
                new DependencyGraph<AllocationComponentOperationPair>(
                AbstractSystemSubRepository.ROOT_ELEMENT_ID,
                new AllocationComponentOperationPair(AbstractSystemSubRepository.ROOT_ELEMENT_ID,
                systemEntityFactory.getOperationFactory().rootOperation, systemEntityFactory.getAllocationFactory().rootAllocationComponent)));
        this.pairFactory = new AllocationComponentOperationPairFactory(systemEntityFactory);
        this.dotOutputFile = dotOutputFile;
        this.includeWeights = includeWeights;
        this.shortLabels = shortLabels;
        this.includeSelfLoops = includeSelfLoops;
    }

    private String containerNodeLabel(final ExecutionContainer container) {
        return String.format("%s\\n%s", STEREOTYPE_EXECUTION_CONTAINER, container.getName());
    }

    private String componentNodeLabel(final AllocationComponent component,
            final boolean shortLabels) {
        //String resourceContainerName = component.getExecutionContainer().getName();
        String assemblyComponentName = component.getAssemblyComponent().getName();
        String componentTypePackagePrefx = component.getAssemblyComponent().getType().getPackageName();
        String componentTypeIdentifier = component.getAssemblyComponent().getType().getTypeName();

        StringBuilder strBuild = new StringBuilder(STEREOTYPE_ALLOCATION_COMPONENT + "\\n");
        strBuild.append(assemblyComponentName).append(":");
        if (!shortLabels) {
            strBuild.append(componentTypePackagePrefx).append(".");
        } else {
            strBuild.append("..");
        }
        strBuild.append(componentTypeIdentifier);
        return strBuild.toString();
    }

    protected void dotEdges(Collection<DependencyGraphNode<AllocationComponentOperationPair>> nodes,
            PrintStream ps, final boolean shortLabels) {

        /* Execution container ID x contained components  */
        Hashtable<Integer, Collection<AllocationComponent>> containerId2componentMapping =
                new Hashtable<Integer, Collection<AllocationComponent>>();
        Hashtable<Integer, Collection<DependencyGraphNode<AllocationComponentOperationPair>>> componentId2pairMapping =
                new Hashtable<Integer, Collection<DependencyGraphNode<AllocationComponentOperationPair>>>();

        // Derive container / component / operation hieraŕchy
        for (DependencyGraphNode<AllocationComponentOperationPair> pairNode : nodes) {
            AllocationComponent curComponent = pairNode.getEntity().getAllocationComponent();
            ExecutionContainer curContainer = curComponent.getExecutionContainer();
            int componentId = curComponent.getId();
            int containerId = curContainer.getId();

            Collection<DependencyGraphNode<AllocationComponentOperationPair>> containedPairs =
                    componentId2pairMapping.get(componentId);
            if (containedPairs == null) {
                // component not yet registered
                containedPairs =
                        new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
                componentId2pairMapping.put(componentId, containedPairs);
                Collection<AllocationComponent> containedComponents =
                        containerId2componentMapping.get(containerId);
                if (containedComponents == null) {
                    containedComponents = new ArrayList<AllocationComponent>();
                    containerId2componentMapping.put(containerId, containedComponents);
                }
                containedComponents.add(curComponent);
            }
            containedPairs.add(pairNode);
        }

        ExecutionContainer rootContainer = this.getSystemEntityFactory().getExecutionEnvironmentFactory().rootExecutionContainer;
        int rootContainerId = rootContainer.getId();
        StringBuilder strBuild = new StringBuilder();
        for (Entry<Integer, Collection<AllocationComponent>> containerComponentEntry : containerId2componentMapping.entrySet()) {
            int curContainerId = containerComponentEntry.getKey();
            ExecutionContainer curContainer = this.getSystemEntityFactory().getExecutionEnvironmentFactory().lookupExecutionContainerByContainerId(curContainerId);

            if (curContainerId == rootContainerId) {
                strBuild.append(DotFactory.createNode("",
                        getNodeId(this.dependencyGraph.getRootNode()),
                        "$",
                        DotFactory.DOT_SHAPE_NONE,
                        null, // style
                        null, // framecolor
                        null, // fillcolor
                        null, // fontcolor
                        DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
                        null, // imagefilename
                        null // misc
                        )).toString();
            } else {
                strBuild.append(DotFactory.createCluster("",
                        CONTAINER_NODE_ID_PREFIX + curContainer.getId(),
                        containerNodeLabel(curContainer),
                        DotFactory.DOT_SHAPE_BOX, // shape
                        DotFactory.DOT_STYLE_FILLED, // style
                        null, // framecolor
                        DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
                        null, // fontcolor
                        DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
                        null));  // misc
                // dot code for contained components
                for (AllocationComponent curComponent : containerComponentEntry.getValue()) {
                    int curComponentId = curComponent.getId();
                    strBuild.append(DotFactory.createCluster("",
                            COMPONENT_NODE_ID_PREFIX + curComponentId,
                            componentNodeLabel(curComponent, shortLabels),
                            DotFactory.DOT_SHAPE_BOX,
                            DotFactory.DOT_STYLE_FILLED, // style
                            null, // framecolor
                            DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
                            null, // fontcolor
                            DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
                            null // misc
                            ));
                    for (DependencyGraphNode<AllocationComponentOperationPair> curPair : componentId2pairMapping.get(curComponentId)) {
                        Signature sig = curPair.getEntity().getOperation().getSignature();
                        StringBuilder opLabel = new StringBuilder(sig.getName());
                        opLabel.append("(");
                        String[] paramList = sig.getParamTypeList();
                        if (paramList != null && paramList.length > 0) {
                            opLabel.append("..");
                        }
                        opLabel.append(")");
                        strBuild.append(DotFactory.createNode("",
                                getNodeId(curPair),
                                opLabel.toString(),
                                DotFactory.DOT_SHAPE_OVAL,
                                DotFactory.DOT_STYLE_FILLED, // style
                                null, // framecolor
                                DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
                                null, // fontcolor
                                DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
                                null, // imagefilename
                                null // misc
                                )).toString();
                    }
                    strBuild.append("}\n");
                }
                strBuild.append("}\n");
            }
        }
        ps.println(strBuild.toString());
    }

    @Override
    public boolean execute() {
        return true; // no need to do anything here
    }

    /**
     * Saves the dependency graph to the dot file if error is not true.
     *
     * @param error
     */
    @Override
    public void terminate(boolean error) {
        if (!error) {
            try {
                this.saveToDotFile(
                        this.dotOutputFile.getCanonicalPath(),
                        this.includeWeights,
                        this.shortLabels,
                        this.includeSelfLoops);
            } catch (IOException ex) {
                log.error("IOException", ex);
            }
        }
    }
    private final IInputPort<MessageTrace> messageTraceInputPort =
            new AbstractInputPort<MessageTrace>("Message traces") {

                @Override
                public void newEvent(MessageTrace t) {
                    for (Message m : t.getSequenceAsVector()) {
                        if (m instanceof SynchronousReplyMessage) {
                            continue;
                        }
                        AllocationComponent senderComponent = m.getSendingExecution().getAllocationComponent();
                        AllocationComponent receiverComponent = m.getReceivingExecution().getAllocationComponent();
                        int rootOperationId = getSystemEntityFactory().getOperationFactory().rootOperation.getId();
                        Operation senderOperation = m.getSendingExecution().getOperation();
                        Operation receiverOperation = m.getReceivingExecution().getOperation();
                        /* The following two get-calls to the factory return s.th. in either case */
                        AllocationComponentOperationPair senderPair =
                                (senderOperation.getId() == rootOperationId) ? dependencyGraph.getRootNode().getEntity() : pairFactory.getPairInstanceByPair(senderComponent, senderOperation);
                        AllocationComponentOperationPair receiverPair =
                                (receiverOperation.getId() == rootOperationId) ? dependencyGraph.getRootNode().getEntity() : pairFactory.getPairInstanceByPair(receiverComponent, receiverOperation);

                        DependencyGraphNode<AllocationComponentOperationPair> senderNode = dependencyGraph.getNode(senderPair.getId());
                        DependencyGraphNode<AllocationComponentOperationPair> receiverNode = dependencyGraph.getNode(receiverPair.getId());
                        if (senderNode == null) {
                            senderNode = new DependencyGraphNode<AllocationComponentOperationPair>(senderPair.getId(), senderPair);
                            dependencyGraph.addNode(senderNode.getId(), senderNode);
                        }
                        if (receiverNode == null) {
                            receiverNode = new DependencyGraphNode<AllocationComponentOperationPair>(receiverPair.getId(), receiverPair);
                            dependencyGraph.addNode(receiverNode.getId(), receiverNode);
                        }
                        senderNode.addOutgoingDependency(receiverNode);
                        receiverNode.addIncomingDependency(senderNode);
                    }
                    reportSuccess(t.getTraceId());
                }
            };

    @Override
    public IInputPort<MessageTrace> getMessageTraceInputPort() {
        return this.messageTraceInputPort;
    }
}
