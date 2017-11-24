package pw.krejci.modules.maven;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.filter.PathFilters;

final class ImportEverythingExportServices {
    private ImportEverythingExportServices() {
        throw new AssertionError();
    }

    static DependencySpec spec(String moduleName) {
        return DependencySpec.createModuleDependencySpec(PathFilters.acceptAll(),
                PathFilters.getMetaInfServicesFilter(), null, moduleName, false);
    }
}
