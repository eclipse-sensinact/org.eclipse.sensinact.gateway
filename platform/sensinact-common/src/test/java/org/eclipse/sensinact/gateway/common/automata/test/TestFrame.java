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
package org.eclipse.sensinact.gateway.common.automata.test;

import org.eclipse.sensinact.gateway.common.automata.Frame;
import org.eclipse.sensinact.gateway.common.automata.FrameException;
import org.eclipse.sensinact.gateway.common.automata.FrameFactory;
import org.eclipse.sensinact.gateway.common.automata.FrameFactoryImpl;
import org.eclipse.sensinact.gateway.common.automata.FrameModel;
import org.eclipse.sensinact.gateway.common.automata.FrameModelException;
import org.eclipse.sensinact.gateway.common.automata.FrameModelHandler;
import org.eclipse.sensinact.gateway.common.automata.FrameModelProvider;
import org.eclipse.sensinact.gateway.common.automata.FrameProcessorException;
import org.eclipse.sensinact.gateway.common.automata.ProcessorImpl;
import org.eclipse.sensinact.gateway.common.automata.ProcessorListener;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Test Frame
 */
public class TestFrame implements ProcessorListener {
    private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName() + ")";
    private static final String HANDLER_FILTER = "(" + Constants.OBJECTCLASS + "=" + FrameModelHandler.class.getCanonicalName() + ")";

    private static final String FACTORY_FILTER = "(" + Constants.OBJECTCLASS + "=" + FrameFactory.class.getCanonicalName() + ")";
    private static final String PROVIDER_FILTER = "(" + Constants.OBJECTCLASS + "=" + FrameModelProvider.class.getCanonicalName() + ")";

    private static final String LISTENERS_FILTER = "(" + Constants.OBJECTCLASS + "=" + ProcessorListener.class.getCanonicalName() + ")";


    private static final String MOCK_BUNDLE_NAME = "MockedBundle";
    private static final long MOCK_BUNDLE_ID = 1;
    private static final String FRAME_XML_MODEL_PATH = "src/test/resources/frameTest.xml";
    private static final String FRAME_XML_MODEL2_PATH = "src/test/resources/frameTest2.xml";

    private static final String IMPLEMENTATION_TEST_CLASS = "org.eclipse.sensinact.gateway.common.automata.test.TFrame";
    private static final byte ESCAPE = 0x23;

    @SuppressWarnings("deprecation")
    private static final long dateLng = Date.parse("Wed May 07 11:18:36 UTC 2014");
    private static final String message = "HELLO";

    private static final String FIRST_TYPE_NAME = "first";
    private static final String SECOND_TYPE_NAME = "second";
    private static final String THIRD_TYPE_NAME = "third";
    private static final String FOURTH_TYPE_NAME = "fourth";
    private static final String FIFTH_TYPE_NAME = "fifth";

    private ProcessorImpl processor;
    private byte[] frameBytes = new byte[]{112, 113, 114, 115, 116};
    private byte[] firstChildFrameBytes = new byte[]{112, 113, 114};
    private byte[] secondChildFrameBytes = new byte[]{115, 116};
    private byte[] firstGranChildFrameBytes = new byte[]{115};
    private byte[] secondGranChildFrameBytes = new byte[]{116};
    private byte[] frameBytes2 = new byte[]{0x0e, 0x53, 0x6a, 0x16, 0x0c, 0x53, 0x6a, 0x16, (byte) 0x8c, 72, 69, 76, 76, 79};
    private byte[] firstChildFrameBytes2 = new byte[]{0x0e};
    private byte[] secondChildFrameBytes2 = new byte[]{0x53, 0x6a, 0x16, 0x0c};
    private byte[] thirdChildFrameBytes2 = new byte[]{0x53, 0x6a, 0x16, (byte) 0x8c, 72, 69, 76, 76, 79};
    private byte[] firstGranChildFrameBytes2 = new byte[]{0x53, 0x6a, 0x16, (byte) 0x8c};
    private byte[] secondGranChildFrameBytes2 = new byte[]{72, 69, 76, 76, 79};
    private byte[] frameBytes3 = new byte[]{0x05, 0x51, 0x9, 0x2d, (byte) 0xd4};
    private byte[] firstChildFrameBytes3 = new byte[]{0x05};
    private byte[] secondChildFrameBytes3 = new byte[]{0x51, 0x9, 0x2d, (byte) 0xd4};

