/*******************************************************************************
 *
 * Copyright (c) 2011, Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * Anton Kozak
 *
 *
 *******************************************************************************/

package org.eclipse.hudson.test.ui;

import com.thoughtworks.selenium.Selenium;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Test cases for verifying cascading project functionality.
 * <p/>
 * Date: 12/13/11
 */
public class CascadingProjectTest extends BaseUITest {

    private static final String LOG_ROTATOR_RESET_LINK_EXP =
        "//a[contains(@reseturl,'/job/child-logrotate/resetProjectProperty?propertyName=logRotator')]";
    private static final String BLOCK_BUILD_UPSTREAM_RESET_LINK_EXP =
        "//a[contains(@reseturl,'/job/child-upstream/resetProjectProperty?propertyName=blockBuildWhenUpstreamBuilding')]";
    private static final String SCM_RESET_LINK_EXP =
        "//a[contains(@reseturl,'/job/child-scm/resetProjectProperty?propertyName=scm')]";
    private static final String CONFIG_SAVE_BUTTON_EXP = "//button[contains(text(), 'Save')]";

    private Selenium selenium;

    /**
     * Tests whether overriding of 'Discard Old Builds' option works correct.
     */
    @Test
    public void testCascadingLogRotate() {
        prepareCascading("parent-logrotate", "child-logrotate");
        selenium.click("//input[@name='logrotate']");
        selenium.type("//input[@name='logrotate_days']", "2");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/job/child-logrotate/configure");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent(LOG_ROTATOR_RESET_LINK_EXP));
        selenium.click(LOG_ROTATOR_RESET_LINK_EXP);
        selenium.waitForPageToLoad("30000");
        assertFalse(selenium.isElementPresent(LOG_ROTATOR_RESET_LINK_EXP));
    }

    /**
     * Tests whether overriding of 'Block build when upstream project is building ' option works correct.
     */
    @Test
    public void testCascadingBlockBuildUpstream() {
        prepareCascading("parent-upstream", "child-upstream");
        selenium.click("//span[@id='yui-gen5']/span/button");
        selenium.click("//input[@name='blockBuildWhenUpstreamBuilding']");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/job/child-upstream/configure");
        selenium.waitForPageToLoad("30000");
        selenium.click("//span[@id='yui-gen5']/span/button");
        assertTrue(selenium.isElementPresent(BLOCK_BUILD_UPSTREAM_RESET_LINK_EXP));
        selenium.click(BLOCK_BUILD_UPSTREAM_RESET_LINK_EXP);
        selenium.waitForPageToLoad("30000");
        selenium.click("//span[@id='yui-gen5']/span/button");
        assertFalse(selenium.isElementPresent(BLOCK_BUILD_UPSTREAM_RESET_LINK_EXP));
    }

    /**
     * Tests whether overriding of 'SCM' option works correct.
     */
    @Test
    public void testCascadingScm() {
        prepareCascading("parent-scm", "child-scm");
        selenium.click("//input[@name='scm' and @value='1']");
        selenium.type("//input[@name='git.repo.url']", "git://github.com/hudson-plugins/cvs-plugin.git");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/job/child-scm/configure");
        selenium.waitForPageToLoad("30000");
        assertTrue(selenium.isElementPresent(SCM_RESET_LINK_EXP));
        selenium.click(SCM_RESET_LINK_EXP);
        selenium.waitForPageToLoad("30000");
        assertFalse(selenium.isElementPresent(SCM_RESET_LINK_EXP));
    }

    /**
     * Tests whether assigning of 'Publish JUnit test result report' option falls through cascading hierarchy.
     */
    @Test
    public void testCascadingInheritance() {
        prepareCascading("parent", "child1");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.type("name", "child2");
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.select("//select[@name='cascadingProjectName']", "child1");
        selenium.waitForPageToLoad("30000");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/job/parent/configure");
        selenium.waitForPageToLoad("30000");
        selenium.click("//input[@name='hudson-tasks-junit-JUnitResultArchiver']");
        selenium.type("//input[@name='_.testResults']", "**/target/surefire-reports/*.xml");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        selenium.open("/job/child2/configure");
        selenium.waitForPageToLoad("30000");
        assertEquals("**/target/surefire-reports/*.xml", selenium.getValue("//input[@name='_.testResults']"));
    }

    /**
     * Creates cascading parent and child.
     *
     * @param cascadingParentName name of cascading parent project.
     * @param cascadingChildName name of cascading child project.
     */
    private void prepareCascading(String cascadingParentName, String cascadingChildName) {
        selenium = getSelenium();
        //Creates cascading parent.
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.waitForPageToLoad("30000");
        selenium.type("name", cascadingParentName);
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.click(CONFIG_SAVE_BUTTON_EXP);
        //Creates cascading child.
        selenium.open("/");
        waitForTextPresent("New Job");
        selenium.click("link=New Job");
        selenium.type("name", cascadingChildName);
        selenium.click("mode");
        selenium.click("//button[@type='button']");
        selenium.waitForPageToLoad("30000");
        selenium.select("//select[@name='cascadingProjectName']", cascadingParentName);
        selenium.waitForPageToLoad("30000");
    }
}
