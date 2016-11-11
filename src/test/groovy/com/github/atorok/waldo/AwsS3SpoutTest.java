package com.github.atorok.waldo;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwsS3SpoutTest {

    public static final List<S3ObjectSummary> OBJECT_SUMMARIES = Arrays.asList(
            mock(S3ObjectSummary.class),
            mock(S3ObjectSummary.class),
            mock(S3ObjectSummary.class)
    );
    @Mock
    AmazonS3 client;
    @Mock
    ListObjectsV2Result resultMock;

    AwsS3Spout testee;

    @Before
    public void setUp() throws Exception {
        testee = new AwsS3Spout(client);
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
                resultMock
        );
        when(resultMock.getObjectSummaries()).thenReturn(
                OBJECT_SUMMARIES
        );
    }

    @Test
    public void noChainingTest() throws Exception {
        assertTrue(testee.hasNext());
        assertObjectSummariesInTestee();
        assertFalse(testee.hasNext());
    }

    @Test
    public void iterationTest() throws Exception {
        int i;
        for (i = 0; testee.hasNext(); i++) {
            assertEquals(OBJECT_SUMMARIES.get(i), ((AwsS3BackedDrop) testee.next()).getSummary());
        }
        assertEquals(OBJECT_SUMMARIES.size(), i);
    }

    @Test
    public void chainedIterators() throws Exception {
        when(resultMock.isTruncated()).thenReturn(true).thenReturn(false);
        assertTrue(testee.hasNext());
        assertObjectSummariesInTestee();
        assertTrue(testee.hasNext());
        assertObjectSummariesInTestee();
        assertFalse(testee.hasNext());
    }

    private void assertObjectSummariesInTestee() {
        assertArrayEquals(
                OBJECT_SUMMARIES.toArray(),
                OBJECT_SUMMARIES.stream().map(
                        // do not do this cast outside of test code
                        each -> ((AwsS3BackedDrop) testee.next()).getSummary()
                ).toArray()
        );
    }


}