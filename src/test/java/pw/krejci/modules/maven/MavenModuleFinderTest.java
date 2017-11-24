package pw.krejci.modules.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleDependencySpec;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.Version;
import org.junit.Test;

public class MavenModuleFinderTest {

    @Test
    public void testModuleLoad() throws Exception {
        MavenModuleFinder finder = new MavenModuleFinder();

        ModuleLoader moduleLoader = new ModuleLoader(new ModuleFinder[]{finder});

        Module junitModule = moduleLoader.loadModule("junit:junit:jar:4.12");

        assertEquals(Version.parse("4.12"), junitModule.getVersion());
        assertEquals("junit:junit:jar:4.12", junitModule.getName());
        assertEquals(3, junitModule.getDependencies().length);
        assertFalse(junitModule.getDependencies()[0] instanceof ModuleDependencySpec);
        assertFalse(junitModule.getDependencies()[1] instanceof ModuleDependencySpec);
        assertTrue(junitModule.getDependencies()[2] instanceof ModuleDependencySpec);
    }

    @Test
    public void testClassLoading() throws Exception {
        Module project = ProjectModule.build().addDependency("junit:junit:4.12").create();
        Class<?> testType = project.getClassLoader().loadClass("org.junit.Test");
        assertEquals("org.junit.Test", testType.getName());
    }
}
