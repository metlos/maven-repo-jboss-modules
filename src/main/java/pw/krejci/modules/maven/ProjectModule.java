package pw.krejci.modules.maven;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;

public final class ProjectModule {

    private ProjectModule() {
        throw new AssertionError();
    }

    public static Builder build() {
        return build("#");
    }

    public static Builder build(String projectName) {
        return new Builder(projectName);
    }

    public static final class Builder {
        private final String projectName;
        private final Set<String> deps;
        private File localRepo;
        private final Map<String, URI> remoteRepositories;

        private Builder(String projectName) {
            this.projectName = projectName;
            this.deps = new HashSet<>(8);
            this.localRepo = new File(new File(System.getProperty("user.home"), ".m2"), "repository");;
            this.remoteRepositories = new HashMap<>(2);
            this.remoteRepositories.put("central", URI.create("https://repo.maven.apache.org/maven2/"));
        }

        public Builder addDependency(String gav) {
            deps.add(new DefaultArtifact(gav).toString());
            return this;
        }

        public Builder localRepository(File root) {
            this.localRepo = root;
            return this;
        }

        public Builder localRepository(String rootPath) {
            return localRepository(new File(rootPath));
        }

        public Builder addRemoteRepository(String name, String url) {
            this.remoteRepositories.put(name, URI.create(url));
            return this;
        }

        public Module create() {
            ModuleFinder projectFinder = new ModuleFinder() {
                @Override
                public ModuleSpec findModule(String name, ModuleLoader delegateLoader) throws ModuleLoadException {
                    if (!name.equals(projectName)) {
                        return null;
                    }

                    ModuleSpec.Builder bld = ModuleSpec.build(projectName);
                    for (String dep : deps) {
                        bld.addDependency(DependencySpec.createModuleDependencySpec(dep));
                    }

                    return bld.create();
                }
            };

            ModuleFinder mavenModuleFinder = new MavenModuleFinder(localRepo, remoteRepositories);

            ModuleLoader loader = new ModuleLoader(new ModuleFinder[]{projectFinder, mavenModuleFinder});

            try {
                return loader.loadModule(projectName);
            } catch (ModuleLoadException e) {
                throw e.toError();
            }
        }
    }
}
