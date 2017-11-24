package pw.krejci.modules.maven;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleSpec;

/**
 * Can be used by the users of {@link ProjectModule} to modify the process of defining the modules and their
 * dependencies.
 * <p>
 * This can be useful for example in situations where you need to have some classes loaded from the system classpath
 * and not through the module system.
 */
public interface ModuleSpecController {

    ModuleSpecController NOOP = new ModuleSpecController() {
    };

    /**
     * Called to inform the controller that a module with given name is about to be processed.
     * <p>
     * This is the first call into the controller for any given module.
     * @param moduleName the name of the module
     */
    default void start(String moduleName) {
    }

    /**
     * Gives the controller a chance to modify how a module depends on the detected dependencies.
     * <p>
     * This is called after {@link #start(String)} zero or more times depending on the number of dependencies
     * automatically detected for given module.
     *
     * @param dependencyName the name of the detected dependency
     * @param original the dependency spec as defined using the default strategy
     * @return the original if no changed needed, a new dependency spec instance if the dependency needs to be defined
     * different from the default or null if the dependency should be omitted altogether
     */
    default DependencySpec modifyDependency(String dependencyName, DependencySpec original) {
        return original;
    }

    /**
     * Called after all dependencies were processed using {@link #modifyDependency(String, DependencySpec)}.
     * <p>
     * This gives the controller a chance to modify the module as a whole - add additional dependencies, change the
     * declared version, etc.
     *
     * @param bld the builder of the module specification
     */
    default void modify(ModuleSpec.Builder bld) {

    }

    /**
     * Called as the last method when processing a module of given name.
     *
     * @param moduleName the name of the module that has been processed
     */
    default void end(String moduleName) {

    }
}
