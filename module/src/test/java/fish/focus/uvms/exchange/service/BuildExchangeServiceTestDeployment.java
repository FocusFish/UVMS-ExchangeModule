package fish.focus.uvms.exchange.service;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

@ArquillianSuiteDeployment
public abstract class BuildExchangeServiceTestDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildExchangeServiceTestDeployment.class);

    @Deployment(name = "exchangeservice", order = 2)
    public static Archive<?> createDeployment() {
        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "exchangeservice.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeAndTestDependencies().resolve().withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addAsLibraries(Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("fish.focus.uvms.lib:usm4uvms")
                .withTransitivity().asFile());

        testWar.addPackages(true, "fish.focus.uvms.exchange.service");
        testWar.addPackages(true, "fish.focus.uvms.exchange.rest");

        testWar.deleteClass(AssetModuleMock.class);
        testWar.deleteClass(UserRestMock.class);
        testWar.deleteClass(UnionVMSMock.class);

        testWar.addAsWebInfResource("META-INF/ejb-jar.xml");
        testWar.addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");
        testWar.addAsResource("META-INF/beans.xml", "beans.xml");

        testWar.delete("/WEB-INF/web.xml");
        testWar.addAsWebInfResource("mock-web.xml", "web.xml");

        return testWar;
    }

    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createMocks() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "unionvms.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.asset:asset-client",
                        "fish.focus.uvms.commons:uvms-commons-message",
                        "fish.focus.uvms.lib:usm4uvms")
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        testWar.addClass(AssetModuleMock.class);
        testWar.addClass(UserRestMock.class);
        testWar.addClass(UnionVMSMock.class);

        return testWar;
    }
}
