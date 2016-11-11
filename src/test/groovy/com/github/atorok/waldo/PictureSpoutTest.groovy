package com.github.atorok.waldo

import spock.lang.Specification

class PictureSpoutTest extends Specification {

    static class TestDrop implements PictureDrop {
        InputStream metadata;
        String overallChecksum;
    }

    static class TestSpout implements PictureSpout {

        private current = 0;
        private all = ['a', 'b', 'c'].collect({
            new TestDrop(metadata: new ByteArrayInputStream("$it".bytes), overallChecksum: "check_$it")
        });

        @Override
        boolean hasNext() {
            return current < all.size();
        }

        @Override
        PictureDrop next() {
            def result = all[current]
            current++;
            return result;
        }
    }

    def "able to threat iterator as array"() {
        when:
            def checksums = (new TestSpout()).collect({
                it.overallChecksum[-1]
            })
        then:
            checksums == 'abc'.bytes;
    }


}
