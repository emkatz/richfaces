package org.richfaces.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.richfaces.deployment.CoreDeployment;
import org.richfaces.shrinkwrap.descriptor.FaceletAsset;
import org.richfaces.shrinkwrap.descriptor.PropertiesAsset;

@RunWith(Arquillian.class)
@WarpTest
public class ResourceMappingTest {

    @Drone
    WebDriver driver;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        CoreDeployment deployment = new CoreDeployment(ResourceMappingTest.class);

        PropertiesAsset staticResourceMapping = new PropertiesAsset()
                .key(":original.css").value("relocated.css")
                .key(":part1.css").value("aggregated.css")
                .key(":part2.css").value("aggregated.css");

        StringAsset stylesheetResource = new StringAsset("content");

        FaceletAsset relocationPage = new FaceletAsset().head("<h:outputStylesheet name=\"original.css\" />");

        FaceletAsset aggregationPage = new FaceletAsset().head("<h:outputStylesheet name=\"part1.css\" />"
                + "<h:outputStylesheet name=\"part2.css\" />");

        deployment.archive()
                /** META-INF */
                .addAsResource(staticResourceMapping, "META-INF/richfaces/static-resource-mappings.properties")
                /** ROOT */
                .addAsWebResource(relocationPage, "relocation.xhtml")
                .addAsWebResource(aggregationPage, "aggregation.xhtml")
                .addAsWebResource(stylesheetResource, "resources/original.css")
                .addAsWebResource(stylesheetResource, "resources/part1.css")
                .addAsWebResource(stylesheetResource, "resources/part2.css")
                .addAsWebResource(stylesheetResource, "resources/relocated.css")
                .addAsWebResource(stylesheetResource, "resources/aggregated.css");

        return deployment.archive();
    }

    @Test
    @RunAsClient
    public void test_resource_relocation() {

        driver.navigate().to(contextPath + "relocation.jsf");

        WebElement element = driver.findElement(By.cssSelector("head > link[rel=stylesheet]"));
        String href = element.getAttribute("href");

        assertTrue("href must end with the relocated.css resource path", href.contains("/javax.faces.resource/relocated.css"));
    }

    @Test
    @RunAsClient
    public void test_resource_aggregation() {

        driver.navigate().to(contextPath + "aggregation.jsf");

        List<WebElement> elements = driver.findElements(By.cssSelector("head > link[rel=stylesheet]"));

        assertEquals("There must be exactly one resource link rendered", 1, elements.size());

        WebElement element = elements.get(0);
        String href = element.getAttribute("href");

        assertTrue("href must end with the aggregated.css resource path", href.contains("/javax.faces.resource/aggregated.css"));
    }
}
