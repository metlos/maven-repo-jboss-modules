package pw.krejci.modules.maven;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

/**
 * A simplistic transfer listener that logs uploads/downloads to the console.
 *
 * This class is taken verbatim from maven resolver demos.
 */
final class SilentTransferListener extends AbstractTransferListener {

    private PrintStream out;

    public SilentTransferListener() {
        this(null);
    }

    public SilentTransferListener(PrintStream out) {
        this.out = (out != null) ? out : System.out;
    }

    @Override
    public void transferInitiated(TransferEvent event) {
    }

    @Override
    public void transferProgressed(TransferEvent event) {
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
    }

    @Override
    public void transferFailed(TransferEvent event) {
    }

    public void transferCorrupted(TransferEvent event) {
    }
}