    private byte[] escapedFrame = new byte[]{0x0e, 0x53, 0x6a, 0x16, 0x0c, 0x53, 0x6a, 0x16, (byte) 0x8c, ESCAPE, (byte) 0xff, 76, 76, 79};
    private byte[] escapedTwiceFrame = new byte[]{0x0b, 0x53, 0x6a, 0x16, 0x0c, 0x53, 0x6a, 0x16, (byte) 0x8c, ESCAPE, ESCAPE, (byte) 0xff, 76, 76, 79};
    private byte[] escapedTwiceFinalFrame = new byte[]{0x0b, 0x53, 0x6a, 0x16, 0x0c, 0x53, 0x6a, 0x16, (byte) 0x8c, ESCAPE, ESCAPE};

    private BundleContext context = null;
    private Bundle bundle = null;
    private ServiceReference reference;

    @Before
    public void init() throws Exception {
        context = Mockito.mock(BundleContext.class);
        bundle = Mockito.mock(Bundle.class);
        reference = Mockito.mock(ServiceReference.class);

        Filter logFilter = Mockito.mock(Filter.class);
        Filter handlerFilter = Mockito.mock(Filter.class);
        Filter factoryFilter = Mockito.mock(Filter.class);
        Filter providerFilter = Mockito.mock(Filter.class);
        Filter listenersFilter = Mockito.mock(Filter.class);

        Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
        Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
        Mockito.when(context.getBundle()).thenReturn(bundle);
        Mockito.when(logFilter.toString()).thenReturn(LOG_FILTER);
        Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(logFilter);
        Mockito.when(handlerFilter.toString()).thenReturn(HANDLER_FILTER);
        Mockito.when(context.createFilter(HANDLER_FILTER)).thenReturn(handlerFilter);
        Mockito.when(factoryFilter.toString()).thenReturn(FACTORY_FILTER);
        Mockito.when(context.createFilter(FACTORY_FILTER)).thenReturn(factoryFilter);

        Mockito.when(providerFilter.toString()).thenReturn(PROVIDER_FILTER);
        Mockito.when(context.createFilter(PROVIDER_FILTER)).thenReturn(providerFilter);
        Mockito.when(listenersFilter.toString()).thenReturn(LISTENERS_FILTER);
        Mockito.when(context.createFilter(LISTENERS_FILTER)).thenReturn(listenersFilter);

        Mockito.when(context.getServiceReferences((String) null, LOG_FILTER)).thenReturn(null);
        Mockito.when(context.getServiceReferences((String) null, HANDLER_FILTER)).thenReturn(null);

        Mockito.when(context.getServiceReferences((String) null, PROVIDER_FILTER)).thenReturn(null);
        Mockito.when(context.getServiceReferences((String) null, LISTENERS_FILTER)).thenReturn(null);

        Mockito.when(reference.getBundle()).thenReturn(bundle);

        Mockito.when(context.getService(reference)).thenReturn(new FrameFactoryImpl());

        Mockito.when(context.getServiceReferences((String) null, FACTORY_FILTER)).thenReturn(new ServiceReference[]{reference});

        Mediator mediator = new Mediator(context);
        processor = new ProcessorImpl(mediator, this);
    }

    @Test
    public void testModel() throws FrameModelException {
        processor.setXmlModel(FRAME_XML_MODEL_PATH);
        FrameModel model = processor.getModel();

        assertEquals(IMPLEMENTATION_TEST_CLASS, model.getClassName());
        assertEquals(0, model.length());
        assertEquals(FIRST_TYPE_NAME, model.getName());

        int index = 0;
        Object[][] models = new Object[][]{{THIRD_TYPE_NAME, IMPLEMENTATION_TEST_CLASS + "_2", 3, 0}, {FOURTH_TYPE_NAME, IMPLEMENTATION_TEST_CLASS + "_3", 2, 3}, {FIFTH_TYPE_NAME, IMPLEMENTATION_TEST_CLASS + "_4", 1, 0}, {SECOND_TYPE_NAME, IMPLEMENTATION_TEST_CLASS + "_1", 1, 1}};

        while (model.size() > 0) {
            int modelIndex = 0;
            FrameModel[] frames = model.children();
            for (; modelIndex < frames.length; modelIndex++) {
                model = frames[modelIndex];
                assertEquals(models[index][0], model.getName());
                assertEquals(models[index][1], model.getClassName());
                assertEquals(models[index][2], model.length());
                assertEquals(models[index][3], model.offset());
                index++;
            }
        }
    }

