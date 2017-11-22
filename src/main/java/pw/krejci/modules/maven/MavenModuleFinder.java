package pw.krejci.modules.maven;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static org.jboss.modules.ResourceLoaderSpec.createResourceLoaderSpec;
import static org.jboss.modules.ResourceLoaders.createJarResourceLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.Version;
import org.jboss.modules.filter.PathFilters;

public class MavenModuleFinder implements ModuleFinder {

    private final RepositorySystemSession mavenSession;
    private final RepositorySystem repositorySystem;
    private final List<RemoteRepository> remoteRepos;

    public MavenModuleFinder() {
        this(new File(new File(System.getProperty("user.home"), ".m2"), "repository"));
    }

    public MavenModuleFinder(File localMavenRepo) {
        this(localMavenRepo, mavenCentral());
    }

    public MavenModuleFinder(File localMavenRepo, Map<String, URI> remoteRepos) {
        repositorySystem = MavenBootstrap.newRepositorySystem();
        mavenSession = MavenBootstrap.newRepositorySystemSession(repositorySystem, localMavenRepo);
        this.remoteRepos = remoteRepos.entrySet().stream()
                .map(e -> new RemoteRepository.Builder(e.getKey(), "default", e.getValue().toString()).build())
                .collect(toList());
    }

    private static Map<String, URI> mavenCentral() {
        Map<String, URI> ret = new HashMap<>(1);
        ret.put("central", URI.create("https://repo.maven.apache.org/maven2/"));
        return ret;
    }

    public ModuleSpec findModule(String name, ModuleLoader delegateLoader) throws ModuleLoadException {
        try {
            DefaultArtifact check = new DefaultArtifact(name);
            if (!check.toString().equals(name)) {
                throw new ModuleLoadException("Non-canonical Maven coordinates used as module name. '" + name
                        + "' should rather be '" + check.toString() + "'.");
            }

            Artifact artifact = resolveArtifact(new DefaultArtifact(name));

            ModuleSpec.Builder bld = ModuleSpec.build(name);

            bld.setVersion(Version.parse(artifact.getVersion()));

            bld.addResourceRoot(createResourceLoaderSpec(createJarResourceLoader(name, new JarFile(artifact.getFile()))));
            //needed, so that the module can load classes from the resource root
            bld.addDependency(DependencySpec.createLocalDependencySpec());

            resolveDependencies(artifact).stream()
                    .filter(a -> !artifact.toString().equals(a.toString()) && "jar".equalsIgnoreCase(a.getExtension()))
                    .forEach(a -> bld.addDependency(DependencySpec.createModuleDependencySpec(a.toString())));

            return bld.create();
        } catch (ArtifactResolutionException | IllegalStateException | IOException e) {
            throw new ModuleLoadException(e);
        }
    }

    private Artifact resolveArtifact(Artifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setRepositories(remoteRepos);
        request.setArtifact(artifact);

        ArtifactResult result = repositorySystem.resolveArtifact(mavenSession, request);
        if (result.getExceptions() != null && !result.getExceptions().isEmpty()) {
            throw new ArtifactResolutionException(singletonList(result), "Artifact resolution failed.");
        }

        if (!result.isResolved() || result.isMissing()) {
            throw new IllegalStateException("Failed to resolve the artifact " + artifact);
        }

        return result.getArtifact();
    }

    private Collection<Artifact> resolveDependencies(Artifact artifact) {
        CollectRequest collectRequest = new CollectRequest(new Dependency(artifact, null), remoteRepos);

        DependencyRequest request = new DependencyRequest(collectRequest, null);

        DependencyResult result;

        try {
            result = repositorySystem.resolveDependencies(mavenSession, request);
        } catch (DependencyResolutionException dre) {
            result = dre.getResult();
        }

        if (result == null) {
            return emptyList();
        }

        return result.getArtifactResults().stream().map(ArtifactResult::getArtifact).collect(toSet());
    }
}
