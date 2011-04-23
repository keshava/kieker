package kieker.tools.traceAnalysis.plugins.messageTraceRepository;

import java.util.Hashtable;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.analysis.plugin.configuration.IInputPort;
import kieker.tools.traceAnalysis.plugins.AbstractMessageTraceProcessingPlugin;
import kieker.tools.traceAnalysis.plugins.traceReconstruction.TraceProcessingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Andre van Hoorn
 */
public class MessageTraceRepositoryPlugin extends AbstractMessageTraceProcessingPlugin {

    private static final Log log = LogFactory.getLog(MessageTraceRepositoryPlugin.class);
    private final Hashtable<Long, MessageTrace> repo = new Hashtable<Long, MessageTrace>();

    // TODO: handle equivalence classes

    public MessageTraceRepositoryPlugin(final String name, final SystemModelRepository systemEntityFactory) {
        super(name, systemEntityFactory);
    }

    public Hashtable<Long, MessageTrace> getMessageTraceRepository() {
        return this.repo;
    }

    private final IInputPort<MessageTrace> messageTraceInputPort =
            new AbstractInputPort<MessageTrace>("Message traces"){

        @Override
        public void newEvent(MessageTrace mt) {
            repo.put(mt.getTraceId(), mt);
        }
    };

    @Override
    public IInputPort<MessageTrace> getMessageTraceInputPort() {
        return this.messageTraceInputPort;
    }

    @Override
    public boolean execute() {
        return true; // no need to do anything here
    }

    @Override
    public void terminate(boolean error) {
        // no need to do anything here
    }
}