    @Test
    public void testFrame() throws FrameProcessorException, FrameModelException, FrameException {
        processor.setXmlModel(FRAME_XML_MODEL_PATH);
        Frame frame = this.push(frameBytes);
        assertNotNull(frame);

        Frame[] children = frame.getChildren();
        assertTrue(compareBytes(firstChildFrameBytes, children[0]));
        assertTrue(compareBytes(secondChildFrameBytes, children[1]));

        children = children[1].getChildren();
        assertTrue(compareBytes(firstGranChildFrameBytes, children[0]));
        assertTrue(compareBytes(secondGranChildFrameBytes, children[1]));

        assertTrue(compareBytes(frameBytes, frame));
    }

    @Test
    public void testStartDelimiterAndSize() throws FrameModelException, FrameProcessorException, FrameException {
        processor.setXmlModel(FRAME_XML_MODEL2_PATH);
        processor.push(new byte[]{(byte) 0xff});
        Frame frame = this.push(frameBytes2);
        assertNotNull(frame);

        Frame[] children = frame.getChildren();
        assertTrue(compareBytes(firstChildFrameBytes2, children[0]));
        assertTrue(compareBytes(secondChildFrameBytes2, children[1]));
        assertTrue(compareBytes(thirdChildFrameBytes2, children[2]));
        children = children[2].getChildren();
        assertTrue(compareBytes(firstGranChildFrameBytes2, children[0]));
        assertTrue(compareBytes(secondGranChildFrameBytes2, children[1]));

        long date = ((UnixEpoch) children[0]).getDate().getTime();
        assertEquals(dateLng, date);
        System.out.println(new Date(dateLng) + " - " + new Date(date));

        String data = ((ASCIIDataFrame) children[1]).getData();
        assertEquals(message, data);

        assertTrue(compareBytes(frameBytes2, frame));
    }

    @Test
    public void testCleanedFrame() throws FrameModelException, FrameProcessorException, FrameException {
        processor.setXmlModel(FRAME_XML_MODEL2_PATH);
        processor.push(new byte[]{(byte) 0xff});
        Frame frame = this.push(frameBytes3);
        assertNotNull(frame);

        Frame[] children = frame.getChildren();
        assertTrue(compareBytes(firstChildFrameBytes3, children[0]));
        assertTrue(compareBytes(secondChildFrameBytes3, children[1]));

        assertEquals(2, children.length);
        assertTrue(compareBytes(frameBytes3, frame));
    }

    @Test
    public void testEscapedFrame() throws FrameModelException, FrameProcessorException, FrameException {
        processor.setXmlModel(FRAME_XML_MODEL2_PATH);
        processor.push(new byte[]{(byte) 0xff});
        Frame frame = this.push(escapedFrame);
        assertNotNull(frame);
        assertTrue(compareBytes(escapedFrame, frame));
    }

    @Test
    public void testEscapedTwiceFrame() throws FrameModelException, FrameProcessorException, FrameException {
        processor.setXmlModel(FRAME_XML_MODEL2_PATH);
        processor.push(new byte[]{(byte) 0xff});
        Frame frame = this.push(escapedTwiceFrame);
        assertNotNull(frame);
        assertTrue(compareBytes(escapedTwiceFinalFrame, frame));
    }

    private boolean compareBytes(byte[] expected, Frame tested) {
        if (tested == null || expected == null) {
            return false;
        }
        byte[] testedBytes = tested.getBytes();
        if (testedBytes.length != expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (testedBytes[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    public URL getModelURL() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.util.frame.ProcessorListener#push(org.eclipse.sensinact.gateway.util.frame.Frame, int, byte[])
     */
    public void push(Frame frame, int delimitation, byte[] delimiters) {
        synchronized (pushed) {
            pushed.set(true);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.util.frame.ProcessorListener#getFrameFactory()
     */
    public FrameFactory getFrameFactory() {
        return new FrameFactoryImpl();
    }

    private Frame push(byte[] bytes) {
        synchronized (pushed) {
            pushed.set(false);
        }
        this.processor.push(bytes);
        int wait = 3000;
        while (wait > 0) {
            synchronized (pushed) {
                if (pushed.get()) {
                    break;
                }
            }
            try {
                Thread.sleep(500);
                wait -= 500;

            } catch (InterruptedException e) {
                Thread.interrupted();
                e.printStackTrace();
            }
        }
        Frame frame = processor.getFrame();
        return frame;
    }

    private final AtomicBoolean pushed = new AtomicBoolean(false);
}
