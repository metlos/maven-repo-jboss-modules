package pw.krejci.modules.maven;

import org.eclipse.aether.artifact.DefaultArtifact;

public final class MavenCoordinates {

    private MavenCoordinates() {
        throw new AssertionError();
    }

    public static String canonicalize(String gav) {
        return new DefaultArtifact(gav).toString();
    }
}
