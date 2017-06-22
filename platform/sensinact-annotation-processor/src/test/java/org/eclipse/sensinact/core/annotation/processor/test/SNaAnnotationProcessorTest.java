/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.core.annotation.processor.test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import java.io.File;
import java.net.MalformedURLException;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

import org.eclipse.sensinact.annotation.processor.SNaConstraintAnnotationProcessor;


/**
 * Constrained annotation processing test
 */
public class SNaAnnotationProcessorTest
{   
    @Test
    public void testValidAnnotationUse() throws MalformedURLException
    {
        File javaFile = new File("./src/test/java/org/eclipse/sensinact/core/annotation/processor/test/ClassOne.java");
        JavaFileObject fileObject = JavaFileObjects.forResource(javaFile.toURI().toURL());
          assert_().about(javaSource()).that(fileObject)
            .processedWith(new SNaConstraintAnnotationProcessor())
            .compilesWithoutError();
    }
    
    @Test
    public void testInvalidAnnotationUse() throws MalformedURLException
    {
        File javaFile = new File("./src/test/java/org/eclipse/sensinact/core/annotation/processor/test/ClassTwo.java");
        JavaFileObject fileObject = JavaFileObjects.forResource(javaFile.toURI().toURL());
          assert_().about(javaSource()).that(fileObject)
            .processedWith(new SNaConstraintAnnotationProcessor())
            .failsToCompile().withErrorContaining("Surname annotated element not found");
    }
}
