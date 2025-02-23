package com.nwu.medimagebackend;

import net.imagej.ImageJ;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MedimagebackendApplicationTests {

    @Test
    void testImagej() {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();

    }

}
