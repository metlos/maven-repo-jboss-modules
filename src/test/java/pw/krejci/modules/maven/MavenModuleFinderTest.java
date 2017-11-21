package pw.krejci.modules.maven;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.Version;
import org.junit.Test;

public class MavenModuleFinderTest {

    @Test
    public void testModuleLoad() throws Exception {
        MavenModuleFinder finder = new MavenModuleFinder(new File(System.getProperty("user.home"), ".m2/repository"));

        ModuleLoader moduleLoader = new ModuleLoader(new ModuleFinder[]{finder});

        Module junitModule = moduleLoader.loadModule("junit:junit:jar:4.12");

        assertEquals(Version.parse("4.12"), junitModule.getVersion());
        assertEquals("junit:junit:jar:4.12", junitModule.getName());
        assertEquals(1, junitModule.getDependencies().length);
    }
}
